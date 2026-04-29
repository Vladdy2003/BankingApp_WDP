namespace BankingApp.DTOs.Banking;

public class MonthlyReportDto
{
    public int AccountId { get; set; }
    public string IBAN { get; set; } = string.Empty;
    public string Currency { get; set; } = string.Empty;
    public int Year { get; set; }
    public int Month { get; set; }
    public decimal OpeningBalance { get; set; }
    public decimal ClosingBalance { get; set; }
    public decimal TotalCredits { get; set; }
    public decimal TotalDebits { get; set; }
    public decimal TotalFees { get; set; }
    public int TransactionCount { get; set; }
    public List<TransactionLineItem> Transactions { get; set; } = [];
}

public class TransactionLineItem
{
    public int Id { get; set; }
    public DateTime Date { get; set; }
    public string Type { get; set; } = string.Empty;
    public string? Description { get; set; }
    public decimal Amount { get; set; }
    public string Currency { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public string Direction { get; set; } = string.Empty; // "Credit" | "Debit"
}
