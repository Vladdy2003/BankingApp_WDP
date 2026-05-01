package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.report.AnnualSummaryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportsApi {
    @GET("api/reports/annual-summary")
    suspend fun getAnnualSummary(
        @Query("accountId") accountId: String,
        @Query("year")      year: Int
    ): AnnualSummaryResponse
}
