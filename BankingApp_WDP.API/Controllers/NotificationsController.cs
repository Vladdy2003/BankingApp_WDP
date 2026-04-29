using System.Security.Claims;
using BankingApp.Data;
using BankingApp.DTOs.Notifications;
using BankingApp.Patterns.Creational.Singleton;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Controllers;

[ApiController]
[Route("api/notifications")]
[Authorize]
public class NotificationsController : ControllerBase
{
    private readonly AppDbContext _db;
    private readonly ILoggerService _logger;

    public NotificationsController(AppDbContext db, ILoggerService logger)
    {
        _db = db;
        _logger = logger;
    }

    private string CurrentUserId => User.FindFirstValue(ClaimTypes.NameIdentifier)!;

    // GET /api/notifications?page=1&pageSize=20
    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] int page = 1, [FromQuery] int pageSize = 20)
    {
        if (page < 1) page = 1;
        if (pageSize < 1 || pageSize > 100) pageSize = 20;

        var query = _db.Notifications
            .Where(n => n.UserId == CurrentUserId)
            .OrderByDescending(n => n.CreatedAt);

        var total = await query.CountAsync();
        var items = await query
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .Select(n => NotificationResponse.FromNotification(n))
            .ToListAsync();

        _logger.LogInformation($"[Notifications] Utilizatorul {CurrentUserId} a cerut lista de notificări (pagina {page}).");

        return Ok(new { page, pageSize, total, items });
    }

    // GET /api/notifications/unread-count
    [HttpGet("unread-count")]
    public async Task<IActionResult> GetUnreadCount()
    {
        var count = await _db.Notifications
            .CountAsync(n => n.UserId == CurrentUserId && !n.IsRead);

        return Ok(new { unreadCount = count });
    }

    // PUT /api/notifications/{id}/read
    [HttpPut("{id:int}/read")]
    public async Task<IActionResult> MarkAsRead(int id)
    {
        var notification = await _db.Notifications
            .FirstOrDefaultAsync(n => n.Id == id && n.UserId == CurrentUserId);

        if (notification == null)
            return NotFound(new { message = $"Notificarea #{id} nu a fost găsită." });

        if (notification.IsRead)
            return Ok(NotificationResponse.FromNotification(notification));

        notification.IsRead = true;
        await _db.SaveChangesAsync();

        _logger.LogInformation($"[Notifications] Notificarea #{id} marcată ca citită de utilizatorul {CurrentUserId}.");

        return Ok(NotificationResponse.FromNotification(notification));
    }

    // PUT /api/notifications/read-all
    [HttpPut("read-all")]
    public async Task<IActionResult> MarkAllAsRead()
    {
        var unread = await _db.Notifications
            .Where(n => n.UserId == CurrentUserId && !n.IsRead)
            .ToListAsync();

        if (unread.Count == 0)
            return Ok(new { markedCount = 0 });

        foreach (var n in unread)
            n.IsRead = true;

        await _db.SaveChangesAsync();

        _logger.LogInformation($"[Notifications] {unread.Count} notificări marcate ca citite pentru utilizatorul {CurrentUserId}.");

        return Ok(new { markedCount = unread.Count });
    }

    // DELETE /api/notifications/{id}
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete(int id)
    {
        var notification = await _db.Notifications
            .FirstOrDefaultAsync(n => n.Id == id && n.UserId == CurrentUserId);

        if (notification == null)
            return NotFound(new { message = $"Notificarea #{id} nu a fost găsită." });

        _db.Notifications.Remove(notification);
        await _db.SaveChangesAsync();

        _logger.LogInformation($"[Notifications] Notificarea #{id} ștearsă de utilizatorul {CurrentUserId}.");

        return NoContent();
    }
}
