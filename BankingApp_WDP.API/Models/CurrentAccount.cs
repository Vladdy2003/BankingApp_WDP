using System.ComponentModel.DataAnnotations.Schema;

namespace BankingApp.Models;

public class CurrentAccount : Account
{
    [Column(TypeName = "decimal(18,2)")]
    public decimal OverdraftLimit { get; set; } = 0;
}
