using BankingApp.Models;

namespace BankingApp.Patterns.Creational.Prototype;

public class BusinessDebitTemplate : CardTemplate
{
    public BusinessDebitTemplate()
    {
        CardType = CardType.Debit;
        DailyLimit = 10000m;
        MonthlyLimit = 100000m;
        Benefits = new List<string>
        {
            "Tranzacții business fără comision",
            "Rapoarte lunare detaliate",
            "Suport prioritar 24/7",
            "Limite extinse pentru plăți corporative"
        };
        AllowedTransactionTypes = new List<string>
        {
            "Deposit", "Withdrawal", "Payment", "Transfer"
        };
    }
}
