using BankingApp.Models;
using BankingApp.Utilities;

namespace BankingApp.Patterns.Creational.FactoryMethod;

/// <summary>
/// Factory Method Pattern #2 — concrete factory that creates the correct Account subclass
/// based on AccountType and initializes it with type-specific defaults.
/// </summary>
public class AccountFactory : IAccountFactory
{
    private readonly IIBANGenerator _ibanGenerator;

    public AccountFactory(IIBANGenerator ibanGenerator)
    {
        _ibanGenerator = ibanGenerator;
    }

    public Account CreateAccount(AccountType type, string userId, AccountCreationOptions options)
    {
        Account account = type switch
        {
            AccountType.Current  => CreateCurrentAccount(options),
            AccountType.Savings  => CreateSavingsAccount(options),
            AccountType.Business => CreateBusinessAccount(options),
            _ => throw new ArgumentException($"Unknown account type: {type}", nameof(type))
        };

        // Common initialization applied to every account regardless of type
        account.IBAN      = _ibanGenerator.Generate();
        account.UserId    = userId;
        account.Type      = type;
        account.Currency  = options.Currency;
        account.Status    = AccountStatus.Active;
        account.Balance   = 0;
        account.CreatedAt = DateTime.UtcNow;

        return account;
    }

    // Each private method encapsulates the type-specific initialization logic.

    private static CurrentAccount CreateCurrentAccount(AccountCreationOptions options) =>
        new()
        {
            // Default overdraft limit: 500 MDL; can be overridden via options
            OverdraftLimit = options.OverdraftLimit ?? 500m
        };

    private static SavingsAccount CreateSavingsAccount(AccountCreationOptions options) =>
        new()
        {
            // Default annual interest rate: 5 %; can be overridden via options
            InterestRate = options.InterestRate ?? 0.05m
        };

    private static BusinessAccount CreateBusinessAccount(AccountCreationOptions options) =>
        new()
        {
            CompanyName = options.CompanyName
        };
}
