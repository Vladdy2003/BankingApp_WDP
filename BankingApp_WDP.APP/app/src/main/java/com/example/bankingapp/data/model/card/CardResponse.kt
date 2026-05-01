package com.example.bankingapp.data.model.card

import com.google.gson.annotations.SerializedName

data class CardResponse(
    @SerializedName("id")               val id: String,
    @SerializedName("accountId")        val accountId: String,
    @SerializedName("maskedCardNumber") val maskedCardNumber: String,
    @SerializedName("expiryDate")       val expiryDate: String,
    @SerializedName("type")             val type: String,
    @SerializedName("status")           val status: String,
    @SerializedName("dailyLimit")       val dailyLimit: Double,
    @SerializedName("monthlyLimit")     val monthlyLimit: Double,
    @SerializedName("createdAt")        val createdAt: String
)
