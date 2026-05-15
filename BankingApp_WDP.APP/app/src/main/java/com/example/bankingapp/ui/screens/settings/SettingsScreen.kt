package com.example.bankingapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaGoldLight

@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToReports: () -> Unit = {},
    onNavigateToLogin: () -> Unit,
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onNavigateToLogin()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header (ba_ink bg — inverted colour block)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.onBackground)
                .padding(vertical = 32.dp, horizontal = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val initials = uiState.fullName
                    .split(" ")
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .joinToString("")
                    .uppercase()

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BaGold),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = initials.ifEmpty { "?" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text  = uiState.fullName.ifEmpty { "Utilizator" },
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.background
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text  = uiState.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                )
            }
        }

        // Menu items
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            SettingsMenuItem(
                icon    = Icons.Default.Person,
                label   = "Profilul Meu",
                onClick = onNavigateToProfile
            )

            Spacer(Modifier.height(8.dp))

            SettingsMenuItem(
                icon    = Icons.Default.BarChart,
                label   = "Rapoarte & Extrase",
                onClick = onNavigateToReports
            )

            Spacer(Modifier.height(8.dp))

            // Theme toggle item
            SettingsMenuItemToggle(
                icon       = if (isDarkTheme) Icons.Default.Nightlight else Icons.Default.WbSunny,
                label      = "Temă: ${if (isDarkTheme) "Întunecat" else "Luminos"}",
                checked    = isDarkTheme,
                onChecked  = { onToggleDarkTheme() }
            )

            Spacer(Modifier.height(8.dp))

            SettingsMenuItem(
                icon     = Icons.Default.ExitToApp,
                label    = "Deconectare",
                onClick  = { viewModel.showLogoutDialog() },
                iconTint = if (isDarkTheme) BaDangerDark else BaDanger,
                textColor = if (isDarkTheme) BaDangerDark else BaDanger
            )
        }
    }

    // Logout confirmation dialog
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLogoutDialog() },
            title = {
                Text(
                    text  = "Deconectare",
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            text = {
                Text(
                    text  = "Sigur doriți să vă deconectați?",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.logout() }) {
                    Text(
                        text  = "Deconectare",
                        color = if (isDarkTheme) BaDangerDark else BaDanger
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideLogoutDialog() }) {
                    Text(text = "Anulează")
                }
            }
        )
    }
}

@Composable
private fun SettingsMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconTint: Color   = BaGold,
    textColor: Color  = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuIconBox(icon = icon, tint = iconTint)

        Spacer(Modifier.width(14.dp))

        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color    = textColor,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector        = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsMenuItemToggle(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuIconBox(icon = icon, tint = BaGold)

        Spacer(Modifier.width(14.dp))

        Text(
            text     = label,
            style    = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )

        Switch(
            checked         = checked,
            onCheckedChange = onChecked,
            colors          = SwitchDefaults.colors(
                checkedThumbColor       = Color.White,
                checkedTrackColor       = BaGold,
                uncheckedThumbColor     = Color.White,
                uncheckedTrackColor     = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun MenuIconBox(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BaGoldLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = tint,
            modifier           = Modifier.size(24.dp)
        )
    }
}
