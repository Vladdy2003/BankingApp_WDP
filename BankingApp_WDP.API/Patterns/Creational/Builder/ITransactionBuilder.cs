using BankingApp.Models;

namespace BankingApp.Patterns.Creational.Builder;

public interface ITransactionBuilder
{
    ITransactionBuilder WithAmount(decimal amount);
    ITransactionBuilder WithType(TransactionType type);
    ITransactionBuilder WithDescription(string description);
    ITransactionBuilder WithSourceAccount(int fromAccountId);
    ITransactionBuilder WithDestinationAccount(int toAccountId);
    ITransactionBuilder WithCurrency(string currency);
    Transaction Build();
}
