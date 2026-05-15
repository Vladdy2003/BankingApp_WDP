package com.example.bankingapp.data.model.auth

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("accessToken")  val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresAt")    val expiresAt: String,
    @SerializedName("userId")       val userId: String,
    @SerializedName("email")        val email: String,
    @SerializedName("fullName")     val fullName: String
)
