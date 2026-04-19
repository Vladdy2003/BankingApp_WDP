using BankingApp.Models;

namespace BankingApp.Patterns.Creational.Prototype;

public interface ICardFactory
{
    Card CreateCard(int accountId, CardType cardType, decimal? dailyLimit = null, decimal? monthlyLimit = null);
}

public class CardFactory : ICardFactory
{
    private readonly Dictionary<CardType, CardTemplate> _templates;

    public CardFactory()
    {
        _templates = new Dictionary<CardType, CardTemplate>
        {
            [CardType.Debit]   = new StandardDebitTemplate(),
            [CardType.Credit]  = new PremiumCreditTemplate(),
            [CardType.Prepaid] = new BusinessDebitTemplate()
        };
    }

    public Card CreateCard(int accountId, CardType cardType, decimal? dailyLimit = null, decimal? monthlyLimit = null)
    {
        if (!_templates.TryGetValue(cardType, out var template))
            throw new ArgumentException($"No template found for card type: {cardType}");

        var clone = template.CloneTemplate();

        return new Card
        {
            AccountId    = accountId,
            CardNumber   = GenerateCardNumber(),
            CVVHash      = GenerateCvvHash(),
            ExpiryDate   = DateTime.UtcNow.AddYears(3),
            Type         = clone.CardType,
            Status       = CardStatus.Pending,
            DailyLimit   = dailyLimit ?? clone.DailyLimit,
            MonthlyLimit = monthlyLimit ?? clone.MonthlyLimit,
            CreatedAt    = DateTime.UtcNow
        };
    }

    // Luhn-valid 16-digit card number formatted as XXXX XXXX XXXX XXXX
    private static string GenerateCardNumber()
    {
        var rng = Random.Shared;
        var digits = new int[15];
        for (int i = 0; i < 15; i++)
            digits[i] = rng.Next(0, 10);

        // Luhn check digit
        int sum = 0;
        for (int i = 14; i >= 0; i--)
        {
            int d = digits[i];
            if ((14 - i) % 2 == 0) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
        }
        int checkDigit = (10 - (sum % 10)) % 10;

        var all = digits.Append(checkDigit).ToArray();
        return string.Join(" ", Enumerable.Range(0, 4).Select(g => string.Concat(all.Skip(g * 4).Take(4))));
    }

    // 3-digit CVV hashed with SHA256
    private static string GenerateCvvHash()
    {
        var cvv = Random.Shared.Next(100, 1000).ToString();
        var bytes = System.Security.Cryptography.SHA256.HashData(System.Text.Encoding.UTF8.GetBytes(cvv));
        return Convert.ToHexString(bytes);
    }
}
