using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BankingApp.Models;

public enum TransactionType
{
    Deposit,
    Withdrawal,
    Transfer,
    Payment
}

public enum TransactionStatus
{
    Pending,
    Completed,
    Failed,
    Cancelled
}

public class Transaction
{
    public int Id { get; set; }

    public int? FromAccountId { get; set; }
    public Account? FromAccount { get; set; }

    public int? ToAccountId { get; set; }
    public Account? ToAccount { get; set; }

    [Required]
    [Column(TypeName = "decimal(18,2)")]
    public decimal Amount { get; set; }

    [MaxLength(3)]
    public string Currency { get; set; } = "RON";

    [Required]
    public TransactionType Type { get; set; }

    public TransactionStatus Status { get; set; } = TransactionStatus.Pending;

    [MaxLength(500)]
    public string? Description { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime? ProcessedAt { get; set; }
}
