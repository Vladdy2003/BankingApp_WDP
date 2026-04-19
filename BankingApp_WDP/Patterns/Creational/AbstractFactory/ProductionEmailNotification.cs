using BankingApp.Patterns.Creational.Singleton;
using System.Net;
using System.Net.Mail;

namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Concrete product — sends real emails via SMTP using settings from EmailSettings config section.
/// </summary>
public class ProductionEmailNotification : IEmailNotification
{
    private readonly ILoggerService _logger;
    private readonly IConfiguration _configuration;

    public ProductionEmailNotification(ILoggerService logger, IConfiguration configuration)
    {
        _logger = logger;
        _configuration = configuration;
    }

    public async Task SendAsync(string to, string subject, string body)
    {
        var emailSettings = _configuration.GetSection("EmailSettings");
        var host = emailSettings["Host"];
        var portStr = emailSettings["Port"];
        var username = emailSettings["Username"];
        var password = emailSettings["Password"];
        var from = emailSettings["From"];

        if (string.IsNullOrWhiteSpace(host) || string.IsNullOrWhiteSpace(from))
        {
            _logger.LogWarning($"SMTP not configured — email not sent to {to} (subject: {subject})");
            return;
        }

        using var client = new SmtpClient(host, int.TryParse(portStr, out var port) ? port : 587)
        {
            EnableSsl = true,
            Credentials = new NetworkCredential(username, password)
        };

        var message = new MailMessage(from, to, subject, body) { IsBodyHtml = true };

        await client.SendMailAsync(message);
        _logger.LogInformation($"Email sent to {to} — subject: {subject}");
    }
}
