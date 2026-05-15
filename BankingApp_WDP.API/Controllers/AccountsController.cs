using System.Security.Claims;
using BankingApp.DTOs.Accounts;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.State;
using BankingApp.Patterns.Creational.AbstractFactory;
using BankingApp.Patterns.Creational.FactoryMethod;
using BankingApp.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BankingApp.Controllers;

[ApiController]
[Route("api/accounts")]
[Authorize]
public class AccountsController : ControllerBase
{
    private readonly IAccountService _accountService;
    private readonly IAccountFactory _accountFactory;
    private readonly INotificationFactory _notificationFactory;
    private readonly IEmailTemplateService _emailTemplates;
    private readonly IAuditService _auditService;

    public AccountsController(
        IAccountService accountService,
        IAccountFactory accountFactory,
        INotificationFactory notificationFactory,
        IEmailTemplateService emailTemplates,
        IAuditService auditService)
    {
        _accountService      = accountService;
        _accountFactory      = accountFactory;
        _notificationFactory = notificationFactory;
        _emailTemplates      = emailTemplates;
        _auditService        = auditService;
    }

    private string? GetClientIp() =>
        Request.Headers["X-Forwarded-For"].FirstOrDefault()
        ?? HttpContext.Connection.RemoteIpAddress?.ToString();

    private string? GetUserAgent() =>
        Request.Headers["User-Agent"].FirstOrDefault();

    private string CurrentUserId =>
        User.FindFirstValue(ClaimTypes.NameIdentifier)!;

    // GET /api/accounts
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        try
        {
            var accounts = await _accountService.GetByUserIdAsync(CurrentUserId);
            return Ok(accounts.Select(AccountResponse.FromAccount));
        }
        catch (UnauthorizedAccessException ex)
        {
            return Forbid(ex.Message);
        }
    }

    // GET /api/accounts/{id}
    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetById(int id)
    {
        try
        {
            var account = await _accountService.GetByIdAsync(id);
            if (account == null) return NotFound(new { message = "Contul nu a fost găsit." });

            return Ok(AccountResponse.FromAccount(account));
        }
        catch (UnauthorizedAccessException ex)
        {
            return Forbid(ex.Message);
        }
    }

    // POST /api/accounts
    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateAccountRequest request)
    {
        var options = new AccountCreationOptions
        {
            Currency = request.Currency,
            OverdraftLimit = request.OverdraftLimit,
            InterestRate = request.InterestRate,
            CompanyName = request.CompanyName
        };

        var account = _accountFactory.CreateAccount(request.Type, CurrentUserId, options);
        var created = await _accountService.CreateAsync(account);

        // Trimitere confirmare prin Abstract Factory Pattern #3
        var firstName = User.FindFirstValue("firstName") ?? "Client";
        var email = _notificationFactory.CreateEmailNotification();
        await email.SendAsync(
            User.FindFirstValue(ClaimTypes.Email) ?? string.Empty,
            "Cont bancar deschis — BankingApp",
            _emailTemplates.AccountCreatedEmail(
                firstName,
                created.Type.ToString(),
                created.IBAN,
                created.Currency,
                created.Id,
                created.CreatedAt));

        await _auditService.LogAsync(
            action:     "Account.Create",
            userId:     CurrentUserId,
            entityType: "Account",
            entityId:   created.Id.ToString(),
            newValues:  $"{{\"type\":\"{created.Type}\",\"iban\":\"{created.IBAN}\"," +
                        $"\"currency\":\"{created.Currency}\",\"status\":\"{created.Status}\"}}",
            ipAddress:  GetClientIp(),
            userAgent:  GetUserAgent());

        return CreatedAtAction(nameof(GetById), new { id = created.Id }, AccountResponse.FromAccount(created));
    }

    // PUT /api/accounts/{id}
    [HttpPut("{id:int}")]
    public async Task<IActionResult> Update(int id, [FromBody] UpdateAccountRequest request)
    {
        try
        {
            var account = await _accountService.GetByIdAsync(id);
            if (account == null) return NotFound(new { message = "Contul nu a fost găsit." });

            // State Pattern #13 — validăm că actualizarea e permisă în starea curentă
            var state = AccountStateContext.Resolve(account.Status);
            state.GuardDeposit(account); // dacă e Closed aruncă excepție

            if (request.Currency != null)
                account.Currency = request.Currency;

            if (account is CurrentAccount ca && request.OverdraftLimit.HasValue)
                ca.OverdraftLimit = request.OverdraftLimit.Value;

            if (account is SavingsAccount sa && request.InterestRate.HasValue)
                sa.InterestRate = request.InterestRate.Value;

            if (account is BusinessAccount ba && request.CompanyName != null)
                ba.CompanyName = request.CompanyName;

            var updated = await _accountService.UpdateAsync(account);

            await _auditService.LogAsync(
                action:     "Account.Update",
                userId:     CurrentUserId,
                entityType: "Account",
                entityId:   id.ToString(),
                newValues:  $"{{\"currency\":\"{updated.Currency}\"}}",
                ipAddress:  GetClientIp(),
                userAgent:  GetUserAgent());

            return Ok(AccountResponse.FromAccount(updated));
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (UnauthorizedAccessException ex)
        {
            return Forbid(ex.Message);
        }
    }

    // DELETE /api/accounts/{id}
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Close(int id)
    {
        try
        {
            var account = await _accountService.GetByIdAsync(id);
            if (account == null) return NotFound(new { message = "Contul nu a fost găsit." });
            if (account.Balance != 0)
                return BadRequest(new { message = "Contul nu poate fi închis cu sold nenul." });

            var closed = await _accountService.CloseAsync(id);
            if (!closed) return NotFound(new { message = "Contul nu a fost găsit." });

            await _auditService.LogAsync(
                action:     "Account.Close",
                userId:     CurrentUserId,
                entityType: "Account",
                entityId:   id.ToString(),
                ipAddress:  GetClientIp(),
                userAgent:  GetUserAgent());

            return NoContent();
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (UnauthorizedAccessException ex)
        {
            return Forbid(ex.Message);
        }
    }

    // PUT /api/accounts/{id}/suspend — State Pattern #13: Active → Suspended
    [HttpPut("{id:int}/suspend")]
    public async Task<IActionResult> Suspend(int id)
    {
        try
        {
            var result = await _accountService.SuspendAsync(id);
            if (!result) return NotFound(new { message = "Contul nu a fost găsit." });

            await _auditService.LogAsync(
                action:     "Account.Suspend",
                userId:     CurrentUserId,
                entityType: "Account",
                entityId:   id.ToString(),
                newValues:  "{\"status\":\"Suspended\"}",
                ipAddress:  GetClientIp(),
                userAgent:  GetUserAgent());

            return Ok(new { message = "Contul a fost suspendat cu succes." });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (UnauthorizedAccessException ex)
        {
            return Forbid(ex.Message);
        }
    }

    // PUT /api/accounts/{id}/activate — State Pattern #13: Suspended/Inactive → Active
    [HttpPut("{id:int}/activate")]
    public async Task<IActionResult> Activate(int id)
    {
        try
        {
            var result = await _accountService.ActivateAsync(id);
            if (!result) return NotFound(new { message = "Contul nu a fost găsit." });

            await _auditService.LogAsync(
                action:     "Account.Activate",
                userId:     CurrentUserId,
                entityType: "Account",
                entityId:   id.ToString(),
                newValues:  "{\"status\":\"Active\"}",
                ipAddress:  GetClientIp(),
                userAgent:  GetUserAgent());

            return Ok(new { message = "Contul a fost reactivat cu succes." });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (UnauthorizedAccessException ex)
        {
            return Forbid(ex.Message);
        }
    }

    // GET /api/accounts/{id}/balance
    [HttpGet("{id:int}/balance")]
    public async Task<IActionResult> GetBalance(int id)
    {
        try
        {
            var account = await _accountService.GetByIdAsync(id);
            if (account == null) return NotFound(new { message = "Contul nu a fost găsit." });

            return Ok(new { accountId = id, balance = account.Balance, currency = account.Currency });
        }
        catch (UnauthorizedAccessException ex)
        {
            return Forbid(ex.Message);
        }
    }
}
