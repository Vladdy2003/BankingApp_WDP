package com.example.bankingapp.data.repository

import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.data.model.report.AnnualSummaryResponse
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.data.network.RetrofitClient

class DashboardRepository {

    suspend fun getAccounts(): List<AccountResponse> =
        RetrofitClient.accountsApi.getAccounts()

    suspend fun getCards(): List<CardResponse> =
        RetrofitClient.cardsApi.getCards()

    suspend fun getRecentTransactions(): List<TransactionResponse> =
        RetrofitClient.transactionsApi.getTransactions(page = 1, pageSize = 5).items

    suspend fun getUnreadNotificationCount(): Int =
        RetrofitClient.notificationsApi.getUnreadCount()

    suspend fun getAnnualSummary(accountId: String, year: Int): AnnualSummaryResponse =
        RetrofitClient.reportsApi.getAnnualSummary(accountId, year)
}
