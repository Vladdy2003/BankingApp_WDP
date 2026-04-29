namespace BankingApp.Patterns.Behavioral.Observer;

public interface ITransactionObservable
{
    void Subscribe(ITransactionObserver observer);
    void Unsubscribe(ITransactionObserver observer);
    Task NotifyObserversAsync(TransactionEvent transactionEvent);
}
