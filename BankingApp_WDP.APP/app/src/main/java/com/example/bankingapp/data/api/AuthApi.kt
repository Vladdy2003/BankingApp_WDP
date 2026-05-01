package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.auth.AuthResponse
import com.example.bankingapp.data.model.auth.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}
