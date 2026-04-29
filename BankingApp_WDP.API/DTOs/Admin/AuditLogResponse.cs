using BankingApp.Models;

namespace BankingApp.DTOs.Admin;

public class AuditLogResponse
{
    public int Id { get; set; }
    public string? UserId { get; set; }
    public string Action { get; set; } = null!;
    public string? EntityType { get; set; }
    public string? EntityId { get; set; }
    public string? OldValues { get; set; }
    public string? NewValues { get; set; }
    public string? IpAddress { get; set; }
    public string? UserAgent { get; set; }
    public DateTime Timestamp { get; set; }

    public static AuditLogResponse FromAuditLog(AuditLog log) => new()
    {
        Id = log.Id,
        UserId = log.UserId,
        Action = log.Action,
        EntityType = log.EntityType,
        EntityId = log.EntityId,
        OldValues = log.OldValues,
        NewValues = log.NewValues,
        IpAddress = log.IpAddress,
        UserAgent = log.UserAgent,
        Timestamp = log.Timestamp
    };
}
