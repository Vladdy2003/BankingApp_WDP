package com.example.bankingapp.data.model.account

import com.google.gson.annotations.SerializedName

data class AccountResponse(
    @SerializedName("id")            val id: String,
    @SerializedName("iban")          val iban: String,
    @SerializedName("type")          val type: String,
    @SerializedName("status")        val status: String,
    @SerializedName("balance")       val balance: Double,
    @SerializedName("currency")      val currency: String,
    @SerializedName("createdAt")     val createdAt: String,
    @SerializedName("overdraftLimit") val overdraftLimit: Double? = null,
    @SerializedName("interestRate")  val interestRate: Double? = null,
    @SerializedName("companyName")   val companyName: String? = null
)
