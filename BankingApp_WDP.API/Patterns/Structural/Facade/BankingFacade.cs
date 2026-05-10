using BankingApp.Data;
using BankingApp.DTOs.Banking;
using BankingApp.DTOs.Transactions;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.ChainOfResponsibility;
using BankingApp.Patterns.Behavioral.Command;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Patterns.Structural.Decorator;
using BankingApp.Services;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Patterns.Structural.Facade;

// Facade Pattern #12 — BankingFacade unifica AccountService, Command, Chain, Observer
// într-o interfață de nivel înalt. Controllere și clienți externi nu mai știu de subsisteme.
public class BankingFacade : IBankingFacade
{
    private readonly AppDbContext _db;
    private readonly IAccountService _accountService;
    private readonly ITransactionCommandInvoker _invoker;
    private readonly ITransactionValidator _validator;
    private readonly ITransactionProcessor _processor;
    private readonly ILoggerService _logger;
    private readonly IReportService _reportService;

    public BankingFacade(
        AppDbContext db,
        IAccountService accountService,
        ITransactionCommandInvoker invoker,
        ITransactionValidator validator,
        ITransactionProcessor processor,
        ILoggerService logger,
        IReportService reportService)
    {
        _db = db;
        _accountService = accountService;
        _invoker = invoker;
        _validator = validator;
        _processor = processor;
        _logger = logger;
        _reportService = reportService;
    }

    // --- GetAccountSummary ---
    // Agregă date din AccountService (Proxy #4), Cards și Transactions într-un singur DTO.
    public async Task<AccountSummaryDto> GetAccountSummaryAsync(string userId)
    {
        _logger.LogInformation($"[Facade] GetAccountSummary pentru userId={userId}");

        var user = await _db.Users.FindAsync(userId) as ApplicationUser
            ?? throw new InvalidOperationException($"Utilizatorul {userId} nu a fost găsit.");

        var accounts = (await _accountService.GetByUserIdAsync(userId)).ToList();
        var accountIds = accounts.Select(a => a.Id).ToList();

        var cardCounts = await _db.Cards
            .Where(c => accountIds.Contains(c.AccountId) && c.Status == CardStatus.Active)
            .GroupBy(c => c.AccountId)
            .Select(g => new { AccountId = g.Key, Count = g.Count() })
            .ToDictionaryAsync(x => x.AccountId, x => x.Count);

        var recentTransactions = await _db.Transactions
            .Include(t => t.FromAccount)
            .Include(t => t.ToAccount)
            .Where(t =>
                (t.FromAccount != null && t.FromAccount.UserId == userId) ||
                (t.ToAccount   != null && t.ToAccount.UserId   == userId))
            .OrderByDescending(t => t.CreatedAt)
            .Take(10)
            .ToListAsync();

        var summary = new AccountSummaryDto
        {
            UserId = userId,
            FullName = $"{user.FirstName} {user.LastName}".Trim(),
            TotalBalance = accounts.Sum(a => a.Balance),
            AccountCount = accounts.Count,
            ActiveCardCount = cardCounts.Values.Sum(),
            Accounts = accounts.Select(a => new AccountSummaryItem
            {
                Id = a.Id,
                IBAN = a.IBAN,
                Type = a.Type.ToString(),
                Status = a.Status.ToString(),
                Balance = a.Balance,
                Currency = a.Currency,
                CardCount = cardCounts.GetValueOrDefault(a.Id, 0)
            }).ToList(),
            RecentTransactions = recentTransactions.Select(t => new RecentTransactionItem
            {
                Id = t.Id,
                Type = t.Type.ToString(),
                Amount = t.Amount,
                Currency = t.Currency,
                Status = t.Status.ToString(),
                Description = t.Description,
                CreatedAt = t.CreatedAt
            }).ToList()
        };

        _logger.LogInformation($"[Facade] AccountSummary: {accounts.Count} conturi, {summary.ActiveCardCount} carduri active.");
        return summary;
    }

    // --- ProcessCompleteTransfer ---
    // Orchestrează: verificare acces → TransferCommand (include Chain #6 și Observer #11).
    public async Task<TransactionResponse> ProcessCompleteTransferAsync(TransferRequest transferDto, string initiatorUserId)
    {
        _logger.LogInformation($"[Facade] ProcessCompleteTransfer: {transferDto.Amount} {transferDto.Currency} cont {transferDto.FromAccountId} → {transferDto.ToAccountId}");

        if (transferDto.FromAccountId == transferDto.ToAccountId)
            throw new InvalidOperationException("Contul sursă și destinație nu pot fi același.");

        var fromAccount = await _accountService.GetByIdAsync(transferDto.FromAccountId)
            ?? throw new InvalidOperationException($"Contul sursă #{transferDto.FromAccountId} nu a fost găsit.");

        if (fromAccount.UserId != initiatorUserId)
            throw new UnauthorizedAccessException("Nu aveți permisiunea de a efectua transferuri din acest cont.");

        _ = await _accountService.GetByIdAsync(transferDto.ToAccountId)
            ?? throw new InvalidOperationException($"Contul destinație #{transferDto.ToAccountId} nu a fost găsit.");

        // TransferCommand include intern Chain of Responsibility #6 (validator).
        // Observer #11 este declanșat de BasicTransactionProcessor după ProcessAsync.
        var command = new TransferCommand(
            _db,
            _validator,
            _processor,
            _logger,
            transferDto.FromAccountId,
            transferDto.ToAccountId,
            transferDto.Amount,
            transferDto.Currency,
            transferDto.Description);

        await _invoker.ExecuteAsync(command);

        var tx = await _db.Transactions
            .OrderByDescending(t => t.Id)
            .FirstOrDefaultAsync(t =>
                t.FromAccountId == transferDto.FromAccountId &&
                t.ToAccountId == transferDto.ToAccountId   &&
                t.Type == TransactionType.Transfer)
            ?? throw new InvalidOperationException("Tranzacția nu a putut fi recuperată după execuție.");

        _logger.LogInformation($"[Facade] Transfer finalizat. TxId={tx.Id}, Status={tx.Status}");
        return TransactionResponse.FromTransaction(tx);
    }

    // --- GenerateMonthlyReport ---
    // Deleghează la IReportService (pasul 6.2) pentru a evita duplicarea logicii.
    public Task<MonthlyReportDto> GenerateMonthlyReportAsync(int accountId, int year, int month)
    {
        _logger.LogInformation($"[Facade] GenerateMonthlyReport → ReportService: contId={accountId}, {year}/{month:D2}");
        return _reportService.GetMonthlyStatementAsync(accountId, year, month);
    }
}
