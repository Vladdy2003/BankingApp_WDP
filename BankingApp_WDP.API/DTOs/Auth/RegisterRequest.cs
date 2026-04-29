using System.ComponentModel.DataAnnotations;

namespace BankingApp.DTOs.Auth;

public class RegisterRequest
{
    [Required, MaxLength(100)]
    public string FirstName { get; set; } = string.Empty;

    [Required, MaxLength(100)]
    public string LastName { get; set; } = string.Empty;

    [Required, EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required, MinLength(8)]
    public string Password { get; set; } = string.Empty;

    [Phone]
    public string? PhoneNumber { get; set; }

    [MaxLength(200)]
    public string? Address { get; set; }

    public DateTime? DateOfBirth { get; set; }

    [MaxLength(13)]
    public string? CNP { get; set; }
}
