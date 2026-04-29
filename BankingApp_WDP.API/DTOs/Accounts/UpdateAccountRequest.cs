using System.ComponentModel.DataAnnotations;

namespace BankingApp.DTOs.Accounts;

public class UpdateAccountRequest
{
    [MaxLength(3)]
    public string? Currency { get; set; }

    // CurrentAccount
    [Range(0, double.MaxValue)]
    public decimal? OverdraftLimit { get; set; }

    // SavingsAccount
    [Range(0, 1)]
    public decimal? InterestRate { get; set; }

    // BusinessAccount
    [MaxLength(200)]
    public string? CompanyName { get; set; }
}
