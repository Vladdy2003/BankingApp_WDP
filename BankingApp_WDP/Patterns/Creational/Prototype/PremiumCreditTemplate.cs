using BankingApp.Models;

namespace BankingApp.Patterns.Creational.Prototype;

public class PremiumCreditTemplate : CardTemplate
{
    public PremiumCreditTemplate()
    {
        CardType = CardType.Credit;
        DailyLimit = 5000m;
        MonthlyLimit = 50000m;
        Benefits = new List<string>
        {
            "Cashback 2% la cumpărături",
            "Asigurare de călătorie inclusă",
            "Acces lounge aeroporturi",
            "Retrageri gratuite internaționale",
            "Protecție la fraudă extinsă"
        };
        AllowedTransactionTypes = new List<string>
        {
            "Deposit", "Withdrawal", "Payment", "Transfer"
        };
    }
}
