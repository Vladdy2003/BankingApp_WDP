namespace BankingApp.Services;

public class EmailTemplateService : IEmailTemplateService
{
    public string TransactionEmail(string firstName, string transactionType, decimal amount, string currency, int txId, DateTime? processedAt)
    {
        var processedAtStr = processedAt.HasValue
            ? processedAt.Value.ToString("dd.MM.yyyy HH:mm") + " UTC"
            : "—";

        return $@"<!DOCTYPE html>
<html lang=""ro"">
<head><meta charset=""UTF-8""><title>Confirmare tranzacție</title></head>
<body style=""margin:0;padding:0;background:#f4f6f9;font-family:Arial,sans-serif;"">
  <table width=""100%"" cellpadding=""0"" cellspacing=""0"" style=""background:#f4f6f9;padding:40px 0;"">
    <tr><td align=""center"">
      <table width=""600"" cellpadding=""0"" cellspacing=""0"" style=""background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);"">

        <tr style=""background:#1a56db;"">
          <td style=""padding:28px 40px;"">
            <h1 style=""margin:0;color:#ffffff;font-size:22px;"">🏦 BankingApp</h1>
          </td>
        </tr>

        <tr><td style=""padding:36px 40px;"">
          <h2 style=""margin:0 0 8px;color:#111827;font-size:20px;"">Tranzacție procesată cu succes</h2>
          <p style=""margin:0 0 24px;color:#6b7280;font-size:14px;"">Bună, {firstName}!</p>

          <table width=""100%"" cellpadding=""0"" cellspacing=""0"" style=""background:#f9fafb;border-radius:6px;padding:0;"">
            <tr>
              <td style=""padding:16px 20px;border-bottom:1px solid #e5e7eb;"">
                <span style=""color:#6b7280;font-size:13px;"">Tip tranzacție</span><br>
                <strong style=""color:#111827;font-size:15px;"">{transactionType}</strong>
              </td>
            </tr>
            <tr>
              <td style=""padding:16px 20px;border-bottom:1px solid #e5e7eb;"">
                <span style=""color:#6b7280;font-size:13px;"">Sumă</span><br>
                <strong style=""color:#1a56db;font-size:18px;"">{amount:N2} {currency}</strong>
              </td>
            </tr>
            <tr>
              <td style=""padding:16px 20px;border-bottom:1px solid #e5e7eb;"">
                <span style=""color:#6b7280;font-size:13px;"">ID tranzacție</span><br>
                <strong style=""color:#111827;font-size:15px;"">#{txId}</strong>
              </td>
            </tr>
            <tr>
              <td style=""padding:16px 20px;"">
                <span style=""color:#6b7280;font-size:13px;"">Procesată la</span><br>
                <strong style=""color:#111827;font-size:15px;"">{processedAtStr}</strong>
              </td>
            </tr>
          </table>

          <p style=""margin:24px 0 0;color:#6b7280;font-size:13px;"">
            Dacă nu ai autorizat această operațiune, contactează-ne imediat la <a href=""mailto:support@bankingapp.md"" style=""color:#1a56db;"">support@bankingapp.md</a>.
          </p>
        </td></tr>

        <tr style=""background:#f9fafb;"">
          <td style=""padding:20px 40px;text-align:center;"">
            <p style=""margin:0;color:#9ca3af;font-size:12px;"">© {DateTime.UtcNow.Year} BankingApp · Republica Moldova</p>
          </td>
        </tr>

      </table>
    </td></tr>
  </table>
</body>
</html>";
    }

    public string SecurityAlertEmail(string firstName, string action, string? ipAddress, DateTime timestamp)
    {
        var ipStr = string.IsNullOrEmpty(ipAddress) ? "necunoscut" : ipAddress;

        return $@"<!DOCTYPE html>
<html lang=""ro"">
<head><meta charset=""UTF-8""><title>Alertă de securitate</title></head>
<body style=""margin:0;padding:0;background:#f4f6f9;font-family:Arial,sans-serif;"">
  <table width=""100%"" cellpadding=""0"" cellspacing=""0"" style=""background:#f4f6f9;padding:40px 0;"">
    <tr><td align=""center"">
      <table width=""600"" cellpadding=""0"" cellspacing=""0"" style=""background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);"">

        <tr style=""background:#dc2626;"">
          <td style=""padding:28px 40px;"">
            <h1 style=""margin:0;color:#ffffff;font-size:22px;"">🏦 BankingApp — Alertă Securitate</h1>
          </td>
        </tr>

        <tr><td style=""padding:36px 40px;"">
          <h2 style=""margin:0 0 8px;color:#111827;font-size:20px;"">⚠️ Activitate detectată pe contul tău</h2>
          <p style=""margin:0 0 24px;color:#6b7280;font-size:14px;"">Bună, {firstName}!</p>

          <table width=""100%"" cellpadding=""0"" cellspacing=""0"" style=""background:#fef2f2;border:1px solid #fecaca;border-radius:6px;"">
            <tr>
              <td style=""padding:16px 20px;border-bottom:1px solid #fecaca;"">
                <span style=""color:#6b7280;font-size:13px;"">Acțiune detectată</span><br>
                <strong style=""color:#111827;font-size:15px;"">{action}</strong>
              </td>
            </tr>
            <tr>
              <td style=""padding:16px 20px;border-bottom:1px solid #fecaca;"">
                <span style=""color:#6b7280;font-size:13px;"">Adresă IP</span><br>
                <strong style=""color:#111827;font-size:15px;"">{ipStr}</strong>
              </td>
            </tr>
            <tr>
              <td style=""padding:16px 20px;"">
                <span style=""color:#6b7280;font-size:13px;"">Data și ora</span><br>
                <strong style=""color:#111827;font-size:15px;"">{timestamp:dd.MM.yyyy HH:mm} UTC</strong>
              </td>
            </tr>
          </table>

          <p style=""margin:24px 0 0;color:#6b7280;font-size:13px;"">
            Dacă nu ai efectuat tu această acțiune, contactează-ne imediat la
            <a href=""mailto:security@bankingapp.md"" style=""color:#dc2626;"">security@bankingapp.md</a>
            sau apelează <strong>+373 022 000 000</strong>.
          </p>
        </td></tr>

        <tr style=""background:#f9fafb;"">
          <td style=""padding:20px 40px;text-align:center;"">
            <p style=""margin:0;color:#9ca3af;font-size:12px;"">© {DateTime.UtcNow.Year} BankingApp · Republica Moldova</p>
          </td>
        </tr>

      </table>
    </td></tr>
  </table>
</body>
</html>";
    }

    public string WelcomeEmail(string firstName, string lastName, string email)
    {
        return $@"<!DOCTYPE html>
<html lang=""ro"">
<head><meta charset=""UTF-8""><title>Bun venit la BankingApp</title></head>
<body style=""margin:0;padding:0;background:#f4f6f9;font-family:Arial,sans-serif;"">
  <table width=""100%"" cellpadding=""0"" cellspacing=""0"" style=""background:#f4f6f9;padding:40px 0;"">
    <tr><td align=""center"">
      <table width=""600"" cellpadding=""0"" cellspacing=""0"" style=""background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.08);"">

        <tr style=""background:#1a56db;"">
          <td style=""padding:28px 40px;"">
            <h1 style=""margin:0;color:#ffffff;font-size:22px;"">🏦 BankingApp</h1>
          </td>
        </tr>

        <tr><td style=""padding:36px 40px;"">
          <h2 style=""margin:0 0 8px;color:#111827;font-size:20px;"">Bun venit, {firstName}! 🎉</h2>
          <p style=""margin:0 0 24px;color:#6b7280;font-size:14px;"">
            Contul tău BankingApp a fost creat cu succes. Ești acum parte din comunitatea noastră bancară.
          </p>

          <table width=""100%"" cellpadding=""0"" cellspacing=""0"" style=""background:#f9fafb;border-radius:6px;"">
            <tr>
              <td style=""padding:16px 20px;border-bottom:1px solid #e5e7eb;"">
                <span style=""color:#6b7280;font-size:13px;"">Nume complet</span><br>
                <strong style=""color:#111827;font-size:15px;"">{firstName} {lastName}</strong>
              </td>
            </tr>
            <tr>
              <td style=""padding:16px 20px;"">
                <span style=""color:#6b7280;font-size:13px;"">Email</span><br>
                <strong style=""color:#111827;font-size:15px;"">{email}</strong>
              </td>
            </tr>
          </table>

          <h3 style=""margin:28px 0 12px;color:#111827;font-size:16px;"">Ce poți face cu BankingApp:</h3>
          <ul style=""margin:0;padding-left:20px;color:#374151;font-size:14px;line-height:1.8;"">
            <li>Deschide conturi bancare (Curent, Economii, Business)</li>
            <li>Efectuează depozite, retrageri și transferuri</li>
            <li>Emite și gestionează carduri bancare</li>
            <li>Vizualizează extrase și rapoarte financiare</li>
          </ul>

          <p style=""margin:28px 0 0;color:#6b7280;font-size:13px;"">
            Ai nevoie de ajutor? Scrie-ne la <a href=""mailto:support@bankingapp.md"" style=""color:#1a56db;"">support@bankingapp.md</a>.
          </p>
        </td></tr>

        <tr style=""background:#f9fafb;"">
          <td style=""padding:20px 40px;text-align:center;"">
            <p style=""margin:0;color:#9ca3af;font-size:12px;"">© {DateTime.UtcNow.Year} BankingApp · Republica Moldova</p>
          </td>
        </tr>

      </table>
    </td></tr>
  </table>
</body>
</html>";
    }
}
