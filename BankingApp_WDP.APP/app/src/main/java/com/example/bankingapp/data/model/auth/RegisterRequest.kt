package com.example.bankingapp.data.model.auth

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("email")       val email: String,
    @SerializedName("password")    val password: String,
    @SerializedName("firstName")   val firstName: String,
    @SerializedName("lastName")    val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("address")     val address: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String,
    @SerializedName("cnp")         val cnp: String
)
