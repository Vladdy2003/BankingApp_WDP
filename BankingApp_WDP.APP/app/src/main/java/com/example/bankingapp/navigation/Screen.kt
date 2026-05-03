package com.example.bankingapp.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Splash         : Screen("splash")
    object Login          : Screen("login?prefillEmail={prefillEmail}") {
        const val argEmail = "prefillEmail"
        fun navRoute(email: String = "") = "login?prefillEmail=${Uri.encode(email)}"
    }
    object Register       : Screen("register")
    object Main           : Screen("main")
    object Notifications  : Screen("notifications")
    object AccountDetail  : Screen("account_detail/{accountId}") {
        const val argAccountId = "accountId"
        fun navRoute(accountId: String) = "account_detail/$accountId"
    }
}
