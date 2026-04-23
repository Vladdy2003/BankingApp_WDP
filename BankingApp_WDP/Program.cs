using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.ChainOfResponsibility;
using BankingApp.Patterns.Behavioral.Command;
using BankingApp.Patterns.Behavioral.Observer;
using BankingApp.Patterns.Behavioral.Strategy;
using BankingApp.Patterns.Creational.AbstractFactory;
using BankingApp.Patterns.Creational.Builder;
using BankingApp.Patterns.Creational.FactoryMethod;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Patterns.Structural.Decorator;
using BankingApp.Patterns.Creational.Prototype;
using BankingApp.Patterns.Structural.Facade;
using BankingApp.Patterns.Structural.Proxy;
using BankingApp.Services;
using BankingApp.Utilities;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.IdentityModel.Tokens;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// Singleton Pattern #1 — LoggerService: o singură instanță de logger centralizat pe toată durata aplicației
builder.Services.AddSingleton<ILoggerService, LoggerService>();

// Singleton Pattern #2 — AppConfigurationManager: setările globale ale aplicației, citite o singură dată la startup
builder.Services.AddSingleton<AppConfigurationManager>();

// Scoped — AppDbContext: un context EF Core per request HTTP (comportamentul implicit al AddDbContext)
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

builder.Services.AddIdentity<ApplicationUser, IdentityRole>()
    .AddEntityFrameworkStores<AppDbContext>()
    .AddDefaultTokenProviders();

// Configurare JWT Authentication
var jwtSettings = builder.Configuration.GetSection("JwtSettings");
var secretKey = jwtSettings["SecretKey"]!;

builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = jwtSettings["Issuer"],
        ValidAudience = jwtSettings["Audience"],
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey)),
        ClockSkew = TimeSpan.Zero
    };
});

// Servicii aplicație
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IAuditService, AuditService>();
builder.Services.AddSingleton<IEmailTemplateService, EmailTemplateService>();

// Factory Method Pattern #2 — AccountFactory + IBANGenerator
builder.Services.AddSingleton<IIBANGenerator, IBANGenerator>();
builder.Services.AddScoped<IAccountFactory, AccountFactory>();

// Abstract Factory Pattern #3 — NotificationFactory
// ProductionNotificationFactory in production; swap to MockNotificationFactory in tests.
var isDevelopment = builder.Environment.IsDevelopment();
if (isDevelopment)
    builder.Services.AddScoped<INotificationFactory, MockNotificationFactory>();
else
    builder.Services.AddScoped<INotificationFactory, ProductionNotificationFactory>();

// Builder Pattern #5 — TransactionBuilder: construiește tranzacții pas cu pas cu validare per câmp
builder.Services.AddTransient<ITransactionBuilder, TransactionBuilder>();

// Proxy Pattern #4 — AccountServiceProxy wrappează AccountService adăugând permisiuni, caching și logging
builder.Services.AddMemoryCache();
builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped<AccountService>();
builder.Services.AddScoped<IAccountService>(sp => new AccountServiceProxy(
    sp.GetRequiredService<AccountService>(),
    sp.GetRequiredService<IMemoryCache>(),
    sp.GetRequiredService<ILoggerService>(),
    sp.GetRequiredService<IHttpContextAccessor>()
));

// Chain of Responsibility Pattern #6 — TransactionValidationChain: validatori înlănțuiți (status → sold → limită zilnică → fraud)
builder.Services.AddScoped<AccountStatusValidator>();
builder.Services.AddScoped<BalanceValidator>();
builder.Services.AddScoped<DailyLimitValidator>();
builder.Services.AddScoped<FraudDetectionValidator>();
builder.Services.AddScoped<ITransactionValidator>(sp =>
{
    var accountStatus = sp.GetRequiredService<AccountStatusValidator>();
    var balance       = sp.GetRequiredService<BalanceValidator>();
    var dailyLimit    = sp.GetRequiredService<DailyLimitValidator>();
    var fraud         = sp.GetRequiredService<FraudDetectionValidator>();
    accountStatus.SetNext(balance).SetNext(dailyLimit).SetNext(fraud);
    return accountStatus;
});

// Command Pattern #7 — TransactionCommandInvoker: execută comenzi și menține istoricul pentru rollback
builder.Services.AddScoped<ITransactionCommandInvoker, TransactionCommandInvoker>();

// Observer Pattern #11 — TransactionEventPublisher notifică observatorii la fiecare tranzacție procesată
builder.Services.AddScoped<ITransactionObserver, EmailNotificationObserver>();
builder.Services.AddScoped<ITransactionObserver, SmsNotificationObserver>();
builder.Services.AddScoped<ITransactionObserver, InAppNotificationObserver>();
builder.Services.AddScoped<ITransactionObserver, AuditLogObserver>();
builder.Services.AddScoped<ITransactionObservable, TransactionEventPublisher>();

// Decorator Pattern #8 — lanț: Notification(Fee(Logging(BasicProcessor)))
builder.Services.AddScoped<BasicTransactionProcessor>();
builder.Services.AddScoped<ITransactionProcessor>(sp =>
{
    ITransactionProcessor processor = sp.GetRequiredService<BasicTransactionProcessor>();
    processor = new LoggingTransactionDecorator(processor, sp.GetRequiredService<ILoggerService>());
    processor = new FeeTransactionDecorator(processor, sp.GetRequiredService<AppDbContext>(), sp.GetRequiredService<ILoggerService>());
    processor = new NotificationTransactionDecorator(processor, sp.GetRequiredService<INotificationFactory>(), sp.GetRequiredService<IEmailTemplateService>(), sp.GetRequiredService<AppDbContext>(), sp.GetRequiredService<ILoggerService>());
    return processor;
});

// Step 4.3 — Card data generators (Singleton: stateless, thread-safe)
builder.Services.AddSingleton<ICardNumberGenerator, CardNumberGenerator>();
builder.Services.AddSingleton<ICVVGenerator, CVVGenerator>();

// Prototype Pattern #10 — CardFactory clonează template-ul potrivit și personalizează cardul pentru utilizatorul specific
builder.Services.AddSingleton<ICardFactory, CardFactory>();

// Strategy Pattern #9 — InterestCalculationStrategy: algoritmi diferiți de dobândă per tip de cont
builder.Services.AddScoped<SimpleInterestStrategy>();
builder.Services.AddScoped<CompoundInterestStrategy>();
builder.Services.AddScoped<NoInterestStrategy>();
builder.Services.AddScoped<IInterestService, InterestService>();

// Facade Pattern #12 — BankingFacade: interfață unificată peste AccountService, Command, Chain, Observer
builder.Services.AddScoped<IBankingFacade, BankingFacade>();

builder.Services.AddControllers();
builder.Services.AddSwaggerGen();

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(options =>
    {
        options.SwaggerEndpoint("/swagger/v1/swagger.json", "BankingApp API v1");
        options.RoutePrefix = "swagger";
    });
}

app.UseHttpsRedirection();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();
