using System.ComponentModel.DataAnnotations;

namespace BankingApp.Models;

public class AuditLog
{
    public int Id { get; set; }

    public string? UserId { get; set; }

    [Required]
    [MaxLength(100)]
    public string Action { get; set; } = null!;

    [MaxLength(100)]
    public string? EntityType { get; set; }

    public string? EntityId { get; set; }

    public string? OldValues { get; set; }

    public string? NewValues { get; set; }

    [MaxLength(45)]
    public string? IpAddress { get; set; }

    [MaxLength(500)]
    public string? UserAgent { get; set; }

    public DateTime Timestamp { get; set; } = DateTime.UtcNow;
}
