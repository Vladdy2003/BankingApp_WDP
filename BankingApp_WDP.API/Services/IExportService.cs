using BankingApp.DTOs.Banking;

namespace BankingApp.Services;

public record ExportResult(byte[] Data, string ContentType, string FileName);

public interface IExportService
{
    IEnumerable<string> SupportedFormats { get; }
    Task<ExportResult> ExportStatementAsync(MonthlyReportDto report, string format);
}
