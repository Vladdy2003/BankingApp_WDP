using System.ComponentModel.DataAnnotations.Schema;

namespace BankingApp.Models;

public class SavingsAccount : Account
{
    [Column(TypeName = "decimal(5,4)")]
    public decimal InterestRate { get; set; } = 0;
}
