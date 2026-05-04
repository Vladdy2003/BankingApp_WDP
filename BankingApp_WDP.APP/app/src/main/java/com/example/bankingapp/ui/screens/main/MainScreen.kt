package com.example.bankingapp.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.consumeWindowInsets
import com.example.bankingapp.navigation.BottomNavItem
import com.example.bankingapp.ui.screens.accounts.AccountsListScreen
import com.example.bankingapp.ui.screens.cards.CardsListScreen
import com.example.bankingapp.ui.screens.dashboard.DashboardScreen
import com.example.bankingapp.ui.screens.transactions.TransactionsListScreen
import com.example.bankingapp.ui.theme.BaGold

@Composable
fun MainScreen(
    onNavigateToNotifications: () -> Unit,
    onNavigateToAccountDetail: (String) -> Unit,
    onNavigateToCardDetail: (String) -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomNavItem.Dashboard.route) }

    val tabs = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Accounts,
        BottomNavItem.Cards,
        BottomNavItem.Transactions,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab.route,
                        onClick  = { selectedTab = tab.route },
                        icon     = {
                            Icon(
                                imageVector        = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label    = {
                            Text(
                                text  = tab.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = BaGold,
                            selectedTextColor   = BaGold,
                            indicatorColor      = MaterialTheme.colorScheme.surfaceVariant,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            when (selectedTab) {
                BottomNavItem.Dashboard.route -> DashboardScreen(
                    onNavigateToNotifications = onNavigateToNotifications
                )
                BottomNavItem.Accounts.route -> AccountsListScreen(
                    onNavigateToAccountDetail = onNavigateToAccountDetail
                )
                BottomNavItem.Cards.route -> CardsListScreen(
                    onNavigateToCardDetail = onNavigateToCardDetail
                )
                BottomNavItem.Transactions.route -> TransactionsListScreen()
                else -> PlaceholderTab(
                    label = tabs.first { it.route == selectedTab }.label
                )
            }
        }
    }
}

@Composable
private fun PlaceholderTab(label: String) {
    Box(
        modifier          = Modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center
    ) {
        Text(
            text  = "$label — în curând",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
