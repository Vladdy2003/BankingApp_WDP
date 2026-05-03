package com.example.bankingapp.data.model.account

import com.google.gson.annotations.SerializedName

data class CreateAccountRequest(
    @SerializedName("type")           val type: Int,     // 0=Current, 1=Savings, 2=Business
    @SerializedName("currency")       val currency: String,
    @SerializedName("overdraftLimit") val overdraftLimit: Double? = null,
    @SerializedName("interestRate")   val interestRate: Double? = null,
    @SerializedName("companyName")    val companyName: String? = null
)
