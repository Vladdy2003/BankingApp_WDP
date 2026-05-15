using System.ComponentModel.DataAnnotations;

namespace BankingApp.DTOs.Transactions;

public class DepositRequest
{
    [Required]
    public int ToAccountId { get; set; }

    [Required, Range(0.01, double.MaxValue, ErrorMessage = "Suma trebuie să fie pozitivă.")]
    public decimal Amount { get; set; }

    [StringLength(3, MinimumLength = 3)]
    public string Currency { get; set; } = "MDL";

    [StringLength(500)]
    public string? Description { get; set; }
}
