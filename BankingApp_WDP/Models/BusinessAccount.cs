using System.ComponentModel.DataAnnotations;

namespace BankingApp.Models;

public class BusinessAccount : Account
{
    [MaxLength(200)]
    public string? CompanyName { get; set; }
}
