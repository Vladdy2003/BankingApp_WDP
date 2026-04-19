using System.Security.Claims;
using BankingApp.Data;
using BankingApp.DTOs.Cards;
using BankingApp.Models;
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

    public CardsController(
        AppDbContext db,
        ICardFactory cardFactory,
        IAccountService accountService,
        ILoggerService logger)
    {
        _db = db;
        _cardFactory = cardFactory;
        _accountService = accountService;
        _logger = logger;
    }

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

        return Ok(CardResponse.FromCard(card));
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

        return NoContent();
    }
}
