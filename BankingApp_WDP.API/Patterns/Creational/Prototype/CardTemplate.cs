using BankingApp.Models;

namespace BankingApp.Patterns.Creational.Prototype;

public abstract class CardTemplate : ICloneable
{
    public CardType CardType { get; protected set; }
    public decimal DailyLimit { get; protected set; }
    public decimal MonthlyLimit { get; protected set; }
    public List<string> Benefits { get; protected set; } = new();
    public List<string> AllowedTransactionTypes { get; protected set; } = new();

    public object Clone() => MemberwiseClone() is CardTemplate clone
        ? clone.DeepCopy()
        : throw new InvalidOperationException("Clone failed.");

    protected virtual CardTemplate DeepCopy()
    {
        var clone = (CardTemplate)MemberwiseClone();
        clone.Benefits = new List<string>(Benefits);
        clone.AllowedTransactionTypes = new List<string>(AllowedTransactionTypes);
        return clone;
    }

    public CardTemplate CloneTemplate() => (CardTemplate)Clone();
}
