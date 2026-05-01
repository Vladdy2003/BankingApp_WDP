package com.example.bankingapp.data.model.report

import com.google.gson.annotations.SerializedName

data class AnnualSummaryResponse(
    @SerializedName("accountId")         val accountId: String,
    @SerializedName("iban")              val iban: String,
    @SerializedName("currency")          val currency: String,
    @SerializedName("year")              val year: Int,
    @SerializedName("totalCredits")      val totalCredits: Double,
    @SerializedName("totalDebits")       val totalDebits: Double,
    @SerializedName("totalFees")         val totalFees: Double,
    @SerializedName("netChange")         val netChange: Double,
    @SerializedName("totalTransactions") val totalTransactions: Int,
    @SerializedName("monthlyBreakdowns") val monthlyBreakdowns: List<MonthlyBreakdown> = emptyList()
)
