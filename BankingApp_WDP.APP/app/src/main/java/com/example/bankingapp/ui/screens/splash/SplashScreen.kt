package com.example.bankingapp.ui.screens.splash

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaLightBg
import com.example.bankingapp.ui.theme.BaObsidian
import com.example.bankingapp.ui.theme.DmSans
import com.example.bankingapp.ui.theme.DmSerifDisplay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Force light (white) system bar icons on the dark obsidian background
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    }

    // Individual animation states for staggered entrance + coordinated exit
    val logoAlpha   = remember { Animatable(0f) }
    val logoOffsetY = remember { Animatable(10f) }
    val lineAlpha   = remember { Animatable(0f) }
    val tagAlpha    = remember { Animatable(0f) }
    val tagOffsetY  = remember { Animatable(10f) }

    // Staggered fade-in + slide-up on first composition
    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }
        launch {
            logoOffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        }
        delay(60)
        launch {
            lineAlpha.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
        }
        delay(60)
        launch {
            tagAlpha.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
        }
        launch {
            tagOffsetY.animateTo(0f, tween(280, easing = FastOutSlowInEasing))
        }
    }

    // Navigate once the ViewModel resolves the destination
    LaunchedEffect(destination) {
        if (destination == SplashViewModel.Destination.Loading) return@LaunchedEffect

        // Coordinated fade-out (200ms) before navigating
        scope.launch {
            launch { logoAlpha.animateTo(0f, tween(200)) }
            launch { lineAlpha.animateTo(0f, tween(200)) }
            launch { tagAlpha.animateTo(0f, tween(200)) }
        }
        delay(220)

        when (destination) {
            SplashViewModel.Destination.Login -> onNavigateToLogin()
            SplashViewModel.Destination.Main  -> onNavigateToMain()
            else                              -> Unit
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier        = Modifier
            .fillMaxSize()
            .background(BaObsidian),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo — DM Serif Display Italic, parchment on obsidian
            Text(
                text  = "BankingApp",
                style = TextStyle(
                    fontFamily  = DmSerifDisplay,
                    fontSize    = 32.sp,
                    fontWeight  = FontWeight.Normal,
                    fontStyle   = FontStyle.Italic,
                    color       = BaLightBg,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.graphicsLayer {
                    alpha        = logoAlpha.value
                    translationY = logoOffsetY.value
                }
            )

            Spacer(Modifier.height(10.dp))

            // Decorative gold line — 40dp × 1dp
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(1.dp)
                    .graphicsLayer { alpha = lineAlpha.value }
                    .background(BaGold)
            )

            Spacer(Modifier.height(16.dp))

            // Tagline — DM Sans Light, tertiary tone
            Text(
                text  = "Simplu. Rapid. Sigur.",
                style = TextStyle(
                    fontFamily    = DmSans,
                    fontSize      = 12.sp,
                    fontWeight    = FontWeight.Light,
                    color         = BaLightBg.copy(alpha = 0.45f),
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.graphicsLayer {
                    alpha        = tagAlpha.value
                    translationY = tagOffsetY.value
                }
            )
        }
    }
}
