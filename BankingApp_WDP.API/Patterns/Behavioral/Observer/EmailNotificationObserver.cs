using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.AbstractFactory;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Services;

namespace BankingApp.Patterns.Behavioral.Observer;

/// <summary>
/// Sends an HTML email alert to the account owner when a transaction is completed.
/// Uses AbstractFactory Pattern #3 for the email implementation and IEmailTemplateService for the HTML body.
/// </summary>
public class EmailNotificationObserver : ITransactionObserver
{
    private readonly INotificationFactory _notificationFactory;
    private readonly IEmailTemplateService _emailTemplates;
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    public EmailNotificationObserver(
        INotificationFactory notificationFactory,
        IEmailTemplateService emailTemplates,
        AppDbContext db,
        ILoggerService logger)
    {
        _notificationFactory = notificationFactory;
        _emailTemplates = emailTemplates;
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
        var htmlBody = _emailTemplates.TransactionEmail(
            user.FirstName,
            tx.Type.ToString(),
            tx.Amount,
            tx.Currency,
            tx.Id,
            tx.ProcessedAt);

        await email.SendAsync(user.Email, subject, htmlBody);
        _logger.LogInformation($"[Observer][Email] Email HTML trimis la {user.Email} pentru tranzacția #{tx.Id}.");
    }
}
