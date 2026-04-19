using System.Security.Claims;
using BankingApp.DTOs.Accounts;
using BankingApp.Models;
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

    public AccountsController(
        IAccountService accountService,
        IAccountFactory accountFactory,
        INotificationFactory notificationFactory)
    {
        _accountService = accountService;
        _accountFactory = accountFactory;
        _notificationFactory = notificationFactory;
    }

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
        var email = _notificationFactory.CreateEmailNotification();
        await email.SendAsync(
            User.FindFirstValue(ClaimTypes.Email) ?? string.Empty,
            "Cont bancar creat",
            $"Contul tău de tip {created.Type} cu IBAN {created.IBAN} a fost creat cu succes.");

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
            if (account.Status == AccountStatus.Closed)
                return BadRequest(new { message = "Nu se poate actualiza un cont închis." });

            if (request.Currency != null)
                account.Currency = request.Currency;

            if (account is CurrentAccount ca && request.OverdraftLimit.HasValue)
                ca.OverdraftLimit = request.OverdraftLimit.Value;

            if (account is SavingsAccount sa && request.InterestRate.HasValue)
                sa.InterestRate = request.InterestRate.Value;

            if (account is BusinessAccount ba && request.CompanyName != null)
                ba.CompanyName = request.CompanyName;

            var updated = await _accountService.UpdateAsync(account);
            return Ok(AccountResponse.FromAccount(updated));
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

            return NoContent();
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
