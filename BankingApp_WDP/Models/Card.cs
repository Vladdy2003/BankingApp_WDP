using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BankingApp.Models;

public enum CardType
{
    Debit,
    Credit,
    Prepaid
}

public enum CardStatus
{
    Active,
    Blocked,
    Expired,
    Pending
}

public class Card
{
    public int Id { get; set; }

    [Required]
    public int AccountId { get; set; }
    public Account Account { get; set; } = null!;

    [Required, MaxLength(19)]
    public string CardNumber { get; set; } = string.Empty;

    public DateTime ExpiryDate { get; set; }

    [Required, MaxLength(256)]
    public string CVVHash { get; set; } = string.Empty;

    [Required]
    public CardType Type { get; set; }

    public CardStatus Status { get; set; } = CardStatus.Pending;

    [Column(TypeName = "decimal(18,2)")]
    public decimal DailyLimit { get; set; } = 1000;

    [Column(TypeName = "decimal(18,2)")]
    public decimal MonthlyLimit { get; set; } = 10000;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
