using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using System.Security.Claims;

namespace BankingApp.Controllers;

[ApiController]
[Route("api/reports")]
[Authorize]
public class ReportsController(
    IReportService reportService,
    IExportService exportService,
    IAccountService accountService,
    ILoggerService logger) : ControllerBase
{
    private string CurrentUserId =>
        User.FindFirstValue(ClaimTypes.NameIdentifier)!;

    // GET /api/reports/monthly-statement?accountId=1&year=2026&month=4
    [HttpGet("monthly-statement")]
    public async Task<IActionResult> GetMonthlyStatement(
        [FromQuery] int accountId,
        [FromQuery] int year,
        [FromQuery] int month)
    {
        if (year < 2000 || year > DateTime.UtcNow.Year + 1)
            return BadRequest("Anul specificat nu este valid.");
        if (month < 1 || month > 12)
            return BadRequest("Luna trebuie să fie între 1 și 12.");

        var account = await accountService.GetByIdAsync(accountId);
        if (account is null) return NotFound("Contul nu a fost găsit.");
        if (account.UserId != CurrentUserId) return Forbid();

        logger.LogInformation($"[ReportsController] Extras lunar pentru contul {accountId}, {year}/{month:D2}");

        var report = await reportService.GetMonthlyStatementAsync(accountId, year, month);
        return Ok(report);
    }

    // GET /api/reports/annual-summary?accountId=1&year=2026
    [HttpGet("annual-summary")]
    public async Task<IActionResult> GetAnnualSummary(
        [FromQuery] int accountId,
        [FromQuery] int year)
    {
        if (year < 2000 || year > DateTime.UtcNow.Year + 1)
            return BadRequest("Anul specificat nu este valid.");

        var account = await accountService.GetByIdAsync(accountId);
        if (account is null) return NotFound("Contul nu a fost găsit.");
        if (account.UserId != CurrentUserId) return Forbid();

        logger.LogInformation($"[ReportsController] Sumar anual pentru contul {accountId}, {year}");

        var summary = await reportService.GetAnnualSummaryAsync(accountId, year);
        return Ok(summary);
    }

    // GET /api/reports/spending?accountId=1&from=2026-01-01&to=2026-04-30
    [HttpGet("spending")]
    public async Task<IActionResult> GetSpending(
        [FromQuery] int accountId,
        [FromQuery] DateTime from,
        [FromQuery] DateTime to)
    {
        if (from > to)
            return BadRequest("Data de început nu poate fi după data de sfârșit.");

        var account = await accountService.GetByIdAsync(accountId);
        if (account is null) return NotFound("Contul nu a fost găsit.");
        if (account.UserId != CurrentUserId) return Forbid();

        logger.LogInformation($"[ReportsController] Cheltuieli pe categorii pentru contul {accountId}, {from:d} — {to:d}");

        var spending = await reportService.GetSpendingByCategoryAsync(accountId, from.ToUniversalTime(), to.ToUniversalTime());
        return Ok(spending);
    }

    // GET /api/reports/export?accountId=1&format=csv&year=2026&month=4
    [HttpGet("export")]
    public async Task<IActionResult> ExportStatement(
        [FromQuery] int accountId,
        [FromQuery] string format,
        [FromQuery] int? year,
        [FromQuery] int? month)
    {
        if (string.IsNullOrWhiteSpace(format))
            return BadRequest("Parametrul 'format' este obligatoriu (csv sau pdf).");

        var exportYear  = year  ?? DateTime.UtcNow.Year;
        var exportMonth = month ?? DateTime.UtcNow.Month;

        if (exportMonth < 1 || exportMonth > 12)
            return BadRequest("Luna trebuie să fie între 1 și 12.");

        var account = await accountService.GetByIdAsync(accountId);
        if (account is null) return NotFound("Contul nu a fost găsit.");
        if (account.UserId != CurrentUserId) return Forbid();

        logger.LogInformation($"[ReportsController] Export {format.ToUpper()} pentru contul {accountId}, {exportYear}/{exportMonth:D2}");

        try
        {
            var report = await reportService.GetMonthlyStatementAsync(accountId, exportYear, exportMonth);
            var result = await exportService.ExportStatementAsync(report, format);
            return File(result.Data, result.ContentType, result.FileName);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(ex.Message);
        }
    }
}
