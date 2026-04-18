using System.ComponentModel.DataAnnotations;

namespace BankingApp.DTOs.Auth;

public class RefreshTokenRequest
{
    [Required]
    public string RefreshToken { get; set; } = string.Empty;
}
