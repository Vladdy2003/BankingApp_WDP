package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.report.AnnualSummaryResponse
import com.example.bankingapp.data.model.report.IncomeVsExpensesResponse
import com.example.bankingapp.data.model.report.MonthlyReportResponse
import com.example.bankingapp.data.model.report.SpendingByCategoryResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ReportsApi {

    @GET("api/reports/annual-summary")
    suspend fun getAnnualSummary(
        @Query("accountId") accountId: String,
        @Query("year")      year: Int
    ): AnnualSummaryResponse

    @GET("api/reports/monthly-statement")
    suspend fun getMonthlyStatement(
        @Query("accountId") accountId: String,
        @Query("year")      year: Int,
        @Query("month")     month: Int
    ): MonthlyReportResponse

    @GET("api/reports/spending")
    suspend fun getSpendingByCategory(
        @Query("accountId") accountId: String,
        @Query("from")      from: String,
        @Query("to")        to: String
    ): SpendingByCategoryResponse

    @GET("api/reports/income-expenses")
    suspend fun getIncomeVsExpenses(
        @Query("accountId") accountId: String,
        @Query("from")      from: String,
        @Query("to")        to: String
    ): IncomeVsExpensesResponse

    @Streaming
    @GET("api/reports/export")
    suspend fun exportReport(
        @Query("accountId") accountId: String,
        @Query("format")    format: String,
        @Query("year")      year: Int,
        @Query("month")     month: Int
    ): ResponseBody
}
