using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.Observer;

namespace BankingApp.Patterns.Structural.Decorator;

/// <summary>
/// Componenta concretă — execută efectiv tranzacția: actualizează soldurile și marchează statusul Completed.
/// Toți decoratorii delegă în final la această clasă. După procesare, publică evenimentul
/// către observatorii înregistrați (Observer Pattern #11).
/// </summary>
public class BasicTransactionProcessor : ITransactionProcessor
{
    private readonly AppDbContext _db;
    private readonly ITransactionObservable _publisher;

    public BasicTransactionProcessor(AppDbContext db, ITransactionObservable publisher)
    {
        _db = db;
        _publisher = publisher;
    }

    public async Task<Transaction> ProcessAsync(Transaction transaction)
    {
        var tx = await _db.Transactions.FindAsync(transaction.Id)
            ?? throw new InvalidOperationException($"Tranzacția #{transaction.Id} nu există în baza de date.");

        string? initiatorUserId = null;

        switch (tx.Type)
        {
            case TransactionType.Deposit:
            {
                var toAcc = await _db.Accounts.FindAsync(tx.ToAccountId)
                    ?? throw new InvalidOperationException($"Contul destinație #{tx.ToAccountId} nu există.");
                toAcc.Balance += tx.Amount;
                initiatorUserId = toAcc.UserId;
                break;
            }
            case TransactionType.Withdrawal:
            {
                var fromAcc = await _db.Accounts.FindAsync(tx.FromAccountId)
                    ?? throw new InvalidOperationException($"Contul sursă #{tx.FromAccountId} nu există.");
                fromAcc.Balance -= tx.Amount;
                initiatorUserId = fromAcc.UserId;
                break;
            }
            case TransactionType.Transfer:
            case TransactionType.Payment:
            {
                var fromAcc = await _db.Accounts.FindAsync(tx.FromAccountId)
                    ?? throw new InvalidOperationException($"Contul sursă #{tx.FromAccountId} nu există.");
                var toAcc = await _db.Accounts.FindAsync(tx.ToAccountId)
                    ?? throw new InvalidOperationException($"Contul destinație #{tx.ToAccountId} nu există.");
                fromAcc.Balance -= tx.Amount;
                toAcc.Balance += tx.Amount;
                initiatorUserId = fromAcc.UserId;
                break;
            }
        }

        tx.Status = TransactionStatus.Completed;
        tx.ProcessedAt = DateTime.UtcNow;
        await _db.SaveChangesAsync();

        await _publisher.NotifyObserversAsync(new TransactionEvent
        {
            Transaction = tx,
            InitiatorUserId = initiatorUserId
        });

        return tx;
    }
}
