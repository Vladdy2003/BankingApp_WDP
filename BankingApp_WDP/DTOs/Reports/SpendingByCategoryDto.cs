namespace BankingApp.DTOs.Reports;

public class SpendingByCategoryDto
{
    public int AccountId { get; set; }
    public DateTime From { get; set; }
    public DateTime To { get; set; }
    public decimal TotalSpending { get; set; }
    public List<CategoryBreakdown> Categories { get; set; } = [];
}

public class CategoryBreakdown
{
    public string Category { get; set; } = string.Empty;
    public decimal Amount { get; set; }
    public decimal Percentage { get; set; }
    public int TransactionCount { get; set; }
}
