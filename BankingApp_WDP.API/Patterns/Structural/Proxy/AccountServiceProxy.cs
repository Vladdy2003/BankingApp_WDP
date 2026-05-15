using System.Security.Claims;
using BankingApp.Models;
using BankingApp.Patterns.Creational.Singleton;
using BankingApp.Services;
using Microsoft.Extensions.Caching.Memory;

namespace BankingApp.Patterns.Structural.Proxy;

/// <summary>
/// Proxy Pattern #4 — wrappează AccountService și adaugă transparant:
///   1. Verificare permisiuni: utilizatorul poate accesa doar propriile conturi.
///   2. Caching în memorie: reduce query-urile la DB pentru operații de citire.
///   3. Logging automat al fiecărei operații prin ILoggerService (Singleton #1).
/// </summary>
public class AccountServiceProxy : IAccountService
{
    private readonly IAccountService _inner;
    private readonly IMemoryCache _cache;
    private readonly ILoggerService _logger;
    private readonly IHttpContextAccessor _httpContextAccessor;

    private static readonly TimeSpan CacheDuration = TimeSpan.FromMinutes(5);

    public AccountServiceProxy(
        IAccountService inner,
        IMemoryCache cache,
        ILoggerService logger,
        IHttpContextAccessor httpContextAccessor)
    {
        _inner = inner;
        _cache = cache;
        _logger = logger;
        _httpContextAccessor = httpContextAccessor;
    }

    private string? CurrentUserId =>
        _httpContextAccessor.HttpContext?.User?.FindFirstValue(ClaimTypes.NameIdentifier);

    public async Task<Account?> GetByIdAsync(int accountId)
    {
        _logger.LogInformation($"[Proxy] GetByIdAsync — accountId={accountId}, caller={CurrentUserId}");

        var cacheKey = $"account:{accountId}";
        if (_cache.TryGetValue(cacheKey, out Account? cached))
        {
            _logger.LogInformation($"[Proxy] Cache hit — accountId={accountId}");
            EnsureAccess(cached, accountId);
            return cached;
        }

        var account = await _inner.GetByIdAsync(accountId);
        if (account == null) return null;

        EnsureAccess(account, accountId);
        _cache.Set(cacheKey, account, CacheDuration);
        return account;
    }

    public async Task<IEnumerable<Account>> GetByUserIdAsync(string userId)
    {
        _logger.LogInformation($"[Proxy] GetByUserIdAsync — userId={userId}, caller={CurrentUserId}");

        if (CurrentUserId != null && CurrentUserId != userId)
            throw new UnauthorizedAccessException("Acces refuzat: nu poți lista conturile altui utilizator.");

        var cacheKey = $"accounts:user:{userId}";
        if (_cache.TryGetValue(cacheKey, out IEnumerable<Account>? cached))
        {
            _logger.LogInformation($"[Proxy] Cache hit — conturi pentru userId={userId}");
            return cached!;
        }

        var accounts = await _inner.GetByUserIdAsync(userId);
        _cache.Set(cacheKey, accounts, CacheDuration);
        return accounts;
    }

    public async Task<Account> CreateAsync(Account account)
    {
        _logger.LogInformation($"[Proxy] CreateAsync — type={account.Type}, userId={account.UserId}");

        var result = await _inner.CreateAsync(account);

        _cache.Set($"account:{result.Id}", result, CacheDuration);
        InvalidateUserCache(account.UserId);

        _logger.LogInformation($"[Proxy] Cont creat — accountId={result.Id}, IBAN={result.IBAN}");
        return result;
    }

    public async Task<Account> UpdateAsync(Account account)
    {
        _logger.LogInformation($"[Proxy] UpdateAsync — accountId={account.Id}, caller={CurrentUserId}");

        EnsureAccess(account, account.Id);

        var result = await _inner.UpdateAsync(account);

        _cache.Remove($"account:{account.Id}");
        InvalidateUserCache(account.UserId);

        return result;
    }

    public async Task<Account?> GetByIBANAsync(string iban)
    {
        _logger.LogInformation($"[Proxy] GetByIBANAsync — iban={iban}, caller={CurrentUserId}");

        var cacheKey = $"account:iban:{iban}";
        if (_cache.TryGetValue(cacheKey, out Account? cached))
        {
            _logger.LogInformation($"[Proxy] Cache hit — iban={iban}");
            return cached;
        }

        var account = await _inner.GetByIBANAsync(iban);
        if (account != null)
            _cache.Set(cacheKey, account, CacheDuration);

        return account;
    }

    public async Task<bool> CloseAsync(int accountId)
    {
        _logger.LogInformation($"[Proxy] CloseAsync — accountId={accountId}, caller={CurrentUserId}");

        var account = await _inner.GetByIdAsync(accountId);
        if (account != null) EnsureAccess(account, accountId);

        var result = await _inner.CloseAsync(accountId);
        if (result)
        {
            _cache.Remove($"account:{accountId}");
            if (account != null)
            {
                _cache.Remove($"account:iban:{account.IBAN}");
                InvalidateUserCache(account.UserId);
            }
            _logger.LogInformation($"[Proxy] Cont închis — accountId={accountId}");
        }

        return result;
    }

    public async Task<bool> SuspendAsync(int accountId)
    {
        _logger.LogInformation($"[Proxy] SuspendAsync — accountId={accountId}, caller={CurrentUserId}");

        var account = await _inner.GetByIdAsync(accountId);
        if (account != null) EnsureAccess(account, accountId);

        var result = await _inner.SuspendAsync(accountId);
        if (result)
        {
            InvalidateAccountCache(accountId, account);
            _logger.LogInformation($"[Proxy] Cont suspendat — accountId={accountId}");
        }

        return result;
    }

    public async Task<bool> ActivateAsync(int accountId)
    {
        _logger.LogInformation($"[Proxy] ActivateAsync — accountId={accountId}, caller={CurrentUserId}");

        var account = await _inner.GetByIdAsync(accountId);
        if (account != null) EnsureAccess(account, accountId);

        var result = await _inner.ActivateAsync(accountId);
        if (result)
        {
            InvalidateAccountCache(accountId, account);
            _logger.LogInformation($"[Proxy] Cont reactivat — accountId={accountId}");
        }

        return result;
    }

    private void EnsureAccess(Account? account, int accountId)
    {
        if (account == null) return;
        if (CurrentUserId == null) return;
        if (account.UserId != CurrentUserId)
            throw new UnauthorizedAccessException($"Acces refuzat la contul {accountId}.");
    }

    private void InvalidateAccountCache(int accountId, Account? account)
    {
        _cache.Remove($"account:{accountId}");
        if (account != null)
        {
            _cache.Remove($"account:iban:{account.IBAN}");
            InvalidateUserCache(account.UserId);
        }
    }

    private void InvalidateUserCache(string userId)
        => _cache.Remove($"accounts:user:{userId}");
}
