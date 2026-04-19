using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Behavioral.Command;

public interface ITransactionCommandInvoker
{
    Task ExecuteAsync(ITransactionCommand command);
    Task UndoLastAsync();
    void ClearHistory();
    bool HasHistory { get; }
}

public class TransactionCommandInvoker : ITransactionCommandInvoker
{
    private readonly Stack<ITransactionCommand> _history = new();
    private readonly ILoggerService _logger;

    public TransactionCommandInvoker(ILoggerService logger)
    {
        _logger = logger;
    }

    public bool HasHistory => _history.Count > 0;

    public async Task ExecuteAsync(ITransactionCommand command)
    {
        await command.ExecuteAsync();
        _history.Push(command);
        _logger.LogInformation($"[Invoker] Comandă executată. Istoric: {_history.Count} comenzi.");
    }

    public async Task UndoLastAsync()
    {
        if (!_history.TryPop(out var command))
            throw new InvalidOperationException("Nu există comenzi de anulat în istoric.");

        await command.UndoAsync();
        _logger.LogInformation($"[Invoker] Undo executat. Istoric rămas: {_history.Count} comenzi.");
    }

    public void ClearHistory()
    {
        _history.Clear();
        _logger.LogInformation("[Invoker] Istoric comenzi șters.");
    }
}
