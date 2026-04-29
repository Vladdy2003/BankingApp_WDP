using BankingApp.Models;
using BankingApp.Utilities;

namespace BankingApp.Patterns.Creational.Prototype;

public interface ICardFactory
{
    Card CreateCard(int accountId, CardType cardType, decimal? dailyLimit = null, decimal? monthlyLimit = null);
}

public class CardFactory : ICardFactory
{
    private readonly Dictionary<CardType, CardTemplate> _templates;
    private readonly ICardNumberGenerator _cardNumberGenerator;
    private readonly ICVVGenerator _cvvGenerator;

    public CardFactory(ICardNumberGenerator cardNumberGenerator, ICVVGenerator cvvGenerator)
    {
        _cardNumberGenerator = cardNumberGenerator;
        _cvvGenerator = cvvGenerator;

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
            CardNumber   = _cardNumberGenerator.Generate(),
            CVVHash      = _cvvGenerator.GenerateHash(),
            ExpiryDate   = DateTime.UtcNow.AddYears(3),
            Type         = clone.CardType,
            Status       = CardStatus.Pending,
            DailyLimit   = dailyLimit ?? clone.DailyLimit,
            MonthlyLimit = monthlyLimit ?? clone.MonthlyLimit,
            CreatedAt    = DateTime.UtcNow
        };
    }
}
