using System.ComponentModel.DataAnnotations;
using BankingApp.Models;

namespace BankingApp.DTOs.Cards;

public class CreateCardRequest
{
    [Required]
    public int AccountId { get; set; }

    [Required]
    public CardType Type { get; set; }

    [Range(0.01, double.MaxValue, ErrorMessage = "DailyLimit trebuie să fie pozitiv.")]
    public decimal? DailyLimit { get; set; }

    [Range(0.01, double.MaxValue, ErrorMessage = "MonthlyLimit trebuie să fie pozitiv.")]
    public decimal? MonthlyLimit { get; set; }
}
