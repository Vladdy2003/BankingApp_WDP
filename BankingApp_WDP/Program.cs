using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.AbstractFactory;
using BankingApp.Patterns.Creational.Builder;
using BankingApp.Patterns.Creational.FactoryMethod;
using BankingApp.Patterns.Creational.Singleton;
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
