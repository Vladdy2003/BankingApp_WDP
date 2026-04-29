using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.State;

/// <summary>
/// Stare Suspendată — operațiile financiare sunt blocate (investigație frauda, blocare administrativă).
/// Contul poate fi reactivat sau închis definitiv.
/// </summary>
public class SuspendedAccountState : IAccountState
{
    public string StateName => "Suspended";

    public void GuardDeposit(Account account) =>
        throw new InvalidOperationException(
            $"Contul {account.IBAN} este suspendat. Depunerile nu sunt permise.");

    public void GuardWithdrawal(Account account) =>
        throw new InvalidOperationException(
            $"Contul {account.IBAN} este suspendat. Retragerile nu sunt permise.");

    public void GuardTransfer(Account account) =>
        throw new InvalidOperationException(
            $"Contul {account.IBAN} este suspendat. Transferurile nu sunt permise.");

    public AccountStatus Suspend(Account account) =>
        throw new InvalidOperationException($"Contul {account.IBAN} este deja suspendat.");

    public AccountStatus Activate(Account account) => AccountStatus.Active;

    public AccountStatus Close(Account account) => AccountStatus.Closed;
}
