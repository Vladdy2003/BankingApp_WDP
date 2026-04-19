namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Abstract product — defines the contract for SMS notification implementations.
/// </summary>
public interface ISmsNotification
{
    Task SendAsync(string phoneNumber, string message);
}
