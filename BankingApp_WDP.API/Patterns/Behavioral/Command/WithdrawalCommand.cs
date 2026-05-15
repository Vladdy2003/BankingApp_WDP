using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.ChainOfResponsibility;
using BankingApp.Patterns.Creational.Builder;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Patterns.Structural.Decorator;

namespace BankingApp.Patterns.Behavioral.Command;

public class WithdrawalCommand : ITransactionCommand
{
    private readonly AppDbContext _db;
    private readonly ITransactionValidator _validator;
    private readonly ITransactionProcessor _processor;
    private readonly ILoggerService _logger;

    private readonly int _fromAccountId;
    private readonly decimal _amount;
    private readonly string _currency;
    private readonly string? _description;

    private int _executedTransactionId;

    public WithdrawalCommand(
        AppDbContext db,
        ITransactionValidator validator,
        ITransactionProcessor processor,
        ILoggerService logger,
        int fromAccountId,
        decimal amount,
        string currency = "MDL",
        string? description = null)
    {
        _db = db;
        _validator = validator;
        _processor = processor;
        _logger = logger;
        _fromAccountId = fromAccountId;
        _amount = amount;
        _currency = currency;
        _description = description;
    }

    public async Task ExecuteAsync()
    {
        var account = await _db.Accounts.FindAsync(_fromAccountId)
            ?? throw new InvalidOperationException($"Contul {_fromAccountId} nu a fost găsit.");

        var tx = new TransactionBuilder()
            .WithAmount(_amount)
            .WithType(TransactionType.Withdrawal)
            .WithSourceAccount(_fromAccountId)
            .WithCurrency(_currency)
            .WithDescription(_description ?? "Retragere")
            .Build(); // Status = Pending

        _validator.Validate(tx, account);

        // Salvăm tranzacția ca Pending, astfel procesorul o poate găsi prin FindAsync
        _db.Transactions.Add(tx);
        await _db.SaveChangesAsync();

        _executedTransactionId = tx.Id;

        // Delegăm la lanțul Decorator → BasicTransactionProcessor care:
        // actualizează soldul, setează Completed, și declanșează Observer (AuditLog, Notificări)
        await _processor.ProcessAsync(tx);

        _logger.LogInformation($"[WithdrawalCommand] Retragere {_amount} {_currency} din cont {_fromAccountId}. TxId={_executedTransactionId}");
    }

    public async Task UndoAsync()
    {
        if (_executedTransactionId == 0)
            throw new InvalidOperationException("WithdrawalCommand nu a fost executat — nimic de anulat.");

        var tx = await _db.Transactions.FindAsync(_executedTransactionId)
            ?? throw new InvalidOperationException($"Tranzacția {_executedTransactionId} nu a fost găsită.");

        var account = await _db.Accounts.FindAsync(_fromAccountId)
            ?? throw new InvalidOperationException($"Contul {_fromAccountId} nu a fost găsit.");

        account.Balance += _amount;
        tx.Status = TransactionStatus.Cancelled;

        await _db.SaveChangesAsync();
        _logger.LogInformation($"[WithdrawalCommand] Undo retragere {_amount} {_currency} în contul {_fromAccountId}. TxId={_executedTransactionId}");
    }
}
