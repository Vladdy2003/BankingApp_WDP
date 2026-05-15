package com.example.bankingapp.data.model.report

import com.google.gson.annotations.SerializedName

data class SpendingByCategoryResponse(
    @SerializedName("accountId")     val accountId: String,
    @SerializedName("from")          val from: String,
    @SerializedName("to")            val to: String,
    @SerializedName("totalSpending") val totalSpending: Double,
    @SerializedName("categories")    val categories: List<CategoryBreakdown>
)

data class CategoryBreakdown(
    @SerializedName("category")   val category: String,
    @SerializedName("amount")     val amount: Double,
    @SerializedName("percentage") val percentage: Double
)
