package com.example.bankingapp.data.model.card

import com.google.gson.annotations.SerializedName

data class UpdateCardLimitsRequest(
    @SerializedName("dailyLimit")   val dailyLimit: Double? = null,
    @SerializedName("monthlyLimit") val monthlyLimit: Double? = null
)
