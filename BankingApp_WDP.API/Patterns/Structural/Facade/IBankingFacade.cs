using BankingApp.DTOs.Banking;
using BankingApp.DTOs.Transactions;

namespace BankingApp.Patterns.Structural.Facade;

// Facade Pattern #12 — IBankingFacade: interfață simplificată peste subsistemele de cont, card și tranzacții.
// Clienții (controllere, servicii) apelează metode de nivel înalt fără să cunoască detaliile de orchestrare.
public interface IBankingFacade
{
    // Agregat din AccountService + Cards + Transactions — un singur apel în loc de trei
    Task<AccountSummaryDto> GetAccountSummaryAsync(string userId);

    // Orchestrează validare (Chain), execuție (Command), notificare (Observer) într-o singură metodă
    Task<TransactionResponse> ProcessCompleteTransferAsync(TransferRequest transferDto, string initiatorUserId);

    // Generează extrasul lunar pentru un cont: bilanț, credite, debite, lista tranzacțiilor
    Task<MonthlyReportDto> GenerateMonthlyReportAsync(int accountId, int year, int month);
}
