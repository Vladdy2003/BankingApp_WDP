using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Structural.Decorator;

/// <summary>
/// Decorator #3 — loghează procesarea tranzacției pentru trasabilitate.
/// Trimiterea email-ului este responsabilitatea EmailNotificationObserver (Observer Pattern #11),
/// pentru a evita duplicarea notificărilor.
/// </summary>
public class NotificationTransactionDecorator : TransactionDecoratorBase
{
    private readonly ILoggerService _logger;

    public NotificationTransactionDecorator(
        ITransactionProcessor inner,
        ILoggerService logger)
        : base(inner)
    {
        _logger = logger;
    }

    public override async Task<Transaction> ProcessAsync(Transaction transaction)
    {
        var result = await base.ProcessAsync(transaction);

        if (result.Status == TransactionStatus.Completed)
            _logger.LogInformation(
                $"[Decorator][Notification] Tranzacția #{result.Id} ({result.Type}, {result.Amount:N2} {result.Currency}) procesată — notificările sunt gestionate de Observer.");

        return result;
    }
}
