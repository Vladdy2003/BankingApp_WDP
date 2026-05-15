using System.ComponentModel.DataAnnotations;

namespace BankingApp.Models;

public enum NotificationType
{
    Transaction,
    Security,
    Marketing,
    System
}

public class Notification
{
    public int Id { get; set; }

    [Required]
    public string UserId { get; set; } = null!;
    public ApplicationUser? User { get; set; }

    [Required]
    [MaxLength(200)]
    public string Title { get; set; } = null!;

    [Required]
    [MaxLength(1000)]
    public string Message { get; set; } = null!;

    public NotificationType Type { get; set; } = NotificationType.Transaction;

    public bool IsRead { get; set; } = false;

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
}
