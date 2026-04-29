using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Concrete factory — creates mock notification products that log messages
/// instead of delivering them. Used in tests and development environments.
/// </summary>
public class MockNotificationFactory : INotificationFactory
{
    private readonly ILoggerService _logger;

    public MockNotificationFactory(ILoggerService logger)
    {
        _logger = logger;
    }

    public IEmailNotification CreateEmailNotification() =>
        new MockEmailNotification(_logger);

    public ISmsNotification CreateSmsNotification() =>
        new MockSmsNotification(_logger);
}
