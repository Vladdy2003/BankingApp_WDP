namespace BankingApp.Patterns.Behavioral.Command;

public interface ITransactionCommand
{
    Task ExecuteAsync();
    Task UndoAsync();
}
