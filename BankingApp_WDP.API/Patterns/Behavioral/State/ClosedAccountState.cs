using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.State;

/// <summary>
/// Stare Închisă — stare terminală, nicio operație nu mai este permisă.
/// Un cont închis nu poate fi reactivat sau suspendat.
/// </summary>
public class ClosedAccountState : IAccountState
{
    public string StateName => "Closed";

    public void GuardDeposit(Account account) =>
        throw new InvalidOperationException(
            $"Contul {account.IBAN} este închis. Nicio operație financiară nu este permisă.");

    public void GuardWithdrawal(Account account) =>
        throw new InvalidOperationException(
            $"Contul {account.IBAN} este închis. Nicio operație financiară nu este permisă.");

    public void GuardTransfer(Account account) =>
        throw new InvalidOperationException(
            $"Contul {account.IBAN} este închis. Nicio operație financiară nu este permisă.");

    public AccountStatus Suspend(Account account) =>
        throw new InvalidOperationException($"Contul {account.IBAN} este închis definitiv și nu poate fi modificat.");

    public AccountStatus Activate(Account account) =>
        throw new InvalidOperationException($"Contul {account.IBAN} este închis definitiv și nu poate fi reactivat.");

    public AccountStatus Close(Account account) =>
        throw new InvalidOperationException($"Contul {account.IBAN} este deja închis.");
}
