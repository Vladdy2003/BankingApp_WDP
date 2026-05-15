using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.ChainOfResponsibility;
using BankingApp.Patterns.Creational.Builder;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Patterns.Structural.Decorator;

namespace BankingApp.Patterns.Behavioral.Command;

public class TransferCommand : ITransactionCommand
{
    private readonly AppDbContext _db;
    private readonly ITransactionValidator _validator;
    private readonly ITransactionProcessor _processor;
    private readonly ILoggerService _logger;

    private readonly int _fromAccountId;
    private readonly int _toAccountId;
    private readonly decimal _amount;
    private readonly string _currency;
    private readonly string? _description;

    private int _executedTransactionId;

    public TransferCommand(
        AppDbContext db,
        ITransactionValidator validator,
        ITransactionProcessor processor,
        ILoggerService logger,
        int fromAccountId,
        int toAccountId,
        decimal amount,
        string currency = "MDL",
        string? description = null)
    {
        _db = db;
        _validator = validator;
        _processor = processor;
        _logger = logger;
        _fromAccountId = fromAccountId;
        _toAccountId = toAccountId;
        _amount = amount;
        _currency = currency;
        _description = description;
    }

    public async Task ExecuteAsync()
    {
        var fromAccount = await _db.Accounts.FindAsync(_fromAccountId)
            ?? throw new InvalidOperationException($"Contul sursă {_fromAccountId} nu a fost găsit.");

        var toAccount = await _db.Accounts.FindAsync(_toAccountId)
            ?? throw new InvalidOperationException($"Contul destinație {_toAccountId} nu a fost găsit.");

        var tx = new TransactionBuilder()
            .WithAmount(_amount)
            .WithType(TransactionType.Transfer)
            .WithSourceAccount(_fromAccountId)
            .WithDestinationAccount(_toAccountId)
            .WithCurrency(_currency)
            .WithDescription(_description ?? "Transfer")
            .Build(); // Status = Pending

        // validarea se execută pe contul sursă (verificare sold, limită zilnică, fraud)
        _validator.Validate(tx, fromAccount);

        // Salvăm tranzacția ca Pending, astfel procesorul o poate găsi prin FindAsync
        _db.Transactions.Add(tx);
        await _db.SaveChangesAsync();

        _executedTransactionId = tx.Id;

        // Delegăm la lanțul Decorator → BasicTransactionProcessor care:
        // actualizează soldurile, setează Completed, și declanșează Observer (AuditLog, Notificări)
        await _processor.ProcessAsync(tx);

        _logger.LogInformation($"[TransferCommand] Transfer {_amount} {_currency}: cont {_fromAccountId} → cont {_toAccountId}. TxId={_executedTransactionId}");
    }

    public async Task UndoAsync()
    {
        if (_executedTransactionId == 0)
            throw new InvalidOperationException("TransferCommand nu a fost executat — nimic de anulat.");

        var tx = await _db.Transactions.FindAsync(_executedTransactionId)
            ?? throw new InvalidOperationException($"Tranzacția {_executedTransactionId} nu a fost găsită.");

        var fromAccount = await _db.Accounts.FindAsync(_fromAccountId)
            ?? throw new InvalidOperationException($"Contul sursă {_fromAccountId} nu a fost găsit.");

        var toAccount = await _db.Accounts.FindAsync(_toAccountId)
            ?? throw new InvalidOperationException($"Contul destinație {_toAccountId} nu a fost găsit.");

        fromAccount.Balance += _amount;
        toAccount.Balance -= _amount;
        tx.Status = TransactionStatus.Cancelled;

        await _db.SaveChangesAsync();
        _logger.LogInformation($"[TransferCommand] Undo transfer {_amount} {_currency}: cont {_toAccountId} → cont {_fromAccountId}. TxId={_executedTransactionId}");
    }
}
