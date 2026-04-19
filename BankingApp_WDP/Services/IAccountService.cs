using BankingApp.Models;

namespace BankingApp.Services;

public interface IAccountService
{
    Task<Account?> GetByIdAsync(int accountId);
    Task<IEnumerable<Account>> GetByUserIdAsync(string userId);
    Task<Account> CreateAsync(Account account);
    Task<Account> UpdateAsync(Account account);
    Task<Account?> GetByIBANAsync(string iban);
    Task<bool> CloseAsync(int accountId);
}
