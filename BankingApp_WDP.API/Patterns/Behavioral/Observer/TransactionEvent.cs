using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.Observer;

public class TransactionEvent
{
    public Transaction Transaction { get; init; } = null!;
    public string? InitiatorUserId { get; init; }
    public DateTime OccurredAt { get; init; } = DateTime.UtcNow;
    public string? IpAddress { get; init; }
    public string? UserAgent { get; init; }
}
