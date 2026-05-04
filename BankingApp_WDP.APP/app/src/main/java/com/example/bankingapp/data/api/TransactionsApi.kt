package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.transaction.DepositRequest
import com.example.bankingapp.data.model.transaction.PagedTransactionResponse
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.data.model.transaction.TransferRequest
import com.example.bankingapp.data.model.transaction.WithdrawRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TransactionsApi {
    @GET("api/transactions")
    suspend fun getTransactions(
        @Query("type")     type: String?  = null,
        @Query("status")   status: String? = null,
        @Query("from")     from: String?  = null,
        @Query("to")       to: String?    = null,
        @Query("page")     page: Int      = 1,
        @Query("pageSize") pageSize: Int  = 20
    ): PagedTransactionResponse

    @GET("api/accounts/{accountId}/transactions")
    suspend fun getAccountTransactions(
        @Path("accountId")  accountId: String,
        @Query("page")      page: Int     = 1,
        @Query("pageSize")  pageSize: Int = 20
    ): PagedTransactionResponse

    @POST("api/transactions/deposit")
    suspend fun deposit(@Body request: DepositRequest): TransactionResponse

    @POST("api/transactions/withdraw")
    suspend fun withdraw(@Body request: WithdrawRequest): TransactionResponse

    @POST("api/transactions/transfer")
    suspend fun transfer(@Body request: TransferRequest): TransactionResponse
}
