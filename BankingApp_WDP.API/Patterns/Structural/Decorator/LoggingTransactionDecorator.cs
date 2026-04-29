using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Structural.Decorator;

/// <summary>
/// Decorator #1 — loghează înainte și după procesarea tranzacției prin ILoggerService (Singleton #1).
/// </summary>
public class LoggingTransactionDecorator : TransactionDecoratorBase
{
    private readonly ILoggerService _logger;

    public LoggingTransactionDecorator(ITransactionProcessor inner, ILoggerService logger)
        : base(inner)
    {
        _logger = logger;
    }

    public override async Task<Transaction> ProcessAsync(Transaction transaction)
    {
        _logger.LogInformation(
            $"[Decorator][Logging] Începe procesarea — Id={transaction.Id}, Type={transaction.Type}, Amount={transaction.Amount} {transaction.Currency}");

        var result = await base.ProcessAsync(transaction);

        _logger.LogInformation(
            $"[Decorator][Logging] Procesare finalizată — Id={result.Id}, Status={result.Status}, ProcessedAt={result.ProcessedAt:u}");

        return result;
    }
}
