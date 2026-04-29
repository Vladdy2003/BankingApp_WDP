namespace BankingApp.Patterns.Creational.Singleton;

/// <summary>
/// Singleton #2 — expune setările globale ale aplicației (JWT, conexiune, limite) într-un singur loc.
/// Înregistrat ca AddSingleton în Program.cs; toate serviciile îl injectează în loc să citească IConfiguration direct.
/// </summary>
public class AppConfigurationManager
{
    public JwtConfig Jwt { get; }
    public DatabaseConfig Database { get; }
    public BankingLimitsConfig BankingLimits { get; }

    public AppConfigurationManager(IConfiguration configuration)
    {
        Jwt = new JwtConfig
        {
            SecretKey = configuration["JwtSettings:SecretKey"] ?? throw new InvalidOperationException("JwtSettings:SecretKey is missing"),
            Issuer = configuration["JwtSettings:Issuer"] ?? "BankingApp.API",
            Audience = configuration["JwtSettings:Audience"] ?? "BankingApp.Client",
            ExpirationMinutes = int.Parse(configuration["JwtSettings:ExpirationMinutes"] ?? "60"),
            RefreshTokenExpirationDays = int.Parse(configuration["JwtSettings:RefreshTokenExpirationDays"] ?? "7")
        };

        Database = new DatabaseConfig
        {
            ConnectionString = configuration.GetConnectionString("DefaultConnection") ?? throw new InvalidOperationException("DefaultConnection is missing")
        };

        BankingLimits = new BankingLimitsConfig
        {
            DailyTransactionLimit = decimal.Parse(configuration["BankingLimits:DailyTransactionLimit"] ?? "10000"),
            MaxTransferAmount = decimal.Parse(configuration["BankingLimits:MaxTransferAmount"] ?? "50000"),
            FraudDetectionThreshold = decimal.Parse(configuration["BankingLimits:FraudDetectionThreshold"] ?? "5000")
        };
    }

    public record JwtConfig
    {
        public required string SecretKey { get; init; }
        public required string Issuer { get; init; }
        public required string Audience { get; init; }
        public int ExpirationMinutes { get; init; }
        public int RefreshTokenExpirationDays { get; init; }
    }

    public record DatabaseConfig
    {
        public required string ConnectionString { get; init; }
    }

    public record BankingLimitsConfig
    {
        public decimal DailyTransactionLimit { get; init; }
        public decimal MaxTransferAmount { get; init; }
        public decimal FraudDetectionThreshold { get; init; }
    }
}
