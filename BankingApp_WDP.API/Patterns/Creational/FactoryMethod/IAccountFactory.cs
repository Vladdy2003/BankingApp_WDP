using BankingApp.Models;

namespace BankingApp.Patterns.Creational.FactoryMethod;

/// <summary>
/// Options passed to the factory to configure the new account.
/// Type-specific fields are nullable; only the relevant ones are used.
/// </summary>
public class AccountCreationOptions
{
    public string Currency { get; set; } = "MDL";

    // CurrentAccount
    public decimal? OverdraftLimit { get; set; }

    // SavingsAccount
    public decimal? InterestRate { get; set; }

    // BusinessAccount
    public string? CompanyName { get; set; }
}

/// <summary>
/// Factory Method pattern — defines the contract for creating Account objects.
/// Concrete factories decide which subclass to instantiate.
/// </summary>
public interface IAccountFactory
{
    Account CreateAccount(AccountType type, string userId, AccountCreationOptions options);
}
