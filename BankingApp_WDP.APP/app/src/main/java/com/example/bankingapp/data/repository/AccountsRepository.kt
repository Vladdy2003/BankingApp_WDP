package com.example.bankingapp.data.repository

import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.account.CreateAccountRequest
import com.example.bankingapp.data.model.account.UpdateAccountRequest
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.data.network.RetrofitClient

class AccountsRepository {
    suspend fun getAccounts(): List<AccountResponse> =
        RetrofitClient.accountsApi.getAccounts()

    suspend fun getAccount(id: String): AccountResponse =
        RetrofitClient.accountsApi.getAccount(id)

    suspend fun createAccount(request: CreateAccountRequest): AccountResponse =
        RetrofitClient.accountsApi.createAccount(request)

    suspend fun updateAccount(id: String, request: UpdateAccountRequest): AccountResponse =
        RetrofitClient.accountsApi.updateAccount(id, request)

    suspend fun deleteAccount(id: String) =
        RetrofitClient.accountsApi.deleteAccount(id)

    suspend fun getAccountTransactions(
        accountId: String,
        page: Int,
        pageSize: Int
    ): List<TransactionResponse> =
        RetrofitClient.transactionsApi.getAccountTransactions(accountId, page, pageSize)
}
