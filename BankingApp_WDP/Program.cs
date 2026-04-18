using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
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
