using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.ChainOfResponsibility;
using BankingApp.Patterns.Creational.Builder;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Patterns.Structural.Decorator;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Patterns.Behavioral.Command;

public class DepositCommand : ITransactionCommand
{
    private readonly AppDbContext _db;
    private readonly ITransactionValidator _validator;
    private readonly ITransactionProcessor _processor;
    private readonly ILoggerService _logger;

    private readonly int _toAccountId;
    private readonly decimal _amount;
    private readonly string _currency;
    private readonly string? _description;

    private int _executedTransactionId;

    public DepositCommand(
        AppDbContext db,
        ITransactionValidator validator,
        ITransactionProcessor processor,
        ILoggerService logger,
        int toAccountId,
        decimal amount,
        string currency = "MDL",
        string? description = null)
    {
        _db = db;
        _validator = validator;
        _processor = processor;
        _logger = logger;
        _toAccountId = toAccountId;
        _amount = amount;
        _currency = currency;
        _description = description;
    }

    public async Task ExecuteAsync()
    {
        var account = await _db.Accounts.FindAsync(_toAccountId)
            ?? throw new InvalidOperationException($"Contul {_toAccountId} nu a fost găsit.");

        var tx = new TransactionBuilder()
            .WithAmount(_amount)
            .WithType(TransactionType.Deposit)
            .WithDestinationAccount(_toAccountId)
            .WithCurrency(_currency)
            .WithDescription(_description ?? "Depozit")
            .Build(); // Status = Pending

        _validator.Validate(tx, account);

        // Salvăm tranzacția ca Pending, astfel procesorul o poate găsi prin FindAsync
        _db.Transactions.Add(tx);
        await _db.SaveChangesAsync();

        _executedTransactionId = tx.Id;

        // Delegăm la lanțul Decorator → BasicTransactionProcessor care:
        // actualizează soldul, setează Completed, și declanșează Observer (AuditLog, Notificări)
        await _processor.ProcessAsync(tx);

        _logger.LogInformation($"[DepositCommand] Depozit {_amount} {_currency} → cont {_toAccountId}. TxId={_executedTransactionId}");
    }

    public async Task UndoAsync()
    {
        if (_executedTransactionId == 0)
            throw new InvalidOperationException("DepositCommand nu a fost executat — nimic de anulat.");

        var tx = await _db.Transactions.FindAsync(_executedTransactionId)
            ?? throw new InvalidOperationException($"Tranzacția {_executedTransactionId} nu a fost găsită.");

        var account = await _db.Accounts.FindAsync(_toAccountId)
            ?? throw new InvalidOperationException($"Contul {_toAccountId} nu a fost găsit.");

        account.Balance -= _amount;
        tx.Status = TransactionStatus.Cancelled;

        await _db.SaveChangesAsync();
        _logger.LogInformation($"[DepositCommand] Undo depozit {_amount} {_currency} din contul {_toAccountId}. TxId={_executedTransactionId}");
    }
}
