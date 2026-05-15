using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Concrete factory — creates production notification products that deliver
/// messages to real recipients via SMTP and SMS gateway.
/// </summary>
public class ProductionNotificationFactory : INotificationFactory
{
    private readonly ILoggerService _logger;
    private readonly IConfiguration _configuration;

    public ProductionNotificationFactory(ILoggerService logger, IConfiguration configuration)
    {
        _logger = logger;
        _configuration = configuration;
    }

    public IEmailNotification CreateEmailNotification() =>
        new ProductionEmailNotification(_logger, _configuration);

    public ISmsNotification CreateSmsNotification() =>
        new ProductionSmsNotification(_logger);
}
