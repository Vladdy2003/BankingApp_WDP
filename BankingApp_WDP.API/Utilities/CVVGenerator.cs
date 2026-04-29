namespace BankingApp.Utilities;

public interface ICVVGenerator
{
    string GenerateHash();
    bool Verify(string plainCvv, string hash);
}

public class CVVGenerator : ICVVGenerator
{
    // Generates a random 3-digit CVV and returns its bcrypt hash (plaintext is never stored)
    public string GenerateHash()
    {
        var cvv = Random.Shared.Next(100, 1000).ToString();
        return BCrypt.Net.BCrypt.HashPassword(cvv, workFactor: 11);
    }

    public bool Verify(string plainCvv, string hash)
        => BCrypt.Net.BCrypt.Verify(plainCvv, hash);
}
