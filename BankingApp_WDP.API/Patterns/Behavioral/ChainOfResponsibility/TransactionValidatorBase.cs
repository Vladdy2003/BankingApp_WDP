using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.ChainOfResponsibility;

public abstract class TransactionValidatorBase : ITransactionValidator
{
    private ITransactionValidator? _next;

    public ITransactionValidator SetNext(ITransactionValidator next)
    {
        _next = next;
        return next;
    }

    public abstract void Validate(Transaction tx, Account account);

    protected void PassToNext(Transaction tx, Account account)
    {
        _next?.Validate(tx, account);
    }
}
