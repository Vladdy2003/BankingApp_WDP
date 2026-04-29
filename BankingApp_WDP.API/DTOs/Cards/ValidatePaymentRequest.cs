using System.ComponentModel.DataAnnotations;

namespace BankingApp.DTOs.Cards;

public class ValidatePaymentRequest
{
    [Required]
    [Range(0.01, double.MaxValue, ErrorMessage = "Suma trebuie să fie pozitivă.")]
    public decimal Amount { get; set; }

    [MaxLength(3)]
    public string Currency { get; set; } = "MDL";
}
