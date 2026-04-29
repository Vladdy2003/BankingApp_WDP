namespace BankingApp.Services;

public interface IInterestService
{
    Task<decimal> CalculateMonthlyInterestAsync(int accountId);
    Task<decimal> ApplyMonthlyInterestAsync(int accountId);
}
