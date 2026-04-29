using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.ChainOfResponsibility;

public interface ITransactionValidator
{
    ITransactionValidator SetNext(ITransactionValidator next);
    void Validate(Transaction tx, Account account);
}
