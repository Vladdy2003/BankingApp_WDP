using BankingApp.Models;
using BankingApp.Patterns.Creational.AbstractFactory;
using BankingApp.Patterns.Creational.Singleton;
using Microsoft.EntityFrameworkCore;
using BankingApp.Data;

namespace BankingApp.Patterns.Behavioral.Observer;

/// <summary>
/// Sends an email alert to the account owner when a transaction is completed.
/// Uses AbstractFactory Pattern #3 to obtain the correct email implementation.
/// </summary>
public class EmailNotificationObserver : ITransactionObserver
{
    private readonly INotificationFactory _notificationFactory;
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    public EmailNotificationObserver(INotificationFactory notificationFactory, AppDbContext db, ILoggerService logger)
    {
        _notificationFactory = notificationFactory;
        _db = db;
        _logger = logger;
    }

    public async Task OnTransactionCompletedAsync(TransactionEvent evt)
    {
        var tx = evt.Transaction;

        if (string.IsNullOrEmpty(evt.InitiatorUserId))
            return;

        var user = await _db.Users.FindAsync(evt.InitiatorUserId);
        if (user?.Email == null)
            return;

        var email = _notificationFactory.CreateEmailNotification();
        var subject = $"Tranzacție {tx.Type} procesată — {tx.Amount:N2} {tx.Currency}";
        var body = $"Bună, {user.FirstName}!\n\n" +
                   $"Tranzacția #{tx.Id} de tip {tx.Type} în valoare de {tx.Amount:N2} {tx.Currency} " +
                   $"a fost procesată cu succes la {tx.ProcessedAt:yyyy-MM-dd HH:mm} UTC.\n\n" +
                   $"Dacă nu ai autorizat această operațiune, contactează-ne imediat.";

        await email.SendAsync(user.Email, subject, body);
        _logger.LogInformation($"[Observer][Email] Email trimis utilizatorului {user.Email} pentru tranzacția #{tx.Id}.");
    }
}
