using System.Text.Json;
using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Behavioral.Observer;

/// <summary>
/// Records every completed transaction in the AuditLog table.
/// Provides full traceability of all financial operations.
/// </summary>
public class AuditLogObserver : ITransactionObserver
{
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    public AuditLogObserver(AppDbContext db, ILoggerService logger)
    {
        _db = db;
        _logger = logger;
    }

    public async Task OnTransactionCompletedAsync(TransactionEvent evt)
    {
        var tx = evt.Transaction;

        var newValues = JsonSerializer.Serialize(new
        {
            tx.Id,
            tx.Type,
            tx.Status,
            tx.Amount,
            tx.Currency,
            tx.FromAccountId,
            tx.ToAccountId,
            tx.ProcessedAt
        });

        var auditEntry = new AuditLog
        {
            UserId = evt.InitiatorUserId,
            Action = $"Transaction.{tx.Type}",
            EntityType = nameof(Transaction),
            EntityId = tx.Id.ToString(),
            NewValues = newValues,
            Timestamp = evt.OccurredAt
        };

        _db.AuditLogs.Add(auditEntry);
        await _db.SaveChangesAsync();

        _logger.LogInformation($"[Observer][Audit] Înregistrat în audit log: {auditEntry.Action} pentru tranzacția #{tx.Id}.");
    }
}
