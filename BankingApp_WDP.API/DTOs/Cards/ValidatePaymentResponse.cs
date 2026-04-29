namespace BankingApp.DTOs.Cards;

public class ValidatePaymentResponse
{
    public bool IsAllowed { get; set; }
    public string? Reason { get; set; }
    public int CardId { get; set; }
    public decimal Amount { get; set; }
    public string Currency { get; set; } = string.Empty;
    public decimal AvailableBalance { get; set; }
    public decimal RemainingDailyLimit { get; set; }
    public decimal RemainingMonthlyLimit { get; set; }
}
