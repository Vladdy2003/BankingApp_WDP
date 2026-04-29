using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
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

    public AuthService(
        UserManager<ApplicationUser> userManager,
        IConfiguration configuration,
        INotificationFactory notificationFactory,
        IEmailTemplateService emailTemplates)
    {
        _userManager = userManager;
        _configuration = configuration;
        _notificationFactory = notificationFactory;
        _emailTemplates = emailTemplates;
    }

    public async Task<AuthResponse> RegisterAsync(RegisterRequest request)
    {
        var existingUser = await _userManager.FindByEmailAsync(request.Email);
        if (existingUser is not null)
            throw new InvalidOperationException("Un utilizator cu acest email există deja.");

        var user = new ApplicationUser
        {
            UserName = request.Email,
            Email = request.Email,
            FirstName = request.FirstName,
            LastName = request.LastName,
            PhoneNumber = request.PhoneNumber,
            Address = request.Address,
            DateOfBirth = request.DateOfBirth,
            CNP = request.CNP
        };

        var result = await _userManager.CreateAsync(user, request.Password);
        if (!result.Succeeded)
        {
            var errors = string.Join("; ", result.Errors.Select(e => e.Description));
            throw new InvalidOperationException($"Înregistrare eșuată: {errors}");
        }

        var email = _notificationFactory.CreateEmailNotification();
        var htmlBody = _emailTemplates.WelcomeEmail(user.FirstName, user.LastName, user.Email!);
        await email.SendAsync(user.Email!, "Bun venit la BankingApp! 🏦", htmlBody);

        return await GenerateAuthResponseAsync(user);
    }

    public async Task<AuthResponse> LoginAsync(LoginRequest request)
    {
        var user = await _userManager.FindByEmailAsync(request.Email)
            ?? throw new UnauthorizedAccessException("Email sau parolă incorectă.");

        var isPasswordValid = await _userManager.CheckPasswordAsync(user, request.Password);
        if (!isPasswordValid)
            throw new UnauthorizedAccessException("Email sau parolă incorectă.");

        return await GenerateAuthResponseAsync(user);
    }

    public async Task<AuthResponse> RefreshTokenAsync(string refreshToken)
    {
        var user = _userManager.Users.SingleOrDefault(u =>
            u.RefreshToken == refreshToken && u.RefreshTokenExpiry > DateTime.UtcNow)
            ?? throw new UnauthorizedAccessException("Refresh token invalid sau expirat.");

        return await GenerateAuthResponseAsync(user);
    }

    private async Task<AuthResponse> GenerateAuthResponseAsync(ApplicationUser user)
    {
        var jwtSettings = _configuration.GetSection("JwtSettings");
        var secretKey = jwtSettings["SecretKey"]!;
        var issuer = jwtSettings["Issuer"]!;
        var audience = jwtSettings["Audience"]!;
        var expirationMinutes = int.Parse(jwtSettings["ExpirationMinutes"]!);
        var refreshExpirationDays = int.Parse(jwtSettings["RefreshTokenExpirationDays"]!);

        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey));
        var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);
        var expiresAt = DateTime.UtcNow.AddMinutes(expirationMinutes);

        var claims = new List<Claim>
        {
            new(JwtRegisteredClaimNames.Sub, user.Id),
            new(JwtRegisteredClaimNames.Email, user.Email!),
            new(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            new("firstName", user.FirstName),
            new("lastName", user.LastName)
        };

        var roles = await _userManager.GetRolesAsync(user);
        claims.AddRange(roles.Select(role => new Claim(ClaimTypes.Role, role)));

        var token = new JwtSecurityToken(
            issuer: issuer,
            audience: audience,
            claims: claims,
            expires: expiresAt,
            signingCredentials: credentials);

        var accessToken = new JwtSecurityTokenHandler().WriteToken(token);
        var refreshToken = GenerateRefreshToken();

        user.RefreshToken = refreshToken;
        user.RefreshTokenExpiry = DateTime.UtcNow.AddDays(refreshExpirationDays);
        await _userManager.UpdateAsync(user);

        return new AuthResponse
        {
            AccessToken = accessToken,
            RefreshToken = refreshToken,
            ExpiresAt = expiresAt,
            UserId = user.Id,
            Email = user.Email!,
            FullName = $"{user.FirstName} {user.LastName}"
        };
    }

    private static string GenerateRefreshToken()
    {
        var bytes = RandomNumberGenerator.GetBytes(64);
        return Convert.ToBase64String(bytes);
    }
}
