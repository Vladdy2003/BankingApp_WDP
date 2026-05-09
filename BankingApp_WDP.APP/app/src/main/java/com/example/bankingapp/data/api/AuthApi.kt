package com.example.bankingapp.data.api

import com.example.bankingapp.data.model.auth.AuthResponse
import com.example.bankingapp.data.model.auth.LoginRequest
import com.example.bankingapp.data.model.auth.RefreshTokenRequest
import com.example.bankingapp.data.model.auth.RegisterRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ResponseBody>

    @POST("api/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): AuthResponse
}
