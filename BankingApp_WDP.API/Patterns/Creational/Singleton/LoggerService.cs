namespace BankingApp.Patterns.Creational.Singleton;

/// <summary>
/// Singleton #1 — centralizează logging-ul întregii aplicații prin ILoggerFactory din DI.
/// Înregistrat ca AddSingleton în Program.cs, deci o singură instanță pe toată durata aplicației.
/// </summary>
public class LoggerService : ILoggerService
{
    private readonly ILogger<LoggerService> _logger;

    public LoggerService(ILogger<LoggerService> logger)
    {
        _logger = logger;
    }

    public void LogInformation(string message) =>
        _logger.LogInformation("[BankingApp] {Message}", message);

    public void LogWarning(string message) =>
        _logger.LogWarning("[BankingApp] {Message}", message);

    public void LogError(string message, Exception? exception = null)
    {
        if (exception is not null)
            _logger.LogError(exception, "[BankingApp] {Message}", message);
        else
            _logger.LogError("[BankingApp] {Message}", message);
    }

    public void LogDebug(string message) =>
        _logger.LogDebug("[BankingApp] {Message}", message);
}
