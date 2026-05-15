using BankingApp.Models;

namespace BankingApp.DTOs.Transactions;

public class TransactionResponse
{
    public int Id { get; set; }
    public int? FromAccountId { get; set; }
    public int? ToAccountId { get; set; }
    public decimal Amount { get; set; }
    public string Currency { get; set; } = string.Empty;
    public string Type { get; set; } = string.Empty;
    public string Status { get; set; } = string.Empty;
    public string? Description { get; set; }
    public DateTime CreatedAt { get; set; }
    public DateTime? ProcessedAt { get; set; }

    public static TransactionResponse FromTransaction(Transaction tx) => new()
    {
        Id = tx.Id,
        FromAccountId = tx.FromAccountId,
        ToAccountId = tx.ToAccountId,
        Amount = tx.Amount,
        Currency = tx.Currency,
        Type = tx.Type.ToString(),
        Status = tx.Status.ToString(),
        Description = tx.Description,
        CreatedAt = tx.CreatedAt,
        ProcessedAt = tx.ProcessedAt
    };
}
