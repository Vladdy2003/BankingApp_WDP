using BankingApp.Models;

namespace BankingApp.DTOs.Cards;

public class CardResponse
{
    public int Id { get; set; }
    public int AccountId { get; set; }
    public string MaskedCardNumber { get; set; } = string.Empty;
    public string ExpiryDate { get; set; } = string.Empty;
    public string Type { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public decimal DailyLimit { get; set; }
    public decimal MonthlyLimit { get; set; }
    public DateTime CreatedAt { get; set; }

    public static CardResponse FromCard(Card card) => new()
    {
        Id = card.Id,
        AccountId = card.AccountId,
        MaskedCardNumber = MaskCardNumber(card.CardNumber),
        ExpiryDate = card.ExpiryDate.ToString("MM/yy"),
        Type = card.Type.ToString(),
        Status = card.Status.ToString(),
        DailyLimit = card.DailyLimit,
        MonthlyLimit = card.MonthlyLimit,
        CreatedAt = card.CreatedAt
    };

    private static string MaskCardNumber(string cardNumber)
    {
        var digits = cardNumber.Replace(" ", "");
        if (digits.Length < 4) return cardNumber;
        return "**** **** **** " + digits[^4..];
    }
}
