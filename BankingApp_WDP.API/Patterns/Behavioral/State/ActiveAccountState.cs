using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.State;

/// <summary>
/// Stare Activă — toate operațiile financiare sunt permise.
/// Contul poate fi suspendat sau închis.
/// </summary>
public class ActiveAccountState : IAccountState
{
    public string StateName => "Active";

    public void GuardDeposit(Account account) { }
    public void GuardWithdrawal(Account account) { }
    public void GuardTransfer(Account account) { }

    public AccountStatus Suspend(Account account) => AccountStatus.Suspended;

    public AccountStatus Activate(Account account) =>
        throw new InvalidOperationException($"Contul {account.IBAN} este deja activ.");

    public AccountStatus Close(Account account) => AccountStatus.Closed;
}
