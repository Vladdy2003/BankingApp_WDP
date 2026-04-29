namespace BankingApp.Patterns.Creational.AbstractFactory;

/// <summary>
/// Abstract Factory Pattern #3 — declares the factory interface for creating
/// families of related notification products (email + SMS) without specifying
/// their concrete classes. Switch the factory to swap the entire delivery mechanism.
/// </summary>
public interface INotificationFactory
{
    IEmailNotification CreateEmailNotification();
    ISmsNotification CreateSmsNotification();
}
