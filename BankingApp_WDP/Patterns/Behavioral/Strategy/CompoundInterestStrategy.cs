using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.Strategy;

// A = P * ((1 + r/365)^(365/12) - 1)  — daily compounding over one month
public class CompoundInterestStrategy : IInterestCalculationStrategy
{
    public decimal Calculate(Account account, decimal balance)
    {
        var rate = account is SavingsAccount sa ? sa.InterestRate : 0m;
        if (rate == 0m) return 0m;

        var dailyRate = (double)rate / 365.0;
        var compoundFactor = (decimal)(Math.Pow(1.0 + dailyRate, 365.0 / 12.0) - 1.0);
        return Math.Round(balance * compoundFactor, 2);
    }
}
