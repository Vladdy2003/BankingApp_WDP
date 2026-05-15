using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using BankingApp.Data;
using BankingApp.DTOs.Auth;
using BankingApp.Models;
using BankingApp.Patterns.Creational.AbstractFactory;
using Microsoft.AspNetCore.Identity;
using Microsoft.IdentityModel.Tokens;

namespace BankingApp.Services;

public class AuthService : IAuthService
{
    private readonly UserManager<ApplicationUser> _userManager;
    private readonly IConfiguration _configuration;
    private readonly INotificationFactory _notificationFactory;
    private readonly IEmailTemplateService _emailTemplates;
    private readonly IAuditService _auditService;
    private readonly AppDbContext _db;
    private readonly IHttpContextAccessor _httpContextAccessor;

    public AuthService(
        UserManager<ApplicationUser> userManager,
        IConfiguration configuration,
        INotificationFactory notificationFactory,
        IEmailTemplateService emailTemplates,
        IAuditService auditService,
        AppDbContext db,
        IHttpContextAccessor httpContextAccessor)
    {
        _userManager         = userManager;
        _configuration       = configuration;
        _notificationFactory = notificationFactory;
        _emailTemplates      = emailTemplates;
        _auditService        = auditService;
        _db                  = db;
        _httpContextAccessor = httpContextAccessor;
    }

    // ── Register ──────────────────────────────────────────────────────────────

    public async Task<AuthResponse> RegisterAsync(RegisterRequest request)
    {
        var existingUser = await _userManager.FindByEmailAsync(request.Email);
        if (existingUser is not null)
            throw new InvalidOperationException("Un utilizator cu acest email există deja.");

        var user = new ApplicationUser
        {
            UserName    = request.Email,
            Email       = request.Email,
            FirstName   = request.FirstName,
            LastName    = request.LastName,
            PhoneNumber = request.PhoneNumber,
            Address     = request.Address,
            DateOfBirth = request.DateOfBirth,
            CNP         = request.CNP
        };

        var result = await _userManager.CreateAsync(user, request.Password);
        if (!result.Succeeded)
        {
            var errors = string.Join("; ", result.Errors.Select(e => e.Description));
            throw new InvalidOperationException($"Înregistrare eșuată: {errors}");
        }

        // Welcome email
        var emailNotif = _notificationFactory.CreateEmailNotification();
        var htmlBody   = _emailTemplates.WelcomeEmail(user.FirstName, user.LastName, user.Email!);
        await emailNotif.SendAsync(user.Email!, "Bun venit la BankingApp!", htmlBody);

        // Audit log
        await _auditService.LogAsync(
            action:     "User.Register",
            userId:     user.Id,
            entityType: "ApplicationUser",
            entityId:   user.Id,
            newValues:  $"{{\"email\":\"{user.Email}\"," +
                        $"\"firstName\":\"{user.FirstName}\"," +
                        $"\"lastName\":\"{user.LastName}\"}}",
            ipAddress:  GetClientIp(),
            userAgent:  GetUserAgent());

        return await GenerateAuthResponseAsync(user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    public async Task<AuthResponse> LoginAsync(LoginRequest request)
    {
        var user = await _userManager.FindByEmailAsync(request.Email)
            ?? throw new UnauthorizedAccessException("Email sau parolă incorectă.");

        var isPasswordValid = await _userManager.CheckPasswordAsync(user, request.Password);
        if (!isPasswordValid)
            throw new UnauthorizedAccessException("Email sau parolă incorectă.");

        var ipAddress = GetClientIp();
        var userAgent = GetUserAgent();
        var now       = DateTime.UtcNow;

        // 1. Audit log
        await _auditService.LogAsync(
            action:     "User.Login",
            userId:     user.Id,
            entityType: "ApplicationUser",
            entityId:   user.Id,
            newValues:  $"{{\"email\":\"{user.Email}\",\"ip\":\"{ipAddress ?? "unknown"}\"}}",
            ipAddress:  ipAddress,
            userAgent:  userAgent);

        // 2. In-app Security notification
        _db.Notifications.Add(new Notification
        {
            UserId    = user.Id,
            Title     = "Autentificare nouă detectată",
            Message   = $"Contul tău a fost accesat din " +
                        $"{ipAddress ?? "locație necunoscută"} " +
                        $"la {now:dd MMM yyyy, HH:mm} UTC. " +
                        $"Dacă nu ești tu, contactează-ne imediat.",
            Type      = NotificationType.Security,
            CreatedAt = now
        });
        await _db.SaveChangesAsync();

        // 3. Security alert email — fire-and-forget (nu blochează login-ul)
        _ = Task.Run(async () =>
        {
            try
            {
                var mail = _notificationFactory.CreateEmailNotification();
                var body = _emailTemplates.SecurityAlertEmail(
                    user.FirstName,
                    "Autentificare în cont",
                    ipAddress,
                    now);
                await mail.SendAsync(
                    user.Email!,
                    "Alertă de securitate — Autentificare nouă",
                    body);
            }
            catch { /* non-critical — nu eșua login-ul din cauza email-ului */ }
        });

        return await GenerateAuthResponseAsync(user);
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    public async Task<AuthResponse> RefreshTokenAsync(string refreshToken)
    {
        var user = _userManager.Users.SingleOrDefault(u =>
            u.RefreshToken == refreshToken && u.RefreshTokenExpiry > DateTime.UtcNow)
            ?? throw new UnauthorizedAccessException("Refresh token invalid sau expirat.");

        await _auditService.LogAsync(
            action:     "User.TokenRefresh",
            userId:     user.Id,
            entityType: "ApplicationUser",
            entityId:   user.Id,
            ipAddress:  GetClientIp(),
            userAgent:  GetUserAgent());

        return await GenerateAuthResponseAsync(user);
    }

    // ── Token generation ──────────────────────────────────────────────────────

    private async Task<AuthResponse> GenerateAuthResponseAsync(ApplicationUser user)
    {
        var jwtSettings          = _configuration.GetSection("JwtSettings");
        var secretKey            = jwtSettings["SecretKey"]!;
        var issuer               = jwtSettings["Issuer"]!;
        var audience             = jwtSettings["Audience"]!;
        var expirationMinutes    = int.Parse(jwtSettings["ExpirationMinutes"]!);
        var refreshExpirationDays = int.Parse(jwtSettings["RefreshTokenExpirationDays"]!);

        var key         = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey));
        var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
        var expiresAt   = DateTime.UtcNow.AddMinutes(expirationMinutes);

        var claims = new List<Claim>
        {
            new(JwtRegisteredClaimNames.Sub,   user.Id),
            new(JwtRegisteredClaimNames.Email, user.Email!),
            new(JwtRegisteredClaimNames.Jti,   Guid.NewGuid().ToString()),
            new("firstName", user.FirstName),
            new("lastName",  user.LastName)
        };

        var roles = await _userManager.GetRolesAsync(user);
        claims.AddRange(roles.Select(role => new Claim(ClaimTypes.Role, role)));

        var token = new JwtSecurityToken(
            issuer:             issuer,
            audience:           audience,
            claims:             claims,
            expires:            expiresAt,
            signingCredentials: credentials);

        var accessToken  = new JwtSecurityTokenHandler().WriteToken(token);
        var refreshToken = GenerateRefreshToken();

        user.RefreshToken       = refreshToken;
        user.RefreshTokenExpiry = DateTime.UtcNow.AddDays(refreshExpirationDays);
        await _userManager.UpdateAsync(user);

        return new AuthResponse
        {
            AccessToken  = accessToken,
            RefreshToken = refreshToken,
            ExpiresAt    = expiresAt,
            UserId       = user.Id,
            Email        = user.Email!,
            FullName     = $"{user.FirstName} {user.LastName}"
        };
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private string? GetClientIp() =>
        _httpContextAccessor.HttpContext?.Request.Headers["X-Forwarded-For"].FirstOrDefault()
        ?? _httpContextAccessor.HttpContext?.Connection.RemoteIpAddress?.ToString();

    private string? GetUserAgent() =>
        _httpContextAccessor.HttpContext?.Request.Headers["User-Agent"].FirstOrDefault();

    private static string GenerateRefreshToken()
    {
        var bytes = RandomNumberGenerator.GetBytes(64);
        return Convert.ToBase64String(bytes);
    }
}
