package com.example.bankingapp.data.model.report

import com.google.gson.annotations.SerializedName

data class IncomeVsExpensesResponse(
    @SerializedName("accountId")    val accountId: String,
    @SerializedName("from")         val from: String,
    @SerializedName("to")           val to: String,
    @SerializedName("totalIncome")  val totalIncome: Double,
    @SerializedName("totalExpenses") val totalExpenses: Double,
    @SerializedName("netBalance")   val netBalance: Double,
    @SerializedName("periods")      val periods: List<PeriodBreakdown>
)

data class PeriodBreakdown(
    @SerializedName("period")   val period: String,
    @SerializedName("income")   val income: Double,
    @SerializedName("expenses") val expenses: Double
)
