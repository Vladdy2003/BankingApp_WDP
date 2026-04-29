namespace BankingApp.Services;

public interface IEmailTemplateService
{
    string TransactionEmail(string firstName, string transactionType, decimal amount, string currency, int txId, DateTime? processedAt);
    string SecurityAlertEmail(string firstName, string action, string? ipAddress, DateTime timestamp);
    string WelcomeEmail(string firstName, string lastName, string email);
}
