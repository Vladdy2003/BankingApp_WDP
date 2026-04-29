using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.Strategy;

public class NoInterestStrategy : IInterestCalculationStrategy
{
    public decimal Calculate(Account account, decimal balance) => 0m;
}
