package com.example.bankingapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary            = BaGold,
    onPrimary          = BaLightBg,
    primaryContainer   = BaGoldLight,
    onPrimaryContainer = BaGoldDeep,
    background         = BaLightBg,
    onBackground       = BaLightInk,
    surface            = BaLightSurface,
    onSurface          = BaLightInk,
    surfaceVariant     = BaLightSurface2,
    onSurfaceVariant   = BaLightInk2,
    outline            = BaLightBorder,
    outlineVariant     = BaLightBorder,
    error              = BaDanger,
    onError            = BaLightSurface,
    tertiary           = BaSuccess,
    onTertiary         = BaLightSurface
)

private val DarkColorScheme = darkColorScheme(
    primary            = BaGoldDark,
    onPrimary          = BaDarkBg,
    primaryContainer   = BaDarkGoldBg,
    onPrimaryContainer = BaDarkGoldDeep,
    background         = BaDarkBg,
    onBackground       = BaDarkInk,
    surface            = BaDarkSurface,
    onSurface          = BaDarkInk,
    surfaceVariant     = BaDarkSurface2,
    onSurfaceVariant   = BaDarkInk2,
    outline            = BaDarkBorder,
    outlineVariant     = BaDarkBorder,
    error              = BaDangerDark,
    onError            = BaDarkBg,
    tertiary           = BaSuccessDark,
    onTertiary         = BaDarkBg
)

@Composable
fun BankingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = BaTypography,
        content     = content
    )
}
