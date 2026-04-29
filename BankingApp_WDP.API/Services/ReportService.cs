using BankingApp.Data;
using BankingApp.DTOs.Banking;
using BankingApp.DTOs.Reports;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Services;

public class ReportService : IReportService
{
    private readonly AppDbContext _db;
    private readonly IAccountService _accountService;
    private readonly ILoggerService _logger;

    public ReportService(AppDbContext db, IAccountService accountService, ILoggerService logger)
    {
        _db = db;
        _accountService = accountService;
        _logger = logger;
    }

    public async Task<MonthlyReportDto> GetMonthlyStatementAsync(int accountId, int year, int month)
    {
        _logger.LogInformation($"[ReportService] GetMonthlyStatement: contId={accountId}, {year}/{month:D2}");

        var account = await _accountService.GetByIdAsync(accountId)
            ?? throw new InvalidOperationException($"Contul #{accountId} nu a fost găsit.");

        var periodStart = new DateTime(year, month, 1, 0, 0, 0, DateTimeKind.Utc);
        var periodEnd   = periodStart.AddMonths(1);

        var transactions = await _db.Transactions
            .Where(t =>
                (t.FromAccountId == accountId || t.ToAccountId == accountId) &&
                t.CreatedAt >= periodStart &&
                t.CreatedAt <  periodEnd &&
                t.Status != TransactionStatus.Cancelled &&
                t.Status != TransactionStatus.Failed)
            .OrderBy(t => t.CreatedAt)
            .ToListAsync();

        decimal totalCredits = 0m;
        decimal totalDebits  = 0m;
        decimal totalFees    = 0m;

        var lineItems = new List<TransactionLineItem>();
        foreach (var tx in transactions)
        {
            bool isCredit = tx.ToAccountId == accountId;
            bool isFee    = tx.Type == TransactionType.Payment &&
                            tx.Description?.StartsWith("Comision") == true;

            if (isFee)
                totalFees += tx.Amount;
            else if (isCredit)
                totalCredits += tx.Amount;
            else
                totalDebits += tx.Amount;

            lineItems.Add(new TransactionLineItem
            {
                Id          = tx.Id,
                Date        = tx.CreatedAt,
                Type        = tx.Type.ToString(),
                Description = tx.Description,
                Amount      = tx.Amount,
                Currency    = tx.Currency,
                Status      = tx.Status.ToString(),
                Direction   = isCredit ? "Credit" : "Debit"
            });
        }

        decimal closingBalance = account.Balance;
        decimal openingBalance = closingBalance - totalCredits + totalDebits + totalFees;

        return new MonthlyReportDto
        {
            AccountId        = accountId,
            IBAN             = account.IBAN,
            Currency         = account.Currency,
            Year             = year,
            Month            = month,
            OpeningBalance   = openingBalance,
            ClosingBalance   = closingBalance,
            TotalCredits     = totalCredits,
            TotalDebits      = totalDebits,
            TotalFees        = totalFees,
            TransactionCount = transactions.Count,
            Transactions     = lineItems
        };
    }

    public async Task<AnnualSummaryDto> GetAnnualSummaryAsync(int accountId, int year)
    {
        _logger.LogInformation($"[ReportService] GetAnnualSummary: contId={accountId}, {year}");

        var account = await _accountService.GetByIdAsync(accountId)
            ?? throw new InvalidOperationException($"Contul #{accountId} nu a fost găsit.");

        var yearStart = new DateTime(year, 1, 1, 0, 0, 0, DateTimeKind.Utc);
        var yearEnd   = yearStart.AddYears(1);

        var transactions = await _db.Transactions
            .Where(t =>
                (t.FromAccountId == accountId || t.ToAccountId == accountId) &&
                t.CreatedAt >= yearStart &&
                t.CreatedAt <  yearEnd &&
                t.Status != TransactionStatus.Cancelled &&
                t.Status != TransactionStatus.Failed)
            .ToListAsync();

        var monthlyBreakdowns = new List<MonthlyBreakdown>();
        decimal annualCredits = 0m;
        decimal annualDebits  = 0m;
        decimal annualFees    = 0m;

        var monthNames = new[]
        {
            "", "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
            "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"
        };

        for (int m = 1; m <= 12; m++)
        {
            var monthStart = new DateTime(year, m, 1, 0, 0, 0, DateTimeKind.Utc);
            var monthEnd   = monthStart.AddMonths(1);

            var monthTx = transactions
                .Where(t => t.CreatedAt >= monthStart && t.CreatedAt < monthEnd)
                .ToList();

            decimal credits = 0m;
            decimal debits  = 0m;
            decimal fees    = 0m;

            foreach (var tx in monthTx)
            {
                bool isCredit = tx.ToAccountId == accountId;
                bool isFee    = tx.Type == TransactionType.Payment &&
                                tx.Description?.StartsWith("Comision") == true;

                if (isFee)         fees    += tx.Amount;
                else if (isCredit) credits += tx.Amount;
                else               debits  += tx.Amount;
            }

            annualCredits += credits;
            annualDebits  += debits;
            annualFees    += fees;

            monthlyBreakdowns.Add(new MonthlyBreakdown
            {
                Month            = m,
                MonthName        = monthNames[m],
                Credits          = credits,
                Debits           = debits,
                Fees             = fees,
                TransactionCount = monthTx.Count
            });
        }

        return new AnnualSummaryDto
        {
            AccountId          = accountId,
            IBAN               = account.IBAN,
            Currency           = account.Currency,
            Year               = year,
            TotalCredits       = annualCredits,
            TotalDebits        = annualDebits,
            TotalFees          = annualFees,
            NetChange          = annualCredits - annualDebits - annualFees,
            TotalTransactions  = transactions.Count,
            MonthlyBreakdowns  = monthlyBreakdowns
        };
    }

    public async Task<SpendingByCategoryDto> GetSpendingByCategoryAsync(int accountId, DateTime from, DateTime to)
    {
        _logger.LogInformation($"[ReportService] GetSpendingByCategory: contId={accountId}, {from:yyyy-MM-dd}–{to:yyyy-MM-dd}");

        _ = await _accountService.GetByIdAsync(accountId)
            ?? throw new InvalidOperationException($"Contul #{accountId} nu a fost găsit.");

        var fromUtc = DateTime.SpecifyKind(from.Date, DateTimeKind.Utc);
        var toUtc   = DateTime.SpecifyKind(to.Date.AddDays(1), DateTimeKind.Utc);

        // Only outgoing (debit) transactions from this account
        var transactions = await _db.Transactions
            .Where(t =>
                t.FromAccountId == accountId &&
                t.CreatedAt >= fromUtc &&
                t.CreatedAt <  toUtc &&
                t.Status != TransactionStatus.Cancelled &&
                t.Status != TransactionStatus.Failed)
            .ToListAsync();

        var categoryMap = new Dictionary<string, (decimal Amount, int Count)>(StringComparer.Ordinal);

        foreach (var tx in transactions)
        {
            string category = tx.Type switch
            {
                TransactionType.Withdrawal => "Retrageri",
                TransactionType.Transfer   => "Transferuri",
                TransactionType.Payment when tx.Description?.StartsWith("Comision") == true => "Comisioane",
                TransactionType.Payment    => "Plăți",
                _                          => "Altele"
            };

            if (!categoryMap.TryGetValue(category, out var entry))
                entry = (0m, 0);

            categoryMap[category] = (entry.Amount + tx.Amount, entry.Count + 1);
        }

        decimal total = categoryMap.Values.Sum(v => v.Amount);

        var categories = categoryMap
            .Select(kv => new CategoryBreakdown
            {
                Category         = kv.Key,
                Amount           = kv.Value.Amount,
                Percentage       = total > 0 ? Math.Round(kv.Value.Amount / total * 100, 2) : 0m,
                TransactionCount = kv.Value.Count
            })
            .OrderByDescending(c => c.Amount)
            .ToList();

        return new SpendingByCategoryDto
        {
            AccountId    = accountId,
            From         = fromUtc,
            To           = toUtc.AddDays(-1),
            TotalSpending = total,
            Categories   = categories
        };
    }

    public async Task<IncomeVsExpensesDto> GetIncomeVsExpensesAsync(int accountId, DateTime from, DateTime to)
    {
        _logger.LogInformation($"[ReportService] GetIncomeVsExpenses: contId={accountId}, {from:yyyy-MM-dd}–{to:yyyy-MM-dd}");

        _ = await _accountService.GetByIdAsync(accountId)
            ?? throw new InvalidOperationException($"Contul #{accountId} nu a fost găsit.");

        var fromUtc = DateTime.SpecifyKind(from.Date, DateTimeKind.Utc);
        var toUtc   = DateTime.SpecifyKind(to.Date.AddDays(1), DateTimeKind.Utc);

        var transactions = await _db.Transactions
            .Where(t =>
                (t.FromAccountId == accountId || t.ToAccountId == accountId) &&
                t.CreatedAt >= fromUtc &&
                t.CreatedAt <  toUtc &&
                t.Status != TransactionStatus.Cancelled &&
                t.Status != TransactionStatus.Failed)
            .ToListAsync();

        // Group by year-month
        var grouped = transactions
            .GroupBy(t => new { t.CreatedAt.Year, t.CreatedAt.Month })
            .OrderBy(g => g.Key.Year).ThenBy(g => g.Key.Month);

        decimal totalIncome   = 0m;
        decimal totalExpenses = 0m;
        var periods = new List<PeriodBreakdown>();

        foreach (var group in grouped)
        {
            decimal income   = 0m;
            decimal expenses = 0m;

            foreach (var tx in group)
            {
                bool isCredit = tx.ToAccountId == accountId;
                if (isCredit)
                    income += tx.Amount;
                else
                    expenses += tx.Amount;
            }

            totalIncome   += income;
            totalExpenses += expenses;

            periods.Add(new PeriodBreakdown
            {
                Period   = $"{group.Key.Year}-{group.Key.Month:D2}",
                Income   = income,
                Expenses = expenses,
                Net      = income - expenses
            });
        }

        return new IncomeVsExpensesDto
        {
            AccountId     = accountId,
            From          = fromUtc,
            To            = toUtc.AddDays(-1),
            TotalIncome   = totalIncome,
            TotalExpenses = totalExpenses,
            NetBalance    = totalIncome - totalExpenses,
            Periods       = periods
        };
    }
}
