using BankingApp.Data;
using BankingApp.Models;

namespace BankingApp.Patterns.Structural.Decorator;

/// <summary>
/// Componenta concretă — execută efectiv tranzacția: actualizează soldurile și marchează statusul Completed.
/// Toți decoratorii delegă în final la această clasă.
/// </summary>
public class BasicTransactionProcessor : ITransactionProcessor
{
    private readonly AppDbContext _db;

    public BasicTransactionProcessor(AppDbContext db)
    {
        _db = db;
    }

    public async Task<Transaction> ProcessAsync(Transaction transaction)
    {
        var tx = await _db.Transactions.FindAsync(transaction.Id)
            ?? throw new InvalidOperationException($"Tranzacția #{transaction.Id} nu există în baza de date.");

        switch (tx.Type)
        {
            case TransactionType.Deposit:
            {
                var toAcc = await _db.Accounts.FindAsync(tx.ToAccountId)
                    ?? throw new InvalidOperationException($"Contul destinație #{tx.ToAccountId} nu există.");
                toAcc.Balance += tx.Amount;
                break;
            }
            case TransactionType.Withdrawal:
            {
                var fromAcc = await _db.Accounts.FindAsync(tx.FromAccountId)
                    ?? throw new InvalidOperationException($"Contul sursă #{tx.FromAccountId} nu există.");
                fromAcc.Balance -= tx.Amount;
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
                break;
            }
        }

        tx.Status = TransactionStatus.Completed;
        tx.ProcessedAt = DateTime.UtcNow;
        await _db.SaveChangesAsync();

        return tx;
    }
}
