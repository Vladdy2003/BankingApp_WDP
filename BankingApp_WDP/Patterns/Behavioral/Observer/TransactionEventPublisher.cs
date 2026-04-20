using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Behavioral.Observer;

/// <summary>
/// Observer Pattern #11 — Subject/Observable. Manages a list of subscribers and
/// notifies all of them when a transaction event occurs.
/// </summary>
public class TransactionEventPublisher : ITransactionObservable
{
    private readonly List<ITransactionObserver> _observers;
    private readonly ILoggerService _logger;

    public TransactionEventPublisher(IEnumerable<ITransactionObserver> observers, ILoggerService logger)
    {
        _observers = observers.ToList();
        _logger = logger;
    }

    public void Subscribe(ITransactionObserver observer)
    {
        if (!_observers.Contains(observer))
            _observers.Add(observer);
    }

    public void Unsubscribe(ITransactionObserver observer)
    {
        _observers.Remove(observer);
    }

    public async Task NotifyObserversAsync(TransactionEvent transactionEvent)
    {
        _logger.LogInformation($"[Observer] Notifying {_observers.Count} observers — Transaction #{transactionEvent.Transaction.Id} ({transactionEvent.Transaction.Type})");

        foreach (var observer in _observers)
        {
            try
            {
                await observer.OnTransactionCompletedAsync(transactionEvent);
            }
            catch (Exception ex)
            {
                _logger.LogError($"[Observer] Observer {observer.GetType().Name} threw an exception.", ex);
            }
        }
    }
}
