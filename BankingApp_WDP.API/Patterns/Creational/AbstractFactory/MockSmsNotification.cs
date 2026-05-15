using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Concrete product (mock) — simulates SMS sending without calling any gateway.
/// Used in tests and development to avoid real side effects.
/// </summary>
public class MockSmsNotification : ISmsNotification
{
    private readonly ILoggerService _logger;

    public MockSmsNotification(ILoggerService logger)
    {
        _logger = logger;
    }

    public Task SendAsync(string phoneNumber, string message)
    {
        _logger.LogInformation($"[MOCK SMS] To: {phoneNumber} | Message: {message}");
        return Task.CompletedTask;
    }
}
