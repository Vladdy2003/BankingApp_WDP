using BankingApp.Data;
using BankingApp.Models;
using BankingApp.Patterns.Behavioral.State;
using Microsoft.EntityFrameworkCore;

namespace BankingApp.Services;

public class AccountService : IAccountService
{
    private readonly AppDbContext _db;

    public AccountService(AppDbContext db)
    {
        _db = db;
    }

    public async Task<Account?> GetByIdAsync(int accountId)
        => await _db.Accounts.FindAsync(accountId);

    public async Task<IEnumerable<Account>> GetByUserIdAsync(string userId)
        => await _db.Accounts
            .Where(a => a.UserId == userId && a.Status != AccountStatus.Closed)
            .ToListAsync();

    public async Task<Account> CreateAsync(Account account)
    {
        _db.Accounts.Add(account);
        await _db.SaveChangesAsync();
        return account;
    }

    public async Task<Account> UpdateAsync(Account account)
    {
        _db.Accounts.Update(account);
        await _db.SaveChangesAsync();
        return account;
    }

    public async Task<Account?> GetByIBANAsync(string iban)
        => await _db.Accounts.FirstOrDefaultAsync(a => a.IBAN == iban);

    public async Task<bool> CloseAsync(int accountId)
    {
        var account = await _db.Accounts.FindAsync(accountId);
        if (account == null) return false;

        // State Pattern #13 — tranziție validată de starea curentă
        var state = AccountStateContext.Resolve(account.Status);
        account.Status = state.Close(account);

        await _db.SaveChangesAsync();
        return true;
    }

    public async Task<bool> SuspendAsync(int accountId)
    {
        var account = await _db.Accounts.FindAsync(accountId);
        if (account == null) return false;

        // State Pattern #13 — doar conturile Active pot fi suspendate
        var state = AccountStateContext.Resolve(account.Status);
        account.Status = state.Suspend(account);

        await _db.SaveChangesAsync();
        return true;
    }

    public async Task<bool> ActivateAsync(int accountId)
    {
        var account = await _db.Accounts.FindAsync(accountId);
        if (account == null) return false;

        // State Pattern #13 — reactivare din Suspended sau Inactive
        var state = AccountStateContext.Resolve(account.Status);
        account.Status = state.Activate(account);

        await _db.SaveChangesAsync();
        return true;
    }
}
