using BankingApp.Models;

namespace BankingApp.DTOs.Accounts;

public class AccountResponse
{
    public int Id { get; set; }
    public string IBAN { get; set; } = string.Empty;
    public AccountType Type { get; set; }
    public AccountStatus Status { get; set; }
    public decimal Balance { get; set; }
    public string Currency { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }

    // Type-specific fields — null when not applicable
    public decimal? OverdraftLimit { get; set; }
    public decimal? InterestRate { get; set; }
    public string? CompanyName { get; set; }

    public static AccountResponse FromAccount(Account account) => new()
    {
        Id = account.Id,
        IBAN = account.IBAN,
        Type = account.Type,
        Status = account.Status,
        Balance = account.Balance,
        Currency = account.Currency,
        CreatedAt = account.CreatedAt,
        OverdraftLimit = account is CurrentAccount ca ? ca.OverdraftLimit : null,
        InterestRate = account is SavingsAccount sa ? sa.InterestRate : null,
        CompanyName = account is BusinessAccount ba ? ba.CompanyName : null,
    };
}
