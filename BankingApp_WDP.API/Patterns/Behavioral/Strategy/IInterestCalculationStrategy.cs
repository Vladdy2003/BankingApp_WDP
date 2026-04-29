using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.Strategy;

public interface IInterestCalculationStrategy
{
    decimal Calculate(Account account, decimal balance);
}
