package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.transaction.TransactionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TransactionsApi {
    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("page")     page: Int     = 1,
        @Query("pageSize") pageSize: Int = 20
    ): List<TransactionResponse>
}
