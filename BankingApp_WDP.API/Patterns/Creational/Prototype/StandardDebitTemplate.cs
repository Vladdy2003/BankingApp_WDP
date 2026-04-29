using BankingApp.Models;

namespace BankingApp.Patterns.Creational.Prototype;

public class StandardDebitTemplate : CardTemplate
{
    public StandardDebitTemplate()
    {
        CardType = CardType.Debit;
        DailyLimit = 1000m;
        MonthlyLimit = 10000m;
        Benefits = new List<string>
        {
            "Retrageri gratuite la ATM-urile băncii",
            "Plăți contactless",
            "Notificări tranzacții"
        };
        AllowedTransactionTypes = new List<string>
        {
            "Deposit", "Withdrawal", "Payment", "Transfer"
        };
    }
}
