package com.example.bankingapp.data.model.transaction

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("id")            val id: String,
    @SerializedName("fromAccountId") val fromAccountId: String?,
    @SerializedName("toAccountId")   val toAccountId: String?,
    @SerializedName("amount")        val amount: Double,
    @SerializedName("currency")      val currency: String,
    @SerializedName("type")          val type: String,
    @SerializedName("status")        val status: String,
    @SerializedName("description")   val description: String?,
    @SerializedName("createdAt")     val createdAt: String,
    @SerializedName("processedAt")   val processedAt: String?
)
