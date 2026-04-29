package com.example.bankingapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.bankingapp.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// ── Font families ─────────────────────────────────────────────────────────────

val DmSerifDisplay = FontFamily(
    Font(
        googleFont = GoogleFont("DM Serif Display"),
        fontProvider = fontProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Normal
    ),
    Font(
        googleFont = GoogleFont("DM Serif Display"),
        fontProvider = fontProvider,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    )
)

val DmSans = FontFamily(
    Font(
        googleFont = GoogleFont("DM Sans"),
        fontProvider = fontProvider,
        weight = FontWeight.Light
    ),
    Font(
        googleFont = GoogleFont("DM Sans"),
        fontProvider = fontProvider,
        weight = FontWeight.Normal
    ),
    Font(
        googleFont = GoogleFont("DM Sans"),
        fontProvider = fontProvider,
        weight = FontWeight.Medium
    )
)

val DmMono = FontFamily(
    Font(
        googleFont = GoogleFont("DM Mono"),
        fontProvider = fontProvider,
        weight = FontWeight.Normal
    )
)

// ── Typography scale ──────────────────────────────────────────────────────────

val BaTypography = Typography(
    // Hero balance display — DM Serif Display 36sp
    displayLarge = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    // Screen titles — DM Serif Display 24sp
    displayMedium = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    // Section title — DM Serif Display 22sp
    displaySmall = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // Card title — DM Serif Display 20sp
    headlineMedium = TextStyle(
        fontFamily = DmSerifDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    // Section heading — DM Sans 14sp/500
    titleLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // UI label — DM Sans 13sp/500
    titleMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // Body — DM Sans 14sp/400
    bodyLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    // Secondary body — DM Sans 13sp/400
    bodyMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),
    // Caption / timestamp — DM Sans 11sp/300
    bodySmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Light,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.03.sp
    ),
    // Button label — DM Sans 14sp/500
    labelLarge = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    // Chip / tag — DM Sans 12sp/500
    labelMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    // Eyebrow — DM Sans 10sp/500 ALL CAPS wide tracking
    labelSmall = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 1.2.sp
    )
)
