namespace BankingApp.Services;

public interface IEmailTemplateService
{
    string TransactionEmail(string firstName, string transactionType, decimal amount, string currency, int txId, DateTime? processedAt);
    string SecurityAlertEmail(string firstName, string action, string? ipAddress, DateTime timestamp);
    string WelcomeEmail(string firstName, string lastName, string email);
    string AccountCreatedEmail(string firstName, string accountType, string iban, string currency, int accountId, DateTime createdAt);
    string CardCreatedEmail(string firstName, string cardType, string maskedCardNumber, DateTime expiryDate, decimal dailyLimit, decimal monthlyLimit, string currency, DateTime createdAt);
}
