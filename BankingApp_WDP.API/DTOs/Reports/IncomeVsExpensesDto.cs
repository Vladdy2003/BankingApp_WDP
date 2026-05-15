namespace BankingApp.DTOs.Reports;

public class IncomeVsExpensesDto
{
    public int AccountId { get; set; }
    public DateTime From { get; set; }
    public DateTime To { get; set; }
    public decimal TotalIncome { get; set; }
    public decimal TotalExpenses { get; set; }
    public decimal NetBalance { get; set; }
    public List<PeriodBreakdown> Periods { get; set; } = [];
}

public class PeriodBreakdown
{
    public string Period { get; set; } = string.Empty; // format "yyyy-MM"
    public decimal Income { get; set; }
    public decimal Expenses { get; set; }
    public decimal Net { get; set; }
}
