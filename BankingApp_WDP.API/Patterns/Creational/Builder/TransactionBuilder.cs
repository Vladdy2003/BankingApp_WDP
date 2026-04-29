using BankingApp.Models;

namespace BankingApp.Patterns.Creational.Builder;

public class TransactionBuilder : ITransactionBuilder
{
    private decimal? _amount;
    private TransactionType? _type;
    private string? _description;
    private int? _fromAccountId;
    private int? _toAccountId;
    private string _currency = "MDL";

    public ITransactionBuilder WithAmount(decimal amount)
    {
        if (amount <= 0)
            throw new ArgumentException("Amount must be greater than zero.", nameof(amount));
        _amount = amount;
        return this;
    }

    public ITransactionBuilder WithType(TransactionType type)
    {
        _type = type;
        return this;
    }

    public ITransactionBuilder WithDescription(string description)
    {
        _description = description;
        return this;
    }

    public ITransactionBuilder WithSourceAccount(int fromAccountId)
    {
        if (fromAccountId <= 0)
            throw new ArgumentException("Source account ID must be positive.", nameof(fromAccountId));
        _fromAccountId = fromAccountId;
        return this;
    }

    public ITransactionBuilder WithDestinationAccount(int toAccountId)
    {
        if (toAccountId <= 0)
            throw new ArgumentException("Destination account ID must be positive.", nameof(toAccountId));
        _toAccountId = toAccountId;
        return this;
    }

    public ITransactionBuilder WithCurrency(string currency)
    {
        if (string.IsNullOrWhiteSpace(currency) || currency.Length != 3)
            throw new ArgumentException("Currency must be a 3-letter ISO code.", nameof(currency));
        _currency = currency.ToUpper();
        return this;
    }

    public Transaction Build()
    {
        if (_amount is null)
            throw new InvalidOperationException("Transaction amount is required.");
        if (_type is null)
            throw new InvalidOperationException("Transaction type is required.");

        if (_type == TransactionType.Transfer)
        {
            if (_fromAccountId is null)
                throw new InvalidOperationException("Source account is required for transfers.");
            if (_toAccountId is null)
                throw new InvalidOperationException("Destination account is required for transfers.");
        }

        if (_type == TransactionType.Withdrawal && _fromAccountId is null)
            throw new InvalidOperationException("Source account is required for withdrawals.");

        if (_type == TransactionType.Deposit && _toAccountId is null)
            throw new InvalidOperationException("Destination account is required for deposits.");

        return new Transaction
        {
            Amount = _amount.Value,
            Type = _type.Value,
            Description = _description,
            FromAccountId = _fromAccountId,
            ToAccountId = _toAccountId,
            Currency = _currency,
            Status = TransactionStatus.Pending,
            CreatedAt = DateTime.UtcNow
        };
    }
}
