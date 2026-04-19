namespace BankingApp.Utilities;

public interface IIBANGenerator
{
    string Generate();
}

public class IBANGenerator : IIBANGenerator
{
    private const string CountryCode = "MD";
    // 2-letter bank identifier used in Moldovan IBANs (MD: 2 alpha + 18 digits = 20-char BBAN)
    private const string BankCode = "BA";

    public string Generate()
    {
        var accountNumber = GenerateAccountNumber();
        var checkDigits = CalculateCheckDigits(accountNumber);
        return $"{CountryCode}{checkDigits}{BankCode}{accountNumber}";
    }

    private static string GenerateAccountNumber()
    {
        // 18 digits: MD IBAN = MD(2) + check(2) + bank(2) + account(18) = 24 chars
        return Random.Shared.NextInt64(100000000000000000L, 999999999999999999L).ToString();
    }

    private static string CalculateCheckDigits(string accountNumber)
    {
        // MOD 97-10 algorithm (ISO 7064): rearrange as BBAN + CC + "00", convert letters, compute 98 - (mod 97)
        var rearranged = $"{BankCode}{accountNumber}{CountryCode}00";
        var numeric = string.Concat(rearranged.Select(c => char.IsLetter(c) ? (c - 'A' + 10).ToString() : c.ToString()));

        var remainder = 0;
        foreach (var ch in numeric)
            remainder = (remainder * 10 + (ch - '0')) % 97;

        return (98 - remainder).ToString("D2");
    }
}
