using BankingApp.Models;
using BankingApp.Patterns.Behavioral.State;

namespace BankingApp.Patterns.Behavioral.ChainOfResponsibility;

/// <summary>
/// Chain link #1 — verifică dacă contul sursă permite operația financiară.
/// Delegă decizia obiectului de stare (State Pattern #13) pentru mesaje specifice per stare.
/// </summary>
public class AccountStatusValidator : TransactionValidatorBase
{
    public override void Validate(Transaction tx, Account account)
    {
        var state = AccountStateContext.Resolve(account.Status);

        if (tx.Type == TransactionType.Deposit)
            state.GuardDeposit(account);
        else if (tx.Type == TransactionType.Withdrawal)
            state.GuardWithdrawal(account);
        else
            state.GuardTransfer(account);

        PassToNext(tx, account);
    }
}
