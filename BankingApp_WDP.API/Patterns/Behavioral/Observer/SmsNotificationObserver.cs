using BankingApp.Data;
using BankingApp.Patterns.Creational.AbstractFactory;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Behavioral.Observer;

/// <summary>
/// Sends an SMS alert for large transactions (amount > 1000 MDL).
/// Uses AbstractFactory Pattern #3 for the SMS implementation.
/// </summary>
public class SmsNotificationObserver : ITransactionObserver
{
    private const decimal LargeTransactionThreshold = 1000m;

    private readonly INotificationFactory _notificationFactory;
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    public SmsNotificationObserver(INotificationFactory notificationFactory, AppDbContext db, ILoggerService logger)
    {
        _notificationFactory = notificationFactory;
        _db = db;
        _logger = logger;
    }

    public async Task OnTransactionCompletedAsync(TransactionEvent evt)
    {
        var tx = evt.Transaction;

        if (tx.Amount < LargeTransactionThreshold)
            return;

        if (string.IsNullOrEmpty(evt.InitiatorUserId))
            return;

        var user = await _db.Users.FindAsync(evt.InitiatorUserId);
        if (user?.PhoneNumber == null)
            return;

        var sms = _notificationFactory.CreateSmsNotification();
        var message = $"BankingApp: Tranzacție {tx.Type} {tx.Amount:N2} {tx.Currency} procesată (#{tx.Id}). " +
                      $"Dacă nu ești tu, sună imediat la suport.";

        await sms.SendAsync(user.PhoneNumber, message);
        _logger.LogInformation($"[Observer][SMS] SMS trimis la {user.PhoneNumber} pentru tranzacția #{tx.Id} ({tx.Amount:N2} {tx.Currency}).");
    }
}
