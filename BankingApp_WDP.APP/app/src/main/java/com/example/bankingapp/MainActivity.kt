package com.example.bankingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import com.example.bankingapp.data.local.ThemePreferences
import com.example.bankingapp.navigation.AppNavGraph
import com.example.bankingapp.ui.theme.BankingAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreferences = remember { ThemePreferences(applicationContext) }
            val systemDark       = isSystemInDarkTheme()
            val isDarkTheme by themePreferences.isDarkTheme.collectAsState(initial = systemDark)
            val scope = rememberCoroutineScope()

            BankingAppTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                AppNavGraph(
                    navController     = navController,
                    isDarkTheme       = isDarkTheme,
                    onToggleDarkTheme = {
                        scope.launch { themePreferences.setDarkTheme(!isDarkTheme) }
                    }
                )
            }
        }
    }
}
