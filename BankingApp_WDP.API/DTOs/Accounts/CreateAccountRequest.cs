using System.ComponentModel.DataAnnotations;
using BankingApp.Models;

namespace BankingApp.DTOs.Accounts;

public class CreateAccountRequest
{
    [Required]
    public AccountType Type { get; set; }

    [MaxLength(3)]
    public string Currency { get; set; } = "MDL";

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
