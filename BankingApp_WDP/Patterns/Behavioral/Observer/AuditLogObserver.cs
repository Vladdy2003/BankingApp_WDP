using System.Text.Json;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Services;

namespace BankingApp.Patterns.Behavioral.Observer;

/// <summary>
/// Records every completed transaction in the AuditLog table via IAuditService.
/// Provides full traceability of all financial operations.
/// </summary>
public class AuditLogObserver : ITransactionObserver
{
    private readonly IAuditService _auditService;
    private readonly ILoggerService _logger;

    public AuditLogObserver(IAuditService auditService, ILoggerService logger)
    {
        _auditService = auditService;
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

        await _auditService.LogAsync(
            action: $"Transaction.{tx.Type}",
            userId: evt.InitiatorUserId,
            entityType: nameof(Transaction),
            entityId: tx.Id.ToString(),
            newValues: newValues);

        _logger.LogInformation($"[Observer][Audit] Înregistrat în audit log: Transaction.{tx.Type} pentru tranzacția #{tx.Id}.");
    }
}
