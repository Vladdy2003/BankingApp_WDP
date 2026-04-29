using BankingApp.DTOs.Banking;
using BankingApp.DTOs.Reports;

namespace BankingApp.Services;

public interface IReportService
{
    Task<MonthlyReportDto> GetMonthlyStatementAsync(int accountId, int year, int month);
    Task<AnnualSummaryDto> GetAnnualSummaryAsync(int accountId, int year);
    Task<SpendingByCategoryDto> GetSpendingByCategoryAsync(int accountId, DateTime from, DateTime to);
    Task<IncomeVsExpensesDto> GetIncomeVsExpensesAsync(int accountId, DateTime from, DateTime to);
}
