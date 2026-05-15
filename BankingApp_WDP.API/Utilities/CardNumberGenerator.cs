namespace BankingApp.Utilities;

public interface ICardNumberGenerator
{
    string Generate();
    bool Validate(string cardNumber);
}

public class CardNumberGenerator : ICardNumberGenerator
{
    // Generates a Luhn-valid 16-digit card number formatted as XXXX XXXX XXXX XXXX
    public string Generate()
    {
        var digits = new int[15];
        for (int i = 0; i < 15; i++)
            digits[i] = Random.Shared.Next(0, 10);

        int checkDigit = ComputeLuhnCheckDigit(digits);
        var all = digits.Append(checkDigit).ToArray();
        return string.Join(" ", Enumerable.Range(0, 4).Select(g => string.Concat(all.Skip(g * 4).Take(4))));
    }

    public bool Validate(string cardNumber)
    {
        var digits = cardNumber.Replace(" ", "")
                               .Where(char.IsDigit)
                               .Select(c => c - '0')
                               .ToArray();

        if (digits.Length != 16) return false;

        int sum = 0;
        for (int i = digits.Length - 1; i >= 0; i--)
        {
            int d = digits[i];
            if ((digits.Length - 1 - i) % 2 == 1) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
        }
        return sum % 10 == 0;
    }

    private static int ComputeLuhnCheckDigit(int[] digits)
    {
        int sum = 0;
        for (int i = digits.Length - 1; i >= 0; i--)
        {
            int d = digits[i];
            if ((digits.Length - i) % 2 == 1) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
        }
        return (10 - (sum % 10)) % 10;
    }
}
