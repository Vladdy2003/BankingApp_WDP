package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.account.CreateAccountRequest
import com.example.bankingapp.data.model.account.UpdateAccountRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AccountsApi {
    @GET("api/accounts")
    suspend fun getAccounts(): List<AccountResponse>

    @GET("api/accounts/{id}")
    suspend fun getAccount(@Path("id") id: String): AccountResponse

    @POST("api/accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): AccountResponse

    @PUT("api/accounts/{id}")
    suspend fun updateAccount(
        @Path("id") id: String,
        @Body request: UpdateAccountRequest
    ): AccountResponse

    @DELETE("api/accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: String)
}
