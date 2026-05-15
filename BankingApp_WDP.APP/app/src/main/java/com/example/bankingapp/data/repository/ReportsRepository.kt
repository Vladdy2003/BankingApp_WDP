package com.example.bankingapp.data.repository

import com.example.bankingapp.data.model.report.AnnualSummaryResponse
import com.example.bankingapp.data.model.report.IncomeVsExpensesResponse
import com.example.bankingapp.data.model.report.MonthlyReportResponse
import com.example.bankingapp.data.model.report.SpendingByCategoryResponse
import com.example.bankingapp.data.network.RetrofitClient
import okhttp3.ResponseBody

class ReportsRepository {

    suspend fun getAnnualSummary(accountId: String, year: Int): AnnualSummaryResponse =
        RetrofitClient.reportsApi.getAnnualSummary(accountId, year)

    suspend fun getMonthlyStatement(accountId: String, year: Int, month: Int): MonthlyReportResponse =
        RetrofitClient.reportsApi.getMonthlyStatement(accountId, year, month)

    suspend fun getSpendingByCategory(accountId: String, from: String, to: String): SpendingByCategoryResponse =
        RetrofitClient.reportsApi.getSpendingByCategory(accountId, from, to)

    suspend fun getIncomeVsExpenses(accountId: String, from: String, to: String): IncomeVsExpensesResponse =
        RetrofitClient.reportsApi.getIncomeVsExpenses(accountId, from, to)

    suspend fun exportReport(accountId: String, format: String, year: Int, month: Int): ResponseBody =
        RetrofitClient.reportsApi.exportReport(accountId, format, year, month)
}
