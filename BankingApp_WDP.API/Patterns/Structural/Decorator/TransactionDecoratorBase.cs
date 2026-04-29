using BankingApp.Models;

namespace BankingApp.Patterns.Structural.Decorator;

/// <summary>
/// Clasa abstractă de bază pentru toți decoratorii.
/// Stochează referința la procesorul interior și deleghează apelul implicit.
/// </summary>
public abstract class TransactionDecoratorBase : ITransactionProcessor
{
    private readonly ITransactionProcessor _inner;

    protected TransactionDecoratorBase(ITransactionProcessor inner)
    {
        _inner = inner;
    }

    public virtual Task<Transaction> ProcessAsync(Transaction transaction)
        => _inner.ProcessAsync(transaction);
}
