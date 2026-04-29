using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.ChainOfResponsibility;

/// <summary>
/// Chain link #2 — rejects debits (Withdrawal, Transfer) when balance is insufficient.
/// Deposits always pass through.
/// </summary>
public class BalanceValidator : TransactionValidatorBase
{
    public override void Validate(Transaction tx, Account account)
    {
        bool isDebit = tx.Type is TransactionType.Withdrawal or TransactionType.Transfer;

        if (isDebit && account.Balance < tx.Amount)
            throw new InvalidOperationException(
                $"Insufficient funds. Available: {account.Balance} {account.Currency}, " +
                $"required: {tx.Amount} {account.Currency}.");

        PassToNext(tx, account);
    }
}
