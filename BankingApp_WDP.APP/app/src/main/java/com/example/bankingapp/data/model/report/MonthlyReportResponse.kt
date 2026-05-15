package com.example.bankingapp.data.model.report

import com.google.gson.annotations.SerializedName

data class MonthlyReportResponse(
    @SerializedName("accountId")      val accountId: String,
    @SerializedName("iban")           val iban: String,
    @SerializedName("currency")       val currency: String,
    @SerializedName("year")           val year: Int,
    @SerializedName("month")          val month: Int,
    @SerializedName("openingBalance") val openingBalance: Double,
    @SerializedName("closingBalance") val closingBalance: Double,
    @SerializedName("totalCredits")   val totalCredits: Double,
    @SerializedName("totalDebits")    val totalDebits: Double,
    @SerializedName("totalFees")      val totalFees: Double,
    @SerializedName("transactionCount") val transactionCount: Int
)
