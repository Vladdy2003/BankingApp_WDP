package com.example.bankingapp.navigation

sealed class Screen(val route: String) {
    object Splash    : Screen("splash")
    object Login     : Screen("login")
    object Register  : Screen("register")
    object Main      : Screen("main")
}
