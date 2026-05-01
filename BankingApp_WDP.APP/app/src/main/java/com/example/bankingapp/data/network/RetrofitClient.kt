package com.example.bankingapp.data.network

import com.example.bankingapp.BuildConfig
import com.example.bankingapp.data.api.AccountsApi
import com.example.bankingapp.data.api.AuthApi
import com.example.bankingapp.data.api.CardsApi
import com.example.bankingapp.data.api.NotificationsApi
import com.example.bankingapp.data.api.ReportsApi
import com.example.bankingapp.data.api.TransactionsApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {

    private const val BASE_URL = "https://10.0.2.2:7016/"

    @Volatile private var authToken: String? = null

    fun setAuthToken(token: String?) { authToken = token }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .apply { authToken?.let { addHeader("Authorization", "Bearer $it") } }
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = if (BuildConfig.DEBUG) {
        // Development only: trust all certificates (self-signed .NET dev cert)
        val trustAll = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustAll), SecureRandom())
        }
        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAll)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    } else {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi             = retrofit.create(AuthApi::class.java)
    val accountsApi: AccountsApi     = retrofit.create(AccountsApi::class.java)
    val cardsApi: CardsApi           = retrofit.create(CardsApi::class.java)
    val transactionsApi: TransactionsApi = retrofit.create(TransactionsApi::class.java)
    val notificationsApi: NotificationsApi = retrofit.create(NotificationsApi::class.java)
    val reportsApi: ReportsApi       = retrofit.create(ReportsApi::class.java)
}
