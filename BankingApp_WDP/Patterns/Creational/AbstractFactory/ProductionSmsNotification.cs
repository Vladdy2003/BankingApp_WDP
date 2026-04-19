using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Concrete product — sends real SMS via an external provider (e.g. Twilio).
/// Provider integration is stubbed; wire it in when an SMS gateway is configured.
/// </summary>
public class ProductionSmsNotification : ISmsNotification
{
    private readonly ILoggerService _logger;

    public ProductionSmsNotification(ILoggerService logger)
    {
        _logger = logger;
    }

    public Task SendAsync(string phoneNumber, string message)
    {
        // Replace with actual SMS gateway call (Twilio, etc.) when configured.
        _logger.LogWarning($"SMS gateway not configured — message not sent to {phoneNumber}: {message}");
        return Task.CompletedTask;
    }
}
