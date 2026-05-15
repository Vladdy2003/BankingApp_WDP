using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Concrete product (mock) — simulates email sending without touching SMTP.
/// Used in tests and development to avoid real side effects.
/// </summary>
public class MockEmailNotification : IEmailNotification
{
    private readonly ILoggerService _logger;

    public MockEmailNotification(ILoggerService logger)
    {
        _logger = logger;
    }

    public Task SendAsync(string to, string subject, string body)
    {
        _logger.LogInformation($"[MOCK EMAIL] To: {to} | Subject: {subject} | Body: {body}");
        return Task.CompletedTask;
    }
}
