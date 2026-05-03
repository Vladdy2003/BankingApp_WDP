package com.example.bankingapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bankingapp.ui.screens.accounts.AccountDetailScreen
import com.example.bankingapp.ui.screens.cards.CardDetailScreen
import com.example.bankingapp.ui.screens.login.LoginScreen
import com.example.bankingapp.ui.screens.main.MainScreen
import com.example.bankingapp.ui.screens.notifications.NotificationsScreen
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
                initialEmail         = prefillEmail,
                onNavigateToMain     = {
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
            MainScreen(
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Screen.AccountDetail.navRoute(accountId))
                },
                onNavigateToCardDetail = { cardId ->
                    navController.navigate(Screen.CardDetail.navRoute(cardId))
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.AccountDetail.route,
            arguments = listOf(
                navArgument(Screen.AccountDetail.argAccountId) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments
                ?.getString(Screen.AccountDetail.argAccountId) ?: ""
            AccountDetailScreen(
                accountId       = accountId,
                onNavigateBack  = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.CardDetail.route,
            arguments = listOf(
                navArgument(Screen.CardDetail.argCardId) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString(Screen.CardDetail.argCardId) ?: ""
            CardDetailScreen(
                cardId                   = cardId,
                onNavigateBack           = { navController.popBackStack() },
                onNavigateToAccountDetail = { accountId ->
                    navController.navigate(Screen.AccountDetail.navRoute(accountId))
                }
            )
        }
    }
}
