using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.State;

/// <summary>
/// Rezolvă starea concretă corespunzătoare statusului curent al contului.
/// Punct unic de intrare pentru State Pattern — elimină switch/if-else răspândite în servicii.
/// </summary>
public static class AccountStateContext
{
    public static IAccountState Resolve(AccountStatus status) => status switch
    {
        AccountStatus.Active    => new ActiveAccountState(),
        AccountStatus.Inactive  => new InactiveAccountState(),
        AccountStatus.Suspended => new SuspendedAccountState(),
        AccountStatus.Closed    => new ClosedAccountState(),
        _ => throw new ArgumentOutOfRangeException(nameof(status), $"Status necunoscut: {status}")
    };
}
