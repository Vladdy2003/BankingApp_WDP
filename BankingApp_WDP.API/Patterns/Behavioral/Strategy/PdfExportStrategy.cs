using BankingApp.DTOs.Banking;
using QuestPDF.Fluent;
using QuestPDF.Helpers;
using QuestPDF.Infrastructure;
using System.Globalization;

namespace BankingApp.Patterns.Behavioral.Strategy;

public class PdfExportStrategy : IExportStrategy
{
    public string Format => "pdf";
    public string ContentType => "application/pdf";
    public string FileExtension => "pdf";

    public Task<byte[]> ExportAsync(MonthlyReportDto report)
    {
        var monthName = new DateTime(report.Year, report.Month, 1)
            .ToString("MMMM yyyy", new CultureInfo("ro-MD"));

        var document = Document.Create(container =>
        {
            container.Page(page =>
            {
                page.Size(PageSizes.A4);
                page.Margin(1.5f, Unit.Centimetre);
                page.DefaultTextStyle(x => x.FontSize(10).FontFamily(Fonts.Arial));

                page.Header().Element(header => ComposeHeader(header, report, monthName));
                page.Content().PaddingTop(10).Element(content => ComposeContent(content, report));
                page.Footer().AlignCenter().Text(x =>
                {
                    x.Span("BankingApp | Pagina ").FontSize(8).FontColor(Colors.Grey.Darken1);
                    x.CurrentPageNumber().FontSize(8).FontColor(Colors.Grey.Darken1);
                    x.Span(" din ").FontSize(8).FontColor(Colors.Grey.Darken1);
                    x.TotalPages().FontSize(8).FontColor(Colors.Grey.Darken1);
                });
            });
        });

        var bytes = document.GeneratePdf();
        return Task.FromResult(bytes);
    }

    private static void ComposeHeader(IContainer container, MonthlyReportDto report, string monthName)
    {
        container.Column(col =>
        {
            col.Item().Row(row =>
            {
                row.RelativeItem().Column(inner =>
                {
                    inner.Item().Text("BANKINGAPP").Bold().FontSize(20).FontColor("#1a56db");
                    inner.Item().Text("Extras de Cont Lunar").FontSize(11).FontColor(Colors.Grey.Darken2);
                });
                row.RelativeItem().AlignRight().Column(inner =>
                {
                    inner.Item().Text(monthName).Bold().FontSize(12);
                    inner.Item().Text($"IBAN: {report.IBAN}").FontSize(9).FontColor(Colors.Grey.Darken2);
                    inner.Item().Text($"Monedă: {report.Currency}").FontSize(9).FontColor(Colors.Grey.Darken2);
                });
            });

            col.Item().PaddingTop(5).LineHorizontal(1).LineColor("#1a56db");
        });
    }

    private static void ComposeContent(IContainer container, MonthlyReportDto report)
    {
        container.Column(col =>
        {
            // Financial summary section
            col.Item().PaddingVertical(8).Text("Sumar Financiar").Bold().FontSize(12);
            col.Item().Table(table =>
            {
                table.ColumnsDefinition(c =>
                {
                    c.RelativeColumn();
                    c.RelativeColumn();
                    c.RelativeColumn();
                    c.RelativeColumn();
                    c.RelativeColumn();
                });

                static IContainer HeaderCell(IContainer c) =>
                    c.Background("#1a56db").Padding(6);

                static IContainer ValueCell(IContainer c) =>
                    c.BorderBottom(1).BorderColor(Colors.Grey.Lighten2).Padding(6);

                table.Header(h =>
                {
                    foreach (var label in new[] { "Sold inițial", "Total credite", "Total debite", "Total comisioane", "Sold final" })
                        h.Cell().Element(HeaderCell).Text(label).FontColor(Colors.White).Bold().FontSize(9);
                });

                table.Cell().Element(ValueCell).Text($"{report.OpeningBalance:N2} {report.Currency}").FontSize(9);
                table.Cell().Element(ValueCell).Text($"{report.TotalCredits:N2} {report.Currency}").FontColor(Colors.Green.Darken2).FontSize(9);
                table.Cell().Element(ValueCell).Text($"{report.TotalDebits:N2} {report.Currency}").FontColor(Colors.Red.Darken2).FontSize(9);
                table.Cell().Element(ValueCell).Text($"{report.TotalFees:N2} {report.Currency}").FontColor("#d97706").FontSize(9);
                table.Cell().Element(ValueCell).Text($"{report.ClosingBalance:N2} {report.Currency}").Bold().FontSize(9);
            });

            // Transactions section
            col.Item().PaddingTop(14).Text($"Tranzacții ({report.TransactionCount})").Bold().FontSize(12);
            col.Item().PaddingTop(5).Table(table =>
            {
                table.ColumnsDefinition(c =>
                {
                    c.ConstantColumn(30);
                    c.ConstantColumn(78);
                    c.RelativeColumn(1.4f);
                    c.ConstantColumn(45);
                    c.ConstantColumn(78);
                    c.RelativeColumn(2f);
                    c.ConstantColumn(58);
                });

                static IContainer TxHeaderCell(IContainer c) =>
                    c.Background(Colors.Grey.Darken2).Padding(4);

                table.Header(h =>
                {
                    foreach (var label in new[] { "ID", "Data", "Tip", "Dir.", "Sumă", "Descriere", "Status" })
                        h.Cell().Element(TxHeaderCell).Text(label).FontColor(Colors.White).Bold().FontSize(8);
                });

                var odd = false;
                foreach (var tx in report.Transactions)
                {
                    odd = !odd;
                    var bg = odd ? Colors.White : Colors.Grey.Lighten4;
                    var amountColor = tx.Direction == "Credit" ? Colors.Green.Darken2 : Colors.Red.Darken2;

                    IContainer RowCell(IContainer c) =>
                        c.Background(bg).BorderBottom(1).BorderColor(Colors.Grey.Lighten3).Padding(3);

                    table.Cell().Element(RowCell).Text(tx.Id.ToString()).FontSize(8);
                    table.Cell().Element(RowCell).Text(tx.Date.ToString("dd.MM.yy HH:mm")).FontSize(8);
                    table.Cell().Element(RowCell).Text(tx.Type).FontSize(8);
                    table.Cell().Element(RowCell).Text(tx.Direction).FontColor(amountColor).FontSize(8);
                    table.Cell().Element(RowCell).Text($"{tx.Amount:N2}").FontColor(amountColor).FontSize(8);
                    table.Cell().Element(RowCell).Text(tx.Description ?? "-").FontSize(8);
                    table.Cell().Element(RowCell).Text(tx.Status).FontSize(8);
                }
            });
        });
    }
}
