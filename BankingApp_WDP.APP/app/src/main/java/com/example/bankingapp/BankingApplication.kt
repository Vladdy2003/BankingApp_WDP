package com.example.bankingapp

import android.app.Application
import com.example.bankingapp.data.network.RetrofitClient

class BankingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.initialize(this)
    }
}
