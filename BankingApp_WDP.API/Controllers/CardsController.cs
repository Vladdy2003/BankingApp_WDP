using System.Security.Claims;
using BankingApp.Data;
using BankingApp.DTOs.Cards;
using BankingApp.Models;
using BankingApp.Patterns.Creational.AbstractFactory;
using BankingApp.Patterns.Creational.Prototype;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Controllers;

[ApiController]
[Route("api/cards")]
[Authorize]
public class CardsController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly ICardFactory _cardFactory;
    private readonly IAccountService _accountService;
    private readonly ILoggerService _logger;
    private readonly INotificationFactory _notificationFactory;
    private readonly IEmailTemplateService _emailTemplates;
    private readonly IAuditService _auditService;

    public CardsController(
        AppDbContext db,
        ICardFactory cardFactory,
        IAccountService accountService,
        ILoggerService logger,
        INotificationFactory notificationFactory,
        IEmailTemplateService emailTemplates,
        IAuditService auditService)
    {
        _db                  = db;
        _cardFactory         = cardFactory;
        _accountService      = accountService;
        _logger              = logger;
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

    // GET /api/cards
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var cards = await _db.Cards
            .Include(c => c.Account)
            .Where(c => c.Account.UserId == CurrentUserId)
            .ToListAsync();

        return Ok(cards.Select(CardResponse.FromCard));
    }

    // GET /api/cards/{id}
    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetById(int id)
    {
        var card = await _db.Cards
            .Include(c => c.Account)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (card == null)
            return NotFound(new { message = "Cardul nu a fost găsit." });

        if (card.Account.UserId != CurrentUserId)
            return Forbid();

        return Ok(CardResponse.FromCard(card));
    }

    // POST /api/cards
    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateCardRequest request)
    {
        Account? account;
        try
        {
            account = await _accountService.GetByIdAsync(request.AccountId);
        }
        catch (UnauthorizedAccessException)
        {
            return Forbid();
        }

        if (account == null)
            return NotFound(new { message = "Contul nu a fost găsit." });

        if (account.Status != AccountStatus.Active)
            return BadRequest(new { message = "Nu se poate emite un card pe un cont inactiv sau închis." });

        var card = _cardFactory.CreateCard(
            accountId: request.AccountId,
            cardType: request.Type,
            dailyLimit: request.DailyLimit,
            monthlyLimit: request.MonthlyLimit);

        _db.Cards.Add(card);
        await _db.SaveChangesAsync();

        _logger.LogInformation($"Card emis: Id={card.Id}, Type={card.Type}, AccountId={card.AccountId}");

        await _auditService.LogAsync(
            action:     "Card.Create",
            userId:     CurrentUserId,
            entityType: "Card",
            entityId:   card.Id.ToString(),
            newValues:  $"{{\"type\":\"{card.Type}\",\"accountId\":{card.AccountId}," +
                        $"\"dailyLimit\":{card.DailyLimit},\"monthlyLimit\":{card.MonthlyLimit}}}",
            ipAddress:  GetClientIp(),
            userAgent:  GetUserAgent());

        // Trimitere confirmare prin Abstract Factory Pattern #3
        var firstName   = User.FindFirstValue("firstName") ?? "Client";
        var userEmail   = User.FindFirstValue(ClaimTypes.Email) ?? string.Empty;
        var last4       = card.CardNumber.Length >= 4 ? card.CardNumber[^4..] : card.CardNumber;
        var maskedNumber = $"**** **** **** {last4}";

        var emailNotif = _notificationFactory.CreateEmailNotification();
        await emailNotif.SendAsync(
            userEmail,
            "Card bancar emis — BankingApp",
            _emailTemplates.CardCreatedEmail(
                firstName,
                card.Type.ToString(),
                maskedNumber,
                card.ExpiryDate,
                card.DailyLimit,
                card.MonthlyLimit,
                account.Currency,
                card.CreatedAt));

        return CreatedAtAction(nameof(GetById), new { id = card.Id }, CardResponse.FromCard(card));
    }

    // PUT /api/cards/{id}/block
    [HttpPut("{id:int}/block")]
    public async Task<IActionResult> Block(int id)
    {
        var card = await _db.Cards
            .Include(c => c.Account)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (card == null)
            return NotFound(new { message = "Cardul nu a fost găsit." });

        if (card.Account.UserId != CurrentUserId)
            return Forbid();

        if (card.Status == CardStatus.Blocked)
            return BadRequest(new { message = "Cardul este deja blocat." });

        if (card.Status == CardStatus.Expired)
            return BadRequest(new { message = "Nu se poate bloca un card expirat/anulat." });

        card.Status = CardStatus.Blocked;
        await _db.SaveChangesAsync();

        _logger.LogInformation($"Card blocat: Id={card.Id}");

        await _auditService.LogAsync(
            action:     "Card.Block",
            userId:     CurrentUserId,
            entityType: "Card",
            entityId:   card.Id.ToString(),
            newValues:  "{\"status\":\"Blocked\"}",
            ipAddress:  GetClientIp(),
            userAgent:  GetUserAgent());

        return Ok(CardResponse.FromCard(card));
    }

    // PUT /api/cards/{id}/unblock
    [HttpPut("{id:int}/unblock")]
    public async Task<IActionResult> Unblock(int id)
    {
        var card = await _db.Cards
            .Include(c => c.Account)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (card == null)
            return NotFound(new { message = "Cardul nu a fost găsit." });

        if (card.Account.UserId != CurrentUserId)
            return Forbid();

        if (card.Status != CardStatus.Blocked)
            return BadRequest(new { message = "Cardul nu este blocat." });

        card.Status = CardStatus.Active;
        await _db.SaveChangesAsync();

        _logger.LogInformation($"Card deblocat: Id={card.Id}");

        await _auditService.LogAsync(
            action:     "Card.Unblock",
            userId:     CurrentUserId,
            entityType: "Card",
            entityId:   card.Id.ToString(),
            newValues:  "{\"status\":\"Active\"}",
            ipAddress:  GetClientIp(),
            userAgent:  GetUserAgent());

        return Ok(CardResponse.FromCard(card));
    }

    // PUT /api/cards/{id}/limits
    [HttpPut("{id:int}/limits")]
    public async Task<IActionResult> UpdateLimits(int id, [FromBody] UpdateCardLimitsRequest request)
    {
        if (request.DailyLimit == null && request.MonthlyLimit == null)
            return BadRequest(new { message = "Trebuie specificat cel puțin o limită nouă." });

        var card = await _db.Cards
            .Include(c => c.Account)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (card == null)
            return NotFound(new { message = "Cardul nu a fost găsit." });

        if (card.Account.UserId != CurrentUserId)
            return Forbid();

        if (card.Status == CardStatus.Expired)
            return BadRequest(new { message = "Nu se pot modifica limitele unui card expirat/anulat." });

        if (request.DailyLimit.HasValue)
            card.DailyLimit = request.DailyLimit.Value;

        if (request.MonthlyLimit.HasValue)
            card.MonthlyLimit = request.MonthlyLimit.Value;

        await _db.SaveChangesAsync();

        _logger.LogInformation($"Limite card actualizate: Id={card.Id}, Daily={card.DailyLimit}, Monthly={card.MonthlyLimit}");

        await _auditService.LogAsync(
            action:     "Card.UpdateLimits",
            userId:     CurrentUserId,
            entityType: "Card",
            entityId:   card.Id.ToString(),
            newValues:  $"{{\"dailyLimit\":{card.DailyLimit},\"monthlyLimit\":{card.MonthlyLimit}}}",
            ipAddress:  GetClientIp(),
            userAgent:  GetUserAgent());

        return Ok(CardResponse.FromCard(card));
    }

    // POST /api/cards/{id}/validate-payment
    [HttpPost("{id:int}/validate-payment")]
    public async Task<IActionResult> ValidatePayment(int id, [FromBody] ValidatePaymentRequest request)
    {
        var card = await _db.Cards
            .Include(c => c.Account)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (card == null)
            return NotFound(new { message = "Cardul nu a fost găsit." });

        if (card.Account.UserId != CurrentUserId)
            return Forbid();

        var today = DateTime.UtcNow.Date;
        var firstOfMonth = new DateTime(DateTime.UtcNow.Year, DateTime.UtcNow.Month, 1, 0, 0, 0, DateTimeKind.Utc);

        var dailySpent = await _db.Transactions
            .Where(t => t.FromAccountId == card.AccountId
                && t.CreatedAt >= today
                && t.Status != TransactionStatus.Failed
                && t.Status != TransactionStatus.Cancelled)
            .SumAsync(t => (decimal?)t.Amount) ?? 0m;

        var monthlySpent = await _db.Transactions
            .Where(t => t.FromAccountId == card.AccountId
                && t.CreatedAt >= firstOfMonth
                && t.Status != TransactionStatus.Failed
                && t.Status != TransactionStatus.Cancelled)
            .SumAsync(t => (decimal?)t.Amount) ?? 0m;

        var remainingDaily = Math.Max(0, card.DailyLimit - dailySpent);
        var remainingMonthly = Math.Max(0, card.MonthlyLimit - monthlySpent);

        var baseResponse = new ValidatePaymentResponse
        {
            CardId = card.Id,
            Amount = request.Amount,
            Currency = request.Currency,
            AvailableBalance = card.Account.Balance,
            RemainingDailyLimit = remainingDaily,
            RemainingMonthlyLimit = remainingMonthly
        };

        // Verificare 1: card neexpirat
        if (card.ExpiryDate < DateTime.UtcNow)
        {
            baseResponse.IsAllowed = false;
            baseResponse.Reason = "Cardul este expirat.";
            return Ok(baseResponse);
        }

        // Verificare 2: card activ
        if (card.Status != CardStatus.Active)
        {
            baseResponse.IsAllowed = false;
            baseResponse.Reason = $"Cardul nu este activ (status curent: {card.Status}).";
            return Ok(baseResponse);
        }

        // Verificare 3: fonduri suficiente
        if (card.Account.Balance < request.Amount)
        {
            baseResponse.IsAllowed = false;
            baseResponse.Reason = "Fonduri insuficiente în contul asociat cardului.";
            return Ok(baseResponse);
        }

        // Verificare 4: limita zilnică
        if (dailySpent + request.Amount > card.DailyLimit)
        {
            baseResponse.IsAllowed = false;
            baseResponse.Reason = $"Limita zilnică a cardului ar fi depășită (disponibil: {remainingDaily:F2} {request.Currency}).";
            return Ok(baseResponse);
        }

        // Verificare 5: limita lunară
        if (monthlySpent + request.Amount > card.MonthlyLimit)
        {
            baseResponse.IsAllowed = false;
            baseResponse.Reason = $"Limita lunară a cardului ar fi depășită (disponibil: {remainingMonthly:F2} {request.Currency}).";
            return Ok(baseResponse);
        }

        baseResponse.IsAllowed = true;
        _logger.LogInformation($"Validare plată aprobată: CardId={card.Id}, Amount={request.Amount} {request.Currency}");
        return Ok(baseResponse);
    }

    // DELETE /api/cards/{id}
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Cancel(int id)
    {
        var card = await _db.Cards
            .Include(c => c.Account)
            .FirstOrDefaultAsync(c => c.Id == id);

        if (card == null)
            return NotFound(new { message = "Cardul nu a fost găsit." });

        if (card.Account.UserId != CurrentUserId)
            return Forbid();

        if (card.Status == CardStatus.Expired)
            return BadRequest(new { message = "Cardul este deja anulat." });

        card.Status = CardStatus.Expired;
        await _db.SaveChangesAsync();

        _logger.LogInformation($"Card anulat: Id={card.Id}");

        await _auditService.LogAsync(
            action:     "Card.Cancel",
            userId:     CurrentUserId,
            entityType: "Card",
            entityId:   card.Id.ToString(),
            newValues:  "{\"status\":\"Expired\"}",
            ipAddress:  GetClientIp(),
            userAgent:  GetUserAgent());

        return NoContent();
    }
}
