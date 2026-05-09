package com.example.bankingapp.data.network

import android.content.Context
import com.example.bankingapp.BuildConfig
import com.example.bankingapp.data.local.TokenManager
import com.example.bankingapp.data.model.auth.AuthResponse
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class TokenAuthenticator(private val context: Context) : Authenticator {

    private val gson = Gson()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite retry loop
        if (responseCount(response) >= 2) return null

        val tokenManager = TokenManager(context)
        val refreshToken = runBlocking { tokenManager.getRefreshToken() } ?: return null

        val authResponse = performRefresh(refreshToken) ?: return null

        runBlocking {
            tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken)
        }
        RetrofitClient.setAuthToken(authResponse.accessToken)

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${authResponse.accessToken}")
            .build()
    }

    private fun performRefresh(refreshToken: String): AuthResponse? {
        return try {
            val client = buildRefreshClient()
            val body = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("https://10.0.2.2:7016/api/auth/refresh-token")
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            val responseBody = response.body?.string() ?: return null
            gson.fromJson(responseBody, AuthResponse::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun buildRefreshClient(): OkHttpClient {
        if (!BuildConfig.DEBUG) return OkHttpClient.Builder().build()

        val trustAll = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
        }
        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAll)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) { count++; prior = prior.priorResponse }
        return count
    }
}
