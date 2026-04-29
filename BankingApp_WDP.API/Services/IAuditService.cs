namespace BankingApp.Services;

public interface IAuditService
{
    Task LogAsync(
        string action,
        string? userId = null,
        string? entityType = null,
        string? entityId = null,
        string? oldValues = null,
        string? newValues = null,
        string? ipAddress = null,
        string? userAgent = null);
}
