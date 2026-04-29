using BankingApp.Models;

namespace BankingApp.Patterns.Structural.Decorator;

/// <summary>
/// Decorator Pattern #8 — Componenta de bază pentru procesarea tranzacțiilor.
/// Decoratorii înlănțuiți adaugă logging, comisioane și notificări transparent.
/// </summary>
public interface ITransactionProcessor
{
    Task<Transaction> ProcessAsync(Transaction transaction);
}
