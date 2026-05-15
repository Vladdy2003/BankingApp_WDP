package com.example.bankingapp.data.model.report

import com.google.gson.annotations.SerializedName

data class MonthlyBreakdown(
    @SerializedName("month")            val month: Int,
    @SerializedName("monthName")        val monthName: String = "",
    @SerializedName("credits")          val totalCredits: Double = 0.0,
    @SerializedName("debits")           val totalDebits: Double = 0.0,
    @SerializedName("fees")             val totalFees: Double = 0.0,
    @SerializedName("transactionCount") val transactionCount: Int = 0
)
