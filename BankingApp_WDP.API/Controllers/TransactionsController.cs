using System.Security.Claims;
using BankingApp.Data;
using BankingApp.DTOs.Transactions;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.ChainOfResponsibility;
using BankingApp.Patterns.Behavioral.Command;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Patterns.Structural.Decorator;
using BankingApp.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Controllers;

[ApiController]
[Route("api/transactions")]
[Authorize]
public class TransactionsController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly IAccountService _accountService;
    private readonly ITransactionCommandInvoker _invoker;
    private readonly ITransactionValidator _validator;
    private readonly ITransactionProcessor _processor;
    private readonly ILoggerService _logger;

    public TransactionsController(
        AppDbContext db,
        IAccountService accountService,
        ITransactionCommandInvoker invoker,
        ITransactionValidator validator,
        ITransactionProcessor processor,
        ILoggerService logger)
    {
        _db = db;
        _accountService = accountService;
        _invoker = invoker;
        _validator = validator;
        _processor = processor;
        _logger = logger;
    }

    private string CurrentUserId => User.FindFirstValue(ClaimTypes.NameIdentifier)!;

    // GET /api/transactions?page=1&pageSize=20&type=Deposit&status=Completed&from=2025-01-01&to=2025-12-31
    [HttpGet]
    public async Task<IActionResult> GetAll(
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20,
        [FromQuery] string? type = null,
        [FromQuery] string? status = null,
        [FromQuery] DateTime? from = null,
        [FromQuery] DateTime? to = null)
    {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        var query = _db.Transactions
            .Include(t => t.FromAccount)
            .Include(t => t.ToAccount)
            .Where(t =>
                (t.FromAccount != null && t.FromAccount.UserId == CurrentUserId) ||
                (t.ToAccount != null && t.ToAccount.UserId == CurrentUserId));

        if (Enum.TryParse<TransactionType>(type, ignoreCase: true, out var parsedType))
            query = query.Where(t => t.Type == parsedType);

        if (Enum.TryParse<TransactionStatus>(status, ignoreCase: true, out var parsedStatus))
            query = query.Where(t => t.Status == parsedStatus);

        if (from.HasValue)
            query = query.Where(t => t.CreatedAt >= from.Value.ToUniversalTime());

        if (to.HasValue)
            query = query.Where(t => t.CreatedAt <= to.Value.ToUniversalTime());

        var total = await query.CountAsync();
        var items = await query
            .OrderByDescending(t => t.CreatedAt)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return Ok(new
        {
            page,
            pageSize,
            total,
            items = items.Select(TransactionResponse.FromTransaction)
        });
    }

    // GET /api/transactions/{id}
    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetById(int id)
    {
        var tx = await _db.Transactions
            .Include(t => t.FromAccount)
            .Include(t => t.ToAccount)
            .FirstOrDefaultAsync(t => t.Id == id);

        if (tx == null)
            return NotFound(new { message = "Tranzacția nu a fost găsită." });

        var belongsToUser =
            (tx.FromAccount?.UserId == CurrentUserId) ||
            (tx.ToAccount?.UserId == CurrentUserId);

        if (!belongsToUser)
            return Forbid();

        return Ok(TransactionResponse.FromTransaction(tx));
    }

    // POST /api/transactions/deposit
    [HttpPost("deposit")]
    public async Task<IActionResult> Deposit([FromBody] DepositRequest request)
    {
        try
        {
            var account = await _accountService.GetByIdAsync(request.ToAccountId);
            if (account == null)
                return NotFound(new { message = $"Contul #{request.ToAccountId} nu a fost găsit." });

            if (account.UserId != CurrentUserId)
                return Forbid();

            var command = new DepositCommand(
                _db, _validator, _processor, _logger,
                request.ToAccountId, request.Amount, request.Currency, request.Description);

            await _invoker.ExecuteAsync(command);

            var tx = await _db.Transactions
                .OrderByDescending(t => t.Id)
                .FirstOrDefaultAsync(t =>
                    t.ToAccountId == request.ToAccountId &&
                    t.Type == TransactionType.Deposit);

            return CreatedAtAction(nameof(GetById), new { id = tx?.Id }, tx == null ? null : TransactionResponse.FromTransaction(tx));
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // POST /api/transactions/withdraw
    [HttpPost("withdraw")]
    public async Task<IActionResult> Withdraw([FromBody] WithdrawRequest request)
    {
        try
        {
            var account = await _accountService.GetByIdAsync(request.FromAccountId);
            if (account == null)
                return NotFound(new { message = $"Contul #{request.FromAccountId} nu a fost găsit." });

            if (account.UserId != CurrentUserId)
                return Forbid();

            var command = new WithdrawalCommand(
                _db, _validator, _processor, _logger,
                request.FromAccountId, request.Amount, request.Currency, request.Description);

            await _invoker.ExecuteAsync(command);

            var tx = await _db.Transactions
                .OrderByDescending(t => t.Id)
                .FirstOrDefaultAsync(t =>
                    t.FromAccountId == request.FromAccountId &&
                    t.Type == TransactionType.Withdrawal);

            return CreatedAtAction(nameof(GetById), new { id = tx?.Id }, tx == null ? null : TransactionResponse.FromTransaction(tx));
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // POST /api/transactions/transfer
    [HttpPost("transfer")]
    public async Task<IActionResult> Transfer([FromBody] TransferRequest request)
    {
        try
        {
            if (request.FromAccountId == request.ToAccountId)
                return BadRequest(new { message = "Contul sursă și destinație nu pot fi același." });

            var fromAccount = await _accountService.GetByIdAsync(request.FromAccountId);
            if (fromAccount == null)
                return NotFound(new { message = $"Contul sursă #{request.FromAccountId} nu a fost găsit." });

            if (fromAccount.UserId != CurrentUserId)
                return Forbid();

            var toAccount = await _accountService.GetByIdAsync(request.ToAccountId);
            if (toAccount == null)
                return NotFound(new { message = $"Contul destinație #{request.ToAccountId} nu a fost găsit." });

            var command = new TransferCommand(
                _db, _validator, _processor, _logger,
                request.FromAccountId, request.ToAccountId,
                request.Amount, request.Currency, request.Description);

            await _invoker.ExecuteAsync(command);

            var tx = await _db.Transactions
                .OrderByDescending(t => t.Id)
                .FirstOrDefaultAsync(t =>
                    t.FromAccountId == request.FromAccountId &&
                    t.ToAccountId == request.ToAccountId &&
                    t.Type == TransactionType.Transfer);

            return CreatedAtAction(nameof(GetById), new { id = tx?.Id }, tx == null ? null : TransactionResponse.FromTransaction(tx));
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // GET /api/accounts/{id}/transactions?page=1&pageSize=20
    [HttpGet("/api/accounts/{accountId:int}/transactions")]
    public async Task<IActionResult> GetByAccount(
        int accountId,
        [FromQuery] int page = 1,
        [FromQuery] int pageSize = 20)
    {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        try
        {
            var account = await _accountService.GetByIdAsync(accountId);
            if (account == null)
                return NotFound(new { message = "Contul nu a fost găsit." });

            var query = _db.Transactions
                .Where(t => t.FromAccountId == accountId || t.ToAccountId == accountId);

            var total = await query.CountAsync();
            var items = await query
                .OrderByDescending(t => t.CreatedAt)
                .Skip((page - 1) * pageSize)
                .Take(pageSize)
                .ToListAsync();

            return Ok(new
            {
                accountId,
                page,
                pageSize,
                total,
                items = items.Select(TransactionResponse.FromTransaction)
            });
        }
        catch (UnauthorizedAccessException)
        {
            return Forbid();
        }
    }
}
