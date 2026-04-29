using BankingApp.DTOs.Banking;
using System.Text;

namespace BankingApp.Patterns.Behavioral.Strategy;

public class CsvExportStrategy : IExportStrategy
{
    public string Format => "csv";
    public string ContentType => "text/csv";
    public string FileExtension => "csv";

    public Task<byte[]> ExportAsync(MonthlyReportDto report)
    {
        var sb = new StringBuilder();

        // Document header
        sb.AppendLine($"Extras de cont,{report.IBAN}");
        sb.AppendLine($"Perioada,\"{new DateTime(report.Year, report.Month, 1):MMMM yyyy}\"");
        sb.AppendLine($"Moneda,{report.Currency}");
        sb.AppendLine($"Nr. tranzactii,{report.TransactionCount}");
        sb.AppendLine();

        // Financial summary
        sb.AppendLine("Sold initial,Total credite,Total debite,Total comisioane,Sold final");
        sb.AppendLine($"{report.OpeningBalance:F2},{report.TotalCredits:F2},{report.TotalDebits:F2},{report.TotalFees:F2},{report.ClosingBalance:F2}");
        sb.AppendLine();

        // Transactions
        sb.AppendLine("ID,Data,Tip,Directie,Suma,Moneda,Status,Descriere");
        foreach (var tx in report.Transactions)
        {
            var desc = (tx.Description ?? string.Empty).Replace("\"", "\"\"");
            sb.AppendLine($"{tx.Id},{tx.Date:yyyy-MM-dd HH:mm},{tx.Type},{tx.Direction},{tx.Amount:F2},{tx.Currency},{tx.Status},\"{desc}\"");
        }

        // BOM for proper UTF-8 display in Excel
        var bom = Encoding.UTF8.GetPreamble();
        var content = Encoding.UTF8.GetBytes(sb.ToString());
        return Task.FromResult(bom.Concat(content).ToArray());
    }
}
