using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.State;

/// <summary>
/// State Pattern #13 — definește comportamentul unui cont în funcție de starea sa curentă.
/// Fiecare stare concretă decide ce operații sunt permise și ce tranziții sunt valide.
/// </summary>
public interface IAccountState
{
    string StateName { get; }

    /// <summary>Verifică dacă depunerea este permisă în starea curentă.</summary>
    void GuardDeposit(Account account);

    /// <summary>Verifică dacă retragerea este permisă în starea curentă.</summary>
    void GuardWithdrawal(Account account);

    /// <summary>Verifică dacă transferul este permis în starea curentă.</summary>
    void GuardTransfer(Account account);

    /// <summary>Tranziție → Suspended. Returnează noul status sau aruncă excepție dacă nu e permisă.</summary>
    AccountStatus Suspend(Account account);

    /// <summary>Tranziție → Active. Returnează noul status sau aruncă excepție dacă nu e permisă.</summary>
    AccountStatus Activate(Account account);

    /// <summary>Tranziție → Closed. Returnează noul status sau aruncă excepție dacă nu e permisă.</summary>
    AccountStatus Close(Account account);
}
