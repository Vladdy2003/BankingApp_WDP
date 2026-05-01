package com.example.bankingapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bankingapp.ui.screens.dashboard.DashboardScreen
import com.example.bankingapp.ui.screens.login.LoginScreen
import com.example.bankingapp.ui.screens.register.RegisterScreen
import com.example.bankingapp.ui.screens.splash.SplashScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.navRoute()) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route     = Screen.Login.route,
            arguments = listOf(
                navArgument(Screen.Login.argEmail) {
                    type         = NavType.StringType
                    defaultValue = ""
                    nullable     = false
                }
            )
        ) { backStackEntry ->
            val prefillEmail = backStackEntry.arguments?.getString(Screen.Login.argEmail) ?: ""
            LoginScreen(
                initialEmail        = prefillEmail,
                onNavigateToMain    = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { email ->
                    navController.navigate(Screen.Login.navRoute(email)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Main.route) {
            DashboardScreen(
                onNavigateToNotifications = { /* C.3 — neimplementat */ }
            )
        }
    }
}
