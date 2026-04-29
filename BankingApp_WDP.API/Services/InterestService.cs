using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.Strategy;
using BankingApp.Patterns.Creational.Singleton;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Services;

public class InterestService : IInterestService
{
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;
    private readonly Dictionary<AccountType, IInterestCalculationStrategy> _strategies;

    public InterestService(
        AppDbContext db,
        ILoggerService logger,
        SimpleInterestStrategy simple,
        CompoundInterestStrategy compound,
        NoInterestStrategy noInterest)
    {
        _db = db;
        _logger = logger;
        _strategies = new Dictionary<AccountType, IInterestCalculationStrategy>
        {
            [AccountType.Savings]  = simple,
            [AccountType.Business] = compound,
            [AccountType.Current]  = noInterest
        };
    }

    public async Task<decimal> CalculateMonthlyInterestAsync(int accountId)
    {
        var account = await _db.Accounts.FindAsync(accountId)
            ?? throw new InvalidOperationException($"Contul {accountId} nu există.");

        var strategy = _strategies[account.Type];
        return strategy.Calculate(account, account.Balance);
    }

    public async Task<decimal> ApplyMonthlyInterestAsync(int accountId)
    {
        var account = await _db.Accounts.FindAsync(accountId)
            ?? throw new InvalidOperationException($"Contul {accountId} nu există.");

        var strategy = _strategies[account.Type];
        var interest = strategy.Calculate(account, account.Balance);

        if (interest > 0)
        {
            account.Balance += interest;
            await _db.SaveChangesAsync();
            _logger.LogInformation($"[InterestService] Dobândă aplicată contului {accountId}: +{interest} {account.Currency} (strategie: {strategy.GetType().Name})");
        }

        return interest;
    }
}
