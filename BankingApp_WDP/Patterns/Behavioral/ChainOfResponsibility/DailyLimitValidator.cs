using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Behavioral.ChainOfResponsibility;

/// <summary>
/// Chain link #3 — rejects outgoing transactions that would push the account's daily
/// debit total past the configured DailyTransactionLimit.
/// Deposits are skipped (no daily cap on incoming funds).
/// </summary>
public class DailyLimitValidator : TransactionValidatorBase
{
    private readonly AppDbContext _dbContext;
    private readonly AppConfigurationManager _config;

    public DailyLimitValidator(AppDbContext dbContext, AppConfigurationManager config)
    {
        _dbContext = dbContext;
        _config = config;
    }

    public override void Validate(Transaction tx, Account account)
    {
        if (tx.Type is TransactionType.Deposit)
        {
            PassToNext(tx, account);
            return;
        }

        var today = DateTime.UtcNow.Date;

        var dailyTotal = _dbContext.Transactions
            .Where(t => t.FromAccountId == account.Id
                     && t.CreatedAt >= today
                     && t.Status != TransactionStatus.Failed
                     && t.Status != TransactionStatus.Cancelled)
            .Sum(t => (decimal?)t.Amount) ?? 0m;

        var limit = _config.BankingLimits.DailyTransactionLimit;

        if (dailyTotal + tx.Amount > limit)
            throw new InvalidOperationException(
                $"Daily transaction limit exceeded. Limit: {limit} MDL, " +
                $"used today: {dailyTotal} MDL, requested: {tx.Amount} MDL.");

        PassToNext(tx, account);
    }
}
