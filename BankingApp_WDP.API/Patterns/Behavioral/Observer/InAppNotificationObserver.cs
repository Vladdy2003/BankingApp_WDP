using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Behavioral.Observer;

/// <summary>
/// Stores an in-app notification in the database for every completed transaction.
/// The notification can be retrieved later via the NotificationsController (step 5.3).
/// </summary>
public class InAppNotificationObserver : ITransactionObserver
{
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    public InAppNotificationObserver(AppDbContext db, ILoggerService logger)
    {
        _db = db;
        _logger = logger;
    }

    public async Task OnTransactionCompletedAsync(TransactionEvent evt)
    {
        var tx = evt.Transaction;

        if (string.IsNullOrEmpty(evt.InitiatorUserId))
            return;

        var notification = new Notification
        {
            UserId = evt.InitiatorUserId,
            Title = $"Tranzacție {tx.Type} procesată",
            Message = $"Tranzacția #{tx.Id} de tip {tx.Type} în valoare de {tx.Amount:N2} {tx.Currency} a fost procesată cu succes.",
            Type = NotificationType.Transaction,
            IsRead = false,
            CreatedAt = DateTime.UtcNow
        };

        _db.Notifications.Add(notification);
        await _db.SaveChangesAsync();

        _logger.LogInformation($"[Observer][InApp] Notificare stocată în DB pentru utilizatorul {evt.InitiatorUserId}, tranzacția #{tx.Id}.");
    }
}
