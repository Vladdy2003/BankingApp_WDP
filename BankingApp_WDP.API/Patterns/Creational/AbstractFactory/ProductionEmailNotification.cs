using BankingApp.Patterns.Creational.Singleton;
using MailKit.Net.Smtp;
using MailKit.Security;
using MimeKit;

namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Concrete product — sends real emails via SMTP using MailKit.
/// Replaces the deprecated System.Net.Mail.SmtpClient which has known SSL/TLS issues with Gmail.
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
        var host     = emailSettings["Host"];
        var portStr  = emailSettings["Port"];
        var username = emailSettings["Username"];
        var password = emailSettings["Password"]?.Replace(" ", ""); // elimină spațiile din App Password
        var from     = emailSettings["From"];

        if (string.IsNullOrWhiteSpace(host) || string.IsNullOrWhiteSpace(from))
        {
            _logger.LogWarning($"[Email] SMTP neconfigurat — email nesolicitat la {to}.");
            return;
        }

        var message = new MimeMessage();
        message.From.Add(MailboxAddress.Parse(from));
        message.To.Add(MailboxAddress.Parse(to));
        message.Subject = subject;
        message.Body = new TextPart(MimeKit.Text.TextFormat.Html) { Text = body };

        using var client = new SmtpClient();
        await client.ConnectAsync(host, int.TryParse(portStr, out var port) ? port : 587, SecureSocketOptions.StartTls);
        await client.AuthenticateAsync(username, password);
        await client.SendAsync(message);
        await client.DisconnectAsync(quit: true);

        _logger.LogInformation($"[Email] Trimis la {to} — subiect: {subject}");
    }
}
