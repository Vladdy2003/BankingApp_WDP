using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Structural.Decorator;

/// <summary>
/// Decorator #2 — aplică un comision automat pentru Transfer și Withdrawal (0,5%, min 1 MDL, max 50 MDL)
/// înainte de a delega procesarea la decoratorul/procesorul interior.
/// </summary>
public class FeeTransactionDecorator : TransactionDecoratorBase
{
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    private const decimal FeeRate = 0.005m;
    private const decimal MinFee = 1m;
    private const decimal MaxFee = 50m;

    public FeeTransactionDecorator(ITransactionProcessor inner, AppDbContext db, ILoggerService logger)
        : base(inner)
    {
        _db = db;
        _logger = logger;
    }

    public override async Task<Transaction> ProcessAsync(Transaction transaction)
    {
        if ((transaction.Type == TransactionType.Transfer || transaction.Type == TransactionType.Withdrawal)
            && transaction.FromAccountId.HasValue)
        {
            var fee = Math.Clamp(transaction.Amount * FeeRate, MinFee, MaxFee);
            var fromAccount = await _db.Accounts.FindAsync(transaction.FromAccountId.Value);

            if (fromAccount != null)
            {
                fromAccount.Balance -= fee;

                var feeTx = new Transaction
                {
                    FromAccountId = transaction.FromAccountId,
                    Amount = fee,
                    Currency = transaction.Currency,
                    Type = TransactionType.Payment,
                    Status = TransactionStatus.Completed,
                    Description = $"Comision tranzacție #{transaction.Id}",
                    CreatedAt = DateTime.UtcNow,
                    ProcessedAt = DateTime.UtcNow
                };

                _db.Transactions.Add(feeTx);
                await _db.SaveChangesAsync();

                _logger.LogInformation(
                    $"[Decorator][Fee] Comision aplicat — {fee} {transaction.Currency} pentru tranzacția #{transaction.Id}");
            }
        }

        return await base.ProcessAsync(transaction);
    }
}
