package com.example.bankingapp.data.model.account

import com.google.gson.annotations.SerializedName

data class UpdateAccountRequest(
    @SerializedName("currency")       val currency: String?  = null,
    @SerializedName("overdraftLimit") val overdraftLimit: Double? = null,
    @SerializedName("interestRate")   val interestRate: Double?   = null,
    @SerializedName("companyName")    val companyName: String?    = null
)
