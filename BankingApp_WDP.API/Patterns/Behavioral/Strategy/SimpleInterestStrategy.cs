using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.Strategy;

// I = P * r * t  (t = 1/12 for one month)
public class SimpleInterestStrategy : IInterestCalculationStrategy
{
    public decimal Calculate(Account account, decimal balance)
    {
        var rate = account is SavingsAccount sa ? sa.InterestRate : 0m;
        return Math.Round(balance * rate / 12m, 2);
    }
}
