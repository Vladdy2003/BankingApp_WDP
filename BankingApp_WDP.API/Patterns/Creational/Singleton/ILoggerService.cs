namespace BankingApp.Patterns.Creational.Singleton;

public interface ILoggerService
{
    void LogInformation(string message);
    void LogWarning(string message);
    void LogError(string message, Exception? exception = null);
    void LogDebug(string message);
}
