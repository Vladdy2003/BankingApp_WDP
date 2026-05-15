package com.example.bankingapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Dashboard    : BottomNavItem("tab_dashboard",    "Acasă",      Icons.Default.Home)
    object Accounts     : BottomNavItem("tab_accounts",     "Conturi",    Icons.Default.AccountBalance)
    object Cards        : BottomNavItem("tab_cards",        "Carduri",    Icons.Default.CreditCard)
    object Transactions : BottomNavItem("tab_transactions", "Tranzacții", Icons.Default.Receipt)
    object Settings     : BottomNavItem("tab_settings",     "Setări",     Icons.Default.Settings)
}
