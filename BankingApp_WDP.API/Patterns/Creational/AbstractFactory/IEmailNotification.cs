namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Abstract product — defines the contract for email notification implementations.
/// </summary>
public interface IEmailNotification
{
    Task SendAsync(string to, string subject, string body);
}
