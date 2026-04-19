using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.AbstractFactory;
using BankingApp.Patterns.Creational.Singleton;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Patterns.Structural.Decorator;

/// <summary>
/// Decorator #3 — trimite o notificare email utilizatorului după procesarea reușită a tranzacției,
/// prin INotificationFactory (Abstract Factory #3).
/// </summary>
public class NotificationTransactionDecorator : TransactionDecoratorBase
{
    private readonly INotificationFactory _notificationFactory;
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    public NotificationTransactionDecorator(
        ITransactionProcessor inner,
        INotificationFactory notificationFactory,
        AppDbContext db,
        ILoggerService logger)
        : base(inner)
    {
        _notificationFactory = notificationFactory;
        _db = db;
        _logger = logger;
    }

    public override async Task<Transaction> ProcessAsync(Transaction transaction)
    {
        var result = await base.ProcessAsync(transaction);

        if (result.Status == TransactionStatus.Completed)
        {
            var accountId = result.ToAccountId ?? result.FromAccountId;
            if (accountId.HasValue)
            {
                var account = await _db.Accounts
                    .Include(a => a.User)
                    .FirstOrDefaultAsync(a => a.Id == accountId.Value);

                if (account?.User?.Email != null)
                {
                    var email = _notificationFactory.CreateEmailNotification();
                    await email.SendAsync(
                        account.User.Email,
                        $"Tranzacție {result.Type} procesată cu succes",
                        $"Tranzacția de {result.Amount} {result.Currency} ({result.Type}) " +
                        $"a fost procesată cu succes la {result.ProcessedAt:dd.MM.yyyy HH:mm} UTC.");

                    _logger.LogInformation(
                        $"[Decorator][Notification] Email trimis pentru tranzacția #{result.Id} la {account.User.Email}");
                }
            }
        }

        return result;
    }
}
