using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.State;

/// <summary>
/// Stare Inactivă — cont nou, în așteptarea verificării sau primei utilizări.
/// Permite doar depuneri mici (onboarding), blochează retrageri și transferuri.
/// </summary>
public class InactiveAccountState : IAccountState
{
    public string StateName => "Inactive";

    public void GuardDeposit(Account account) { }

    public void GuardWithdrawal(Account account) =>
        throw new InvalidOperationException(
            $"Contul {account.IBAN} este inactiv. Activează contul înainte de a efectua retrageri.");

    public void GuardTransfer(Account account) =>
        throw new InvalidOperationException(
            $"Contul {account.IBAN} este inactiv. Activează contul înainte de a efectua transferuri.");

    public AccountStatus Suspend(Account account) =>
        throw new InvalidOperationException($"Un cont inactiv nu poate fi suspendat direct.");

    public AccountStatus Activate(Account account) => AccountStatus.Active;

    public AccountStatus Close(Account account) => AccountStatus.Closed;
}
