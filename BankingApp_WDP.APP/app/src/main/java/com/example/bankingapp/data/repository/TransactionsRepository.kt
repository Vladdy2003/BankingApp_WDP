package com.example.bankingapp.data.repository

import com.example.bankingapp.data.model.transaction.DepositRequest
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.data.model.transaction.TransferRequest
import com.example.bankingapp.data.model.transaction.WithdrawRequest
import com.example.bankingapp.data.network.RetrofitClient

class TransactionsRepository {
    suspend fun getTransactions(
        type: String?   = null,
        status: String? = null,
        from: String?   = null,
        to: String?     = null,
        page: Int       = 1,
        pageSize: Int   = 20
    ): List<TransactionResponse> =
        RetrofitClient.transactionsApi.getTransactions(
            type     = type?.ifBlank { null },
            status   = status?.ifBlank { null },
            from     = from?.ifBlank { null },
            to       = to?.ifBlank { null },
            page     = page,
            pageSize = pageSize
        ).items

    suspend fun deposit(request: DepositRequest): TransactionResponse =
        RetrofitClient.transactionsApi.deposit(request)

    suspend fun withdraw(request: WithdrawRequest): TransactionResponse =
        RetrofitClient.transactionsApi.withdraw(request)

    suspend fun transfer(request: TransferRequest): TransactionResponse =
        RetrofitClient.transactionsApi.transfer(request)
}
