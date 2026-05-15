namespace BankingApp.Services;

public class EmailTemplateService : IEmailTemplateService
{
    // ── Shared layout constants ───────────────────────────────────────────────
    private const string GoogleFonts =
        "https://fonts.googleapis.com/css2?family=DM+Serif+Display:ital@0;1&family=DM+Sans:wght@300;400;500&family=DM+Mono:wght@400&display=swap";

    // Brand colors
    private const string BaBg       = "#F2EFE9"; // parchment background
    private const string BaSurface  = "#FFFFFF"; // card surface
    private const string BaSurface2 = "#EDEAE3"; // subtle surface / dividers
    private const string BaInk      = "#1A1814"; // primary text / header bg
    private const string BaInk2     = "#5C5850"; // secondary text
    private const string BaInk3     = "#9C9890"; // tertiary text, timestamps
    private const string BaGold     = "#B8965A"; // accent
    private const string BaGoldLight= "#EAD9B8"; // gold tint bg
    private const string BaGoldDark = "#7A5E2A"; // gold on light
    private const string BaSuccess  = "#3A7D5C"; // positive values
    private const string BaDanger   = "#B84040"; // negative / danger
    private const string BaBorder   = "rgba(26,24,20,0.12)";

    private static string HtmlShell(string preheader, string bodyContent) => $@"<!DOCTYPE html>
<html lang=""ro"">
<head>
  <meta charset=""UTF-8"">
  <meta name=""viewport"" content=""width=device-width,initial-scale=1"">
  <title>BankingApp</title>
  <link href=""{GoogleFonts}"" rel=""stylesheet"">
  <style>
    body {{ margin:0; padding:0; background:{BaBg}; }}
    @media only screen and (max-width:620px) {{
      .email-wrapper {{ padding: 0 !important; }}
      .email-card    {{ border-radius: 0 !important; border-left: none !important; border-right: none !important; }}
    }}
  </style>
</head>
<body style=""margin:0;padding:0;background:{BaBg};font-family:'DM Sans',system-ui,sans-serif;"">

  <!-- Preheader (hidden preview text) -->
  <div style=""display:none;max-height:0;overflow:hidden;mso-hide:all;"">
    {preheader}&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌&nbsp;‌
  </div>

  <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"" class=""email-wrapper""
         style=""background:{BaBg};padding:40px 16px;"">
    <tr><td align=""center"">
      <table width=""600"" cellpadding=""0"" cellspacing=""0"" role=""presentation"" class=""email-card""
             style=""background:{BaSurface};border-radius:16px;border:0.5px solid {BaBorder};overflow:hidden;"">

        {bodyContent}

        <!-- Footer -->
        <tr>
          <td style=""background:{BaBg};padding:24px 40px;border-top:0.5px solid {BaBorder};"">
            <p style=""margin:0 0 6px;font-family:'DM Sans',system-ui,sans-serif;font-size:10px;font-weight:500;
                       letter-spacing:0.12em;text-transform:uppercase;color:{BaInk3};"">
              BANKINGAPP · REPUBLICA MOLDOVA
            </p>
            <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;font-size:11px;font-weight:300;
                       color:{BaInk3};line-height:1.6;"">
              Ai întrebări? Scrie-ne la
              <a href=""mailto:support@bankingapp.md"" style=""color:{BaGold};text-decoration:none;"">support@bankingapp.md</a>.
              Acest email a fost generat automat — te rugăm să nu răspunzi direct.
            </p>
            <p style=""margin:8px 0 0;font-family:'DM Sans',system-ui,sans-serif;font-size:10px;font-weight:300;
                       color:{BaInk3};letter-spacing:0.03em;"">
              © {DateTime.UtcNow.Year} BankingApp. Toate drepturile rezervate.
            </p>
          </td>
        </tr>

      </table>
    </td></tr>
  </table>

</body>
</html>";

    // ── TransactionEmail ──────────────────────────────────────────────────────
    public string TransactionEmail(
        string firstName, string transactionType,
        decimal amount, string currency,
        int txId, DateTime? processedAt)
    {
        var processedStr = processedAt.HasValue
            ? processedAt.Value.ToString("dd MMM yyyy, HH:mm") + " UTC"
            : "—";

        var typeLabel = transactionType switch
        {
            "Deposit"    => "Depozit",
            "Withdrawal" => "Retragere",
            "Transfer"   => "Transfer",
            "Payment"    => "Plată",
            _            => transactionType
        };

        var amountColor = transactionType switch
        {
            "Deposit" => BaSuccess,
            "Withdrawal" or "Payment" => BaDanger,
            _ => BaInk
        };

        var amountPrefix = transactionType switch
        {
            "Deposit"  => "+",
            "Withdrawal" or "Payment" => "−",
            _ => ""
        };

        var body = $@"
        <!-- Header -->
        <tr>
          <td style=""background:{BaInk};padding:28px 40px 24px;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">
              <tr>
                <td>
                  <p style=""margin:0 0 2px;font-family:'DM Sans',system-ui,sans-serif;font-size:10px;
                             font-weight:500;letter-spacing:0.14em;text-transform:uppercase;color:{BaGold};"">
                    BANKINGAPP
                  </p>
                  <p style=""margin:0;font-family:'DM Serif Display',Georgia,serif;font-size:22px;
                             font-weight:400;color:{BaBg};letter-spacing:-0.01em;"">
                    Tranzacție procesată
                  </p>
                </td>
                <td align=""right"" valign=""middle"">
                  <span style=""display:inline-block;background:{BaGoldLight};color:{BaGoldDark};
                                padding:4px 12px;border-radius:999px;
                                font-family:'DM Sans',system-ui,sans-serif;
                                font-size:11px;font-weight:500;letter-spacing:0.04em;"">
                    {typeLabel.ToUpper()}
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Greeting + Amount hero -->
        <tr>
          <td style=""padding:32px 40px 0;"">
            <p style=""margin:0 0 24px;font-family:'DM Sans',system-ui,sans-serif;
                       font-size:14px;font-weight:400;color:{BaInk2};line-height:1.6;"">
              Bună, {firstName}. Tranzacția ta a fost procesată cu succes.
            </p>

            <!-- Amount card -->
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""background:{BaBg};border-radius:12px;border:0.5px solid {BaBorder};"">
              <tr>
                <td style=""padding:20px 24px;"">
                  <p style=""margin:0 0 4px;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.12em;
                             text-transform:uppercase;color:{BaInk3};"">
                    SUMĂ {typeLabel.ToUpper()}
                  </p>
                  <p style=""margin:0;font-family:'DM Serif Display',Georgia,serif;
                             font-size:36px;font-weight:400;font-style:italic;
                             color:{amountColor};letter-spacing:-0.02em;line-height:1.1;"">
                    {amountPrefix}{amount:N2}
                    <span style=""font-size:20px;font-style:normal;color:{BaInk2};""> {currency}</span>
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Details rows -->
        <tr>
          <td style=""padding:24px 40px 32px;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">TIP TRANZACȚIE</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;color:{BaInk};"">
                    {typeLabel}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">ID TRANZACȚIE</p>
                  <p style=""margin:4px 0 0;font-family:'DM Mono','Courier New',monospace;
                             font-size:13px;font-weight:400;color:{BaInk};"">
                    #{txId:D6}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">DATA PROCESĂRII</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:400;color:{BaInk};"">
                    {processedStr}
                  </p>
                </td>
              </tr>

            </table>

            <!-- Security note -->
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""margin-top:24px;background:{BaGoldLight};border-radius:10px;
                           border:0.5px solid {BaGold};"">
              <tr>
                <td style=""padding:14px 18px;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:12px;font-weight:400;color:{BaGoldDark};line-height:1.6;"">
                    Dacă nu ai autorizat această operațiune, contactează-ne imediat la
                    <a href=""mailto:security@bankingapp.md""
                       style=""color:{BaGoldDark};font-weight:500;text-decoration:underline;"">
                      security@bankingapp.md
                    </a>.
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>";

        return HtmlShell($"Tranzacție {typeLabel} {amountPrefix}{amount:N2} {currency} procesată cu succes.", body);
    }

    // ── SecurityAlertEmail ────────────────────────────────────────────────────
    public string SecurityAlertEmail(
        string firstName, string action,
        string? ipAddress, DateTime timestamp)
    {
        var ipStr = string.IsNullOrEmpty(ipAddress) ? "necunoscută" : ipAddress;

        var body = $@"
        <!-- Header -->
        <tr>
          <td style=""background:{BaInk};padding:28px 40px 24px;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">
              <tr>
                <td>
                  <p style=""margin:0 0 2px;font-family:'DM Sans',system-ui,sans-serif;font-size:10px;
                             font-weight:500;letter-spacing:0.14em;text-transform:uppercase;color:{BaGold};"">
                    BANKINGAPP
                  </p>
                  <p style=""margin:0;font-family:'DM Serif Display',Georgia,serif;font-size:22px;
                             font-weight:400;color:{BaBg};letter-spacing:-0.01em;"">
                    Alertă de securitate
                  </p>
                </td>
                <td align=""right"" valign=""middle"">
                  <span style=""display:inline-block;background:{BaDanger};color:#fff;
                                padding:4px 12px;border-radius:999px;
                                font-family:'DM Sans',system-ui,sans-serif;
                                font-size:11px;font-weight:500;letter-spacing:0.04em;"">
                    ACȚIUNE NECESARĂ
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Alert banner -->
        <tr>
          <td style=""padding:0;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""background:#FDF2F2;border-bottom:2px solid {BaDanger};"">
              <tr>
                <td style=""padding:16px 40px;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:13px;font-weight:500;color:{BaDanger};line-height:1.5;"">
                    ⚠️ &nbsp;Am detectat activitate neobișnuită pe contul tău. Verifică detaliile de mai jos.
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Body -->
        <tr>
          <td style=""padding:32px 40px;"">
            <p style=""margin:0 0 24px;font-family:'DM Sans',system-ui,sans-serif;
                       font-size:14px;font-weight:400;color:{BaInk2};line-height:1.6;"">
              Bună, {firstName}. O acțiune a fost efectuată pe contul tău. Dacă nu ai fost tu, contactează-ne imediat.
            </p>

            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">ACȚIUNE DETECTATĂ</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;color:{BaInk};"">
                    {action}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">ADRESĂ IP</p>
                  <p style=""margin:4px 0 0;font-family:'DM Mono','Courier New',monospace;
                             font-size:13px;font-weight:400;color:{BaInk};"">
                    {ipStr}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">DATA ȘI ORA</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:400;color:{BaInk};"">
                    {timestamp:dd MMM yyyy, HH:mm} UTC
                  </p>
                </td>
              </tr>

            </table>

            <!-- CTA -->
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""margin-top:28px;"">
              <tr>
                <td align=""center"">
                  <a href=""mailto:security@bankingapp.md""
                     style=""display:inline-block;background:{BaInk};color:{BaBg};
                             font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;text-decoration:none;
                             padding:14px 32px;border-radius:12px;letter-spacing:0.01em;"">
                    Contactează echipa de securitate
                  </a>
                </td>
              </tr>
              <tr>
                <td align=""center"" style=""padding-top:12px;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:11px;font-weight:300;color:{BaInk3};"">
                    sau apelează <strong style=""color:{BaInk2};"">+373 022 000 000</strong> non-stop
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>";

        return HtmlShell("Alertă de securitate pe contul tău BankingApp — verifică imediat.", body);
    }

    // ── WelcomeEmail ──────────────────────────────────────────────────────────
    public string WelcomeEmail(string firstName, string lastName, string email)
    {
        var body = $@"
        <!-- Header -->
        <tr>
          <td style=""background:{BaInk};padding:32px 40px 28px;"">
            <p style=""margin:0 0 2px;font-family:'DM Sans',system-ui,sans-serif;font-size:10px;
                       font-weight:500;letter-spacing:0.14em;text-transform:uppercase;color:{BaGold};"">
              BANKINGAPP
            </p>
            <p style=""margin:0 0 8px;font-family:'DM Serif Display',Georgia,serif;font-size:28px;
                       font-weight:400;color:{BaBg};letter-spacing:-0.01em;line-height:1.2;"">
              Bun venit, {firstName}.
            </p>
            <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;font-size:14px;
                       font-weight:300;color:{BaInk3};line-height:1.6;"">
              Contul tău a fost creat cu succes.
            </p>
          </td>
        </tr>

        <!-- Gold accent bar -->
        <tr>
          <td style=""background:{BaGold};padding:3px 0;""></td>
        </tr>

        <!-- Body -->
        <tr>
          <td style=""padding:32px 40px 0;"">
            <p style=""margin:0 0 24px;font-family:'DM Sans',system-ui,sans-serif;
                       font-size:14px;font-weight:400;color:{BaInk2};line-height:1.6;"">
              Ești acum parte din comunitatea BankingApp. Mai jos găsești detaliile contului tău.
            </p>

            <!-- Account details card -->
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""background:{BaBg};border-radius:12px;border:0.5px solid {BaBorder};"">
              <tr>
                <td style=""padding:20px 24px;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0 0 4px;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">NUME COMPLET</p>
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:15px;font-weight:500;color:{BaInk};"">
                    {firstName} {lastName}
                  </p>
                </td>
              </tr>
              <tr>
                <td style=""padding:20px 24px;"">
                  <p style=""margin:0 0 4px;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">ADRESĂ EMAIL</p>
                  <p style=""margin:0;font-family:'DM Mono','Courier New',monospace;
                             font-size:13px;font-weight:400;color:{BaInk};"">
                    {email}
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- What you can do -->
        <tr>
          <td style=""padding:28px 40px 0;"">
            <p style=""margin:0 0 14px;font-family:'DM Sans',system-ui,sans-serif;
                       font-size:10px;font-weight:500;letter-spacing:0.12em;
                       text-transform:uppercase;color:{BaInk3};"">
              CE POȚI FACE CU BANKINGAPP
            </p>

            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">
              {WelcomeFeatureRow("Conturi bancare", "Deschide conturi Curent, Economii sau Business.")}
              {WelcomeFeatureRow("Tranzacții", "Depozite, retrageri și transferuri în timp real.")}
              {WelcomeFeatureRow("Carduri", "Emite și gestionează carduri bancare virtuale și fizice.")}
              {WelcomeFeatureRow("Rapoarte", "Vizualizează extrase și analize financiare detaliate.")}
            </table>
          </td>
        </tr>

        <!-- CTA -->
        <tr>
          <td style=""padding:28px 40px 36px;"">
            <table cellpadding=""0"" cellspacing=""0"" role=""presentation"">
              <tr>
                <td style=""background:{BaInk};border-radius:12px;"">
                  <a href=""bankingapp://login""
                     style=""display:inline-block;padding:14px 32px;
                             font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;color:{BaBg};
                             text-decoration:none;letter-spacing:0.01em;"">
                    Deschide BankingApp
                  </a>
                </td>
              </tr>
            </table>
          </td>
        </tr>";

        return HtmlShell($"Bun venit, {firstName}! Contul tău BankingApp a fost creat.", body);
    }

    private static string WelcomeFeatureRow(string title, string description) => $@"
      <tr>
        <td style=""padding:10px 0;border-bottom:0.5px solid {BaBorder};"">
          <table cellpadding=""0"" cellspacing=""0"" role=""presentation"">
            <tr>
              <td style=""padding-right:14px;vertical-align:top;"">
                <div style=""width:6px;height:6px;border-radius:999px;
                             background:{BaGold};margin-top:5px;""></div>
              </td>
              <td>
                <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                           font-size:13px;font-weight:500;color:{BaInk};"">
                  {title}
                </p>
                <p style=""margin:2px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                           font-size:12px;font-weight:400;color:{BaInk2};line-height:1.5;"">
                  {description}
                </p>
              </td>
            </tr>
          </table>
        </td>
      </tr>";

    // ── AccountCreatedEmail ───────────────────────────────────────────────────
    public string AccountCreatedEmail(
        string firstName, string accountType, string iban,
        string currency, int accountId, DateTime createdAt)
    {
        var typeLabel = accountType switch
        {
            "Current"  => "Curent",
            "Savings"  => "Economii",
            "Business" => "Business",
            _          => accountType
        };

        var body = $@"
        <!-- Header -->
        <tr>
          <td style=""background:{BaInk};padding:32px 40px 28px;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">
              <tr>
                <td>
                  <p style=""margin:0 0 2px;font-family:'DM Sans',system-ui,sans-serif;font-size:10px;
                             font-weight:500;letter-spacing:0.14em;text-transform:uppercase;color:{BaGold};"">
                    BANKINGAPP
                  </p>
                  <p style=""margin:0 0 6px;font-family:'DM Serif Display',Georgia,serif;font-size:24px;
                             font-weight:400;color:{BaBg};letter-spacing:-0.01em;line-height:1.2;"">
                    Cont bancar deschis
                  </p>
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;font-size:13px;
                             font-weight:300;color:{BaInk3};line-height:1.6;"">
                    Bună, {firstName}. Noul tău cont este activ.
                  </p>
                </td>
                <td align=""right"" valign=""top"">
                  <span style=""display:inline-block;background:{BaGoldLight};color:{BaGoldDark};
                                padding:4px 12px;border-radius:999px;
                                font-family:'DM Sans',system-ui,sans-serif;
                                font-size:11px;font-weight:500;letter-spacing:0.04em;"">
                    {typeLabel.ToUpper()}
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Gold accent bar -->
        <tr>
          <td style=""background:{BaGold};padding:3px 0;""></td>
        </tr>

        <!-- IBAN hero -->
        <tr>
          <td style=""padding:32px 40px 0;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""background:{BaBg};border-radius:12px;border:0.5px solid {BaBorder};"">
              <tr>
                <td style=""padding:22px 24px;"">
                  <p style=""margin:0 0 4px;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.12em;
                             text-transform:uppercase;color:{BaInk3};"">
                    IBAN
                  </p>
                  <p style=""margin:0;font-family:'DM Mono','Courier New',monospace;
                             font-size:18px;font-weight:400;color:{BaInk};letter-spacing:0.06em;"">
                    {iban}
                  </p>
                  <p style=""margin:8px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:11px;font-weight:300;color:{BaInk3};"">
                    Folosește acest IBAN pentru a primi transferuri.
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Details rows -->
        <tr>
          <td style=""padding:24px 40px 0;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">TIP CONT</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;color:{BaInk};"">
                    {typeLabel}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">MONEDĂ</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;color:{BaInk};"">
                    {currency}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">ID CONT</p>
                  <p style=""margin:4px 0 0;font-family:'DM Mono','Courier New',monospace;
                             font-size:13px;font-weight:400;color:{BaInk};"">
                    #{accountId:D6}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">DATA DESCHIDERII</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:400;color:{BaInk};"">
                    {createdAt:dd MMM yyyy, HH:mm} UTC
                  </p>
                </td>
              </tr>

            </table>
          </td>
        </tr>

        <!-- Info note -->
        <tr>
          <td style=""padding:20px 40px 36px;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""background:{BaGoldLight};border-radius:10px;border:0.5px solid {BaGold};"">
              <tr>
                <td style=""padding:14px 18px;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:12px;font-weight:400;color:{BaGoldDark};line-height:1.6;"">
                    Contul tău este acum activ. Poți efectua depozite, retrageri și transferuri
                    direct din aplicația BankingApp.
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>";

        return HtmlShell($"Contul tău {typeLabel} a fost deschis — IBAN: {iban}", body);
    }

    // ── CardCreatedEmail ──────────────────────────────────────────────────────
    public string CardCreatedEmail(
        string firstName, string cardType, string maskedCardNumber,
        DateTime expiryDate, decimal dailyLimit, decimal monthlyLimit,
        string currency, DateTime createdAt)
    {
        var typeLabel = cardType switch
        {
            "Debit"   => "Debit",
            "Credit"  => "Credit",
            "Prepaid" => "Preplătit",
            _         => cardType
        };

        // Format expiry as MM/YY
        var expiryStr = expiryDate.ToString("MM/yy");

        var body = $@"
        <!-- Header -->
        <tr>
          <td style=""background:{BaInk};padding:32px 40px 28px;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">
              <tr>
                <td>
                  <p style=""margin:0 0 2px;font-family:'DM Sans',system-ui,sans-serif;font-size:10px;
                             font-weight:500;letter-spacing:0.14em;text-transform:uppercase;color:{BaGold};"">
                    BANKINGAPP
                  </p>
                  <p style=""margin:0 0 6px;font-family:'DM Serif Display',Georgia,serif;font-size:24px;
                             font-weight:400;color:{BaBg};letter-spacing:-0.01em;line-height:1.2;"">
                    Card bancar emis
                  </p>
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;font-size:13px;
                             font-weight:300;color:{BaInk3};line-height:1.6;"">
                    Bună, {firstName}. Noul tău card este gata de utilizare.
                  </p>
                </td>
                <td align=""right"" valign=""top"">
                  <span style=""display:inline-block;background:{BaGoldLight};color:{BaGoldDark};
                                padding:4px 12px;border-radius:999px;
                                font-family:'DM Sans',system-ui,sans-serif;
                                font-size:11px;font-weight:500;letter-spacing:0.04em;"">
                    {typeLabel.ToUpper()}
                  </span>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Gold accent bar -->
        <tr>
          <td style=""background:{BaGold};padding:3px 0;""></td>
        </tr>

        <!-- Card visual -->
        <tr>
          <td style=""padding:32px 40px 0;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""background:{BaInk};border-radius:16px;overflow:hidden;"">
              <tr>
                <td style=""padding:24px 28px 8px;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.14em;
                             text-transform:uppercase;color:{BaGold};"">
                    BANKINGAPP {typeLabel.ToUpper()}
                  </p>
                </td>
              </tr>
              <tr>
                <td style=""padding:8px 28px;"">
                  <p style=""margin:0;font-family:'DM Mono','Courier New',monospace;
                             font-size:20px;font-weight:400;color:{BaBg};letter-spacing:0.18em;"">
                    {maskedCardNumber}
                  </p>
                </td>
              </tr>
              <tr>
                <td style=""padding:8px 28px 24px;"">
                  <table cellpadding=""0"" cellspacing=""0"" role=""presentation"">
                    <tr>
                      <td style=""padding-right:32px;"">
                        <p style=""margin:0 0 2px;font-family:'DM Sans',system-ui,sans-serif;
                                   font-size:9px;font-weight:500;letter-spacing:0.1em;
                                   text-transform:uppercase;color:{BaInk3};"">EXPIRĂ</p>
                        <p style=""margin:0;font-family:'DM Mono','Courier New',monospace;
                                   font-size:14px;font-weight:400;color:{BaBg};"">
                          {expiryStr}
                        </p>
                      </td>
                      <td>
                        <p style=""margin:0 0 2px;font-family:'DM Sans',system-ui,sans-serif;
                                   font-size:9px;font-weight:500;letter-spacing:0.1em;
                                   text-transform:uppercase;color:{BaInk3};"">MONEDĂ</p>
                        <p style=""margin:0;font-family:'DM Mono','Courier New',monospace;
                                   font-size:14px;font-weight:400;color:{BaBg};"">
                          {currency}
                        </p>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>

        <!-- Details rows -->
        <tr>
          <td style=""padding:24px 40px 0;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation"">

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">TIP CARD</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;color:{BaInk};"">
                    {typeLabel}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">LIMITĂ ZILNICĂ</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;color:{BaInk};"">
                    {dailyLimit:N2} {currency}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;border-bottom:0.5px solid {BaBorder};"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">LIMITĂ LUNARĂ</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:500;color:{BaInk};"">
                    {monthlyLimit:N2} {currency}
                  </p>
                </td>
              </tr>

              <tr>
                <td style=""padding:12px 0;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:10px;font-weight:500;letter-spacing:0.08em;
                             text-transform:uppercase;color:{BaInk3};"">DATA EMITERII</p>
                  <p style=""margin:4px 0 0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:14px;font-weight:400;color:{BaInk};"">
                    {createdAt:dd MMM yyyy, HH:mm} UTC
                  </p>
                </td>
              </tr>

            </table>
          </td>
        </tr>

        <!-- Security note -->
        <tr>
          <td style=""padding:20px 40px 36px;"">
            <table width=""100%"" cellpadding=""0"" cellspacing=""0"" role=""presentation""
                   style=""background:{BaGoldLight};border-radius:10px;border:0.5px solid {BaGold};"">
              <tr>
                <td style=""padding:14px 18px;"">
                  <p style=""margin:0;font-family:'DM Sans',system-ui,sans-serif;
                             font-size:12px;font-weight:400;color:{BaGoldDark};line-height:1.6;"">
                    Nu distribui nimănui datele cardului. Dacă nu ai solicitat tu emiterea
                    acestui card, contactează-ne imediat la
                    <a href=""mailto:security@bankingapp.md""
                       style=""color:{BaGoldDark};font-weight:500;text-decoration:underline;"">
                      security@bankingapp.md
                    </a>.
                  </p>
                </td>
              </tr>
            </table>
          </td>
        </tr>";

        return HtmlShell($"Cardul tău {typeLabel} {maskedCardNumber} a fost emis cu succes.", body);
    }
}
