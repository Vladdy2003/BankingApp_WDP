package com.example.bankingapp.data.model.transaction

import com.google.gson.annotations.SerializedName

data class TransferRequest(
    @SerializedName("fromAccountId") val fromAccountId: String,
    @SerializedName("toAccountId")   val toAccountId: String,
    @SerializedName("amount")        val amount: Double,
    @SerializedName("currency")      val currency: String = "MDL",
    @SerializedName("description")   val description: String? = null
)
