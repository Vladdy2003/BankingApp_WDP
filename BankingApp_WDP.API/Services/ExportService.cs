using BankingApp.DTOs.Banking;
using BankingApp.Patterns.Behavioral.Strategy;
using BankingApp.Patterns.Creational.Singleton;

namespace BankingApp.Services;

public class ExportService : IExportService
{
    private readonly Dictionary<string, IExportStrategy> _strategies;
    private readonly ILoggerService _logger;

    public ExportService(IEnumerable<IExportStrategy> strategies, ILoggerService logger)
    {
        _strategies = strategies.ToDictionary(s => s.Format.ToLowerInvariant());
        _logger = logger;
    }

    public IEnumerable<string> SupportedFormats => _strategies.Keys;

    public async Task<ExportResult> ExportStatementAsync(MonthlyReportDto report, string format)
    {
        var key = format.ToLowerInvariant();
        if (!_strategies.TryGetValue(key, out var strategy))
            throw new ArgumentException($"Formatul '{format}' nu este suportat. Formate disponibile: {string.Join(", ", SupportedFormats)}");

        _logger.LogInformation($"[ExportService] Export {format.ToUpper()} pentru contul {report.AccountId}, perioada {report.Year}/{report.Month:D2}");

        var data = await strategy.ExportAsync(report);
        var fileName = $"extras_{report.IBAN}_{report.Year}_{report.Month:D2}.{strategy.FileExtension}";

        return new ExportResult(data, strategy.ContentType, fileName);
    }
}
