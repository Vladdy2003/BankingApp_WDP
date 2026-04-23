using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BankingApp.Models;

public enum AccountType
{
    Current,
    Savings,
    Business
}

public enum AccountStatus
{
    Active,
    Inactive,
    Suspended,
    Closed
}

public abstract class Account
{
    public int Id { get; set; }

    [Required, MaxLength(34)]
    public string IBAN { get; set; } = string.Empty;

    [Required]
    public AccountType Type { get; set; }

    public AccountStatus Status { get; set; } = AccountStatus.Active;

    [Column(TypeName = "decimal(18,2)")]
    public decimal Balance { get; set; } = 0;

    [MaxLength(3)]
    public string Currency { get; set; } = "MDL";

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    [Required]
    public string UserId { get; set; } = string.Empty;

    public ApplicationUser User { get; set; } = null!;

    public ICollection<Transaction> OutgoingTransactions { get; set; } = new List<Transaction>();
    public ICollection<Transaction> IncomingTransactions { get; set; } = new List<Transaction>();
    public ICollection<Card> Cards { get; set; } = new List<Card>();
}
