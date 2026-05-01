package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.account.AccountResponse
import retrofit2.http.GET

interface AccountsApi {
    @GET("api/accounts")
    suspend fun getAccounts(): List<AccountResponse>
}
