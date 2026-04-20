using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Services;

public class AuditService : IAuditService
{
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    public AuditService(AppDbContext db, ILoggerService logger)
    {
        _db = db;
        _logger = logger;
    }

    public async Task LogAsync(
        string action,
        string? userId = null,
        string? entityType = null,
        string? entityId = null,
        string? oldValues = null,
        string? newValues = null,
        string? ipAddress = null,
        string? userAgent = null)
    {
        var entry = new AuditLog
        {
            Action = action,
            UserId = userId,
            EntityType = entityType,
            EntityId = entityId,
            OldValues = oldValues,
            NewValues = newValues,
            IpAddress = ipAddress,
            UserAgent = userAgent,
            Timestamp = DateTime.UtcNow
        };

        _db.AuditLogs.Add(entry);
        await _db.SaveChangesAsync();

        _logger.LogInformation($"[Audit] {action} — entitate: {entityType} #{entityId}, utilizator: {userId ?? "sistem"}");
    }
}
