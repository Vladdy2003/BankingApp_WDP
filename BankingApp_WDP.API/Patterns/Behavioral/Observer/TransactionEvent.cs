using BankingApp.Models;

namespace BankingApp.Patterns.Behavioral.Observer;

public class TransactionEvent
{
    public Transaction Transaction { get; init; } = null!;
    public string? InitiatorUserId { get; init; }
    public DateTime OccurredAt { get; init; } = DateTime.UtcNow;
}
