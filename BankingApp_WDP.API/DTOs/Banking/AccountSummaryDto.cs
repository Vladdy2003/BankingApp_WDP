namespace BankingApp.DTOs.Banking;

public class AccountSummaryDto
{
    public string UserId { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public decimal TotalBalance { get; set; }
    public int AccountCount { get; set; }
    public int ActiveCardCount { get; set; }
    public List<AccountSummaryItem> Accounts { get; set; } = [];
    public List<RecentTransactionItem> RecentTransactions { get; set; } = [];
}

public class AccountSummaryItem
{
    public int Id { get; set; }
    public string IBAN { get; set; } = string.Empty;
    public string Type { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public decimal Balance { get; set; }
    public string Currency { get; set; } = string.Empty;
    public int CardCount { get; set; }
}

public class RecentTransactionItem
{
    public int Id { get; set; }
    public string Type { get; set; } = string.Empty;
    public decimal Amount { get; set; }
    public string Currency { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public string? Description { get; set; }
    public DateTime CreatedAt { get; set; }
}
