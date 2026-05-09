package com.example.bankingapp.data.network

import android.content.Context
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
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun setAuthToken(token: String?) { authToken = token }

    private val noCacheInterceptor = Interceptor { chain ->
        chain.proceed(
            chain.request().newBuilder()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .build()
        )
    }

    private val authInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .apply { authToken?.let { addHeader("Authorization", "Bearer $it") } }
            .build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        val ctx = requireNotNull(appContext) {
            "RetrofitClient.initialize(context) must be called before any API usage"
        }
        val authenticator = TokenAuthenticator(ctx)

        if (BuildConfig.DEBUG) {
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
                .addInterceptor(noCacheInterceptor)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .authenticator(authenticator)
                .build()
        } else {
            OkHttpClient.Builder()
                .addInterceptor(noCacheInterceptor)
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .authenticator(authenticator)
                .build()
        }
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi             by lazy { retrofit.create(AuthApi::class.java) }
    val accountsApi: AccountsApi     by lazy { retrofit.create(AccountsApi::class.java) }
    val cardsApi: CardsApi           by lazy { retrofit.create(CardsApi::class.java) }
    val transactionsApi: TransactionsApi by lazy { retrofit.create(TransactionsApi::class.java) }
    val notificationsApi: NotificationsApi by lazy { retrofit.create(NotificationsApi::class.java) }
    val reportsApi: ReportsApi       by lazy { retrofit.create(ReportsApi::class.java) }
}
