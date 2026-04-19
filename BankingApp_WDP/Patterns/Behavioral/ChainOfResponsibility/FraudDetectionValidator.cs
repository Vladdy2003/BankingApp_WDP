using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Patterns.Behavioral.ChainOfResponsibility;

/// <summary>
/// Chain link #4 — blocks transactions that exceed the absolute per-transaction maximum
/// (MaxTransferAmount). Amounts above FraudDetectionThreshold but below the maximum are
/// logged as suspicious but allowed to proceed.
/// </summary>
public class FraudDetectionValidator : TransactionValidatorBase
{
    private readonly AppConfigurationManager _config;
    private readonly ILoggerService _logger;

    public FraudDetectionValidator(AppConfigurationManager config, ILoggerService logger)
    {
        _config = config;
        _logger = logger;
    }

    public override void Validate(Transaction tx, Account account)
    {
        var maxAmount = _config.BankingLimits.MaxTransferAmount;
        var suspiciousThreshold = _config.BankingLimits.FraudDetectionThreshold;

        if (tx.Amount > maxAmount)
            throw new InvalidOperationException(
                $"Transaction blocked: amount {tx.Amount} {tx.Currency} exceeds the maximum " +
                $"allowed per transaction ({maxAmount} MDL). Contact support for high-value transfers.");

        if (tx.Amount > suspiciousThreshold)
            _logger.LogWarning(
                $"[FraudDetection] Suspicious transaction on account {account.IBAN}: " +
                $"{tx.Amount} {tx.Currency} ({tx.Type}). Flagged for review.");

        PassToNext(tx, account);
    }
}
