using System.ComponentModel.DataAnnotations;

namespace BankingApp.DTOs.Cards;

public class UpdateCardLimitsRequest
{
    [Range(0.01, double.MaxValue, ErrorMessage = "DailyLimit trebuie să fie pozitiv.")]
    public decimal? DailyLimit { get; set; }

    [Range(0.01, double.MaxValue, ErrorMessage = "MonthlyLimit trebuie să fie pozitiv.")]
    public decimal? MonthlyLimit { get; set; }
}
