using BankingApp.DTOs.Banking;

namespace BankingApp.Patterns.Behavioral.Strategy;

public interface IExportStrategy
{
    string Format { get; }
    string ContentType { get; }
    string FileExtension { get; }
    Task<byte[]> ExportAsync(MonthlyReportDto report);
}
