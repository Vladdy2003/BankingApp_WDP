package com.example.bankingapp.data.repository

import com.example.bankingapp.data.model.auth.AuthResponse
import com.example.bankingapp.data.model.auth.LoginRequest
import com.example.bankingapp.data.model.auth.RegisterRequest
import com.example.bankingapp.data.network.RetrofitClient

class AuthRepository {

    private val api = RetrofitClient.authApi

    suspend fun login(email: String, password: String): AuthResponse =
        api.login(LoginRequest(email, password))

    suspend fun register(request: RegisterRequest) {
        val response = api.register(request)
        if (!response.isSuccessful) {
            throw retrofit2.HttpException(response)
        }
    }
}
