package com.example.bankingapp.data.model.report

import com.google.gson.annotations.SerializedName

data class MonthlyBreakdown(
    @SerializedName("month")            val month: Int,
    @SerializedName("totalCredits")     val totalCredits: Double = 0.0,
    @SerializedName("totalDebits")      val totalDebits: Double = 0.0,
    @SerializedName("totalFees")        val totalFees: Double = 0.0,
    @SerializedName("netChange")        val netChange: Double = 0.0,
    @SerializedName("transactionCount") val transactionCount: Int = 0
)
