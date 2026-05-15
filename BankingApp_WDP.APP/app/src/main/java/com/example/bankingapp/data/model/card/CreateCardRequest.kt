package com.example.bankingapp.data.model.card

import com.google.gson.annotations.SerializedName

data class CreateCardRequest(
    @SerializedName("accountId")    val accountId: String,
    @SerializedName("type")         val type: Int,
    @SerializedName("dailyLimit")   val dailyLimit: Double? = null,
    @SerializedName("monthlyLimit") val monthlyLimit: Double? = null
)
