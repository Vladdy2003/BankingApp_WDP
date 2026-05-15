namespace BankingApp.DTOs.Reports;

public class AnnualSummaryDto
{
    public int AccountId { get; set; }
    public string IBAN { get; set; } = string.Empty;
    public string Currency { get; set; } = string.Empty;
    public int Year { get; set; }
    public decimal TotalCredits { get; set; }
    public decimal TotalDebits { get; set; }
    public decimal TotalFees { get; set; }
    public decimal NetChange { get; set; }
    public int TotalTransactions { get; set; }
    public List<MonthlyBreakdown> MonthlyBreakdowns { get; set; } = [];
}

public class MonthlyBreakdown
{
    public int Month { get; set; }
    public string MonthName { get; set; } = string.Empty;
    public decimal Credits { get; set; }
    public decimal Debits { get; set; }
    public decimal Fees { get; set; }
    public int TransactionCount { get; set; }
}
