using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.ChainOfResponsibility;

/// <summary>
/// Chain link #1 — rejects the transaction if the source account is not Active.
/// </summary>
public class AccountStatusValidator : TransactionValidatorBase
{
    public override void Validate(Transaction tx, Account account)
    {
        if (account.Status != AccountStatus.Active)
            throw new InvalidOperationException(
                $"Account {account.IBAN} is not active (status: {account.Status}). Transaction rejected.");

        PassToNext(tx, account);
    }
}
