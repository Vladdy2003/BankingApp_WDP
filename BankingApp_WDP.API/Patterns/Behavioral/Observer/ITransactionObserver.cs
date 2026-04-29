namespace BankingApp.Patterns.Behavioral.Observer;

public interface ITransactionObserver
{
    Task OnTransactionCompletedAsync(TransactionEvent transactionEvent);
}
