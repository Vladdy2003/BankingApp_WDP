package com.example.bankingapp.ui.theme

import androidx.compose.ui.graphics.Color

// ── BankingApp Brand — "Institutional Warmth" ─────────────────────────────────

// Always-dark surface used for SplashScreen + account/card hero headers
val BaObsidian = Color(0xFF141210)

// ── Light Theme ───────────────────────────────────────────────────────────────
val BaLightBg       = Color(0xFFF2EFE9)  // parchment — app background
val BaLightSurface  = Color(0xFFFFFFFF)  // elevated cards, sheets
val BaLightSurface2 = Color(0xFFEDEAE3)  // subtle surfaces, icon backgrounds
val BaLightInk      = Color(0xFF1A1814)  // primary text
val BaLightInk2     = Color(0xFF5C5850)  // secondary text, labels
val BaLightInk3     = Color(0xFF9C9890)  // placeholder, timestamp, tertiary
val BaLightBorder   = Color(0x1F1A1814)  // rgba(26,24,20,0.12)

// ── Dark Theme ────────────────────────────────────────────────────────────────
val BaDarkBg        = Color(0xFF141210)
val BaDarkSurface   = Color(0xFF1E1C18)
val BaDarkSurface2  = Color(0xFF282420)
val BaDarkInk       = Color(0xFFF0EDE5)
val BaDarkInk2      = Color(0xFFA8A49C)
val BaDarkInk3      = Color(0xFF6A665E)
val BaDarkBorder    = Color(0x1AF0EDE5)  // rgba(240,237,229,0.1)

// ── Accent — the only accent colour, used sparingly ──────────────────────────
val BaGold          = Color(0xFFB8965A)  // light mode
val BaGoldDark      = Color(0xFFC9A96A)  // dark mode (slightly lighter)
val BaGoldLight     = Color(0xFFEAD9B8)  // gold tint background
val BaGoldDeep      = Color(0xFF7A5E2A)  // gold on light surfaces (text)
val BaDarkGoldDeep  = Color(0xFFE8CB90)  // gold on dark surfaces (text)
val BaDarkGoldBg    = Color(0xFF3A2E18)  // gold tint background (dark mode)

// ── Semantic ──────────────────────────────────────────────────────────────────
val BaSuccess       = Color(0xFF3A7D5C)  // income, positive, confirmed
val BaSuccessDark   = Color(0xFF4D9E74)
val BaDanger        = Color(0xFFB84040)  // error, negative, debit
val BaDangerDark    = Color(0xFFD45C5C)
