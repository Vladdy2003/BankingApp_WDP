package com.example.bankingapp.ui.screens.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaPrimaryButton
import com.example.bankingapp.ui.components.BaSecondaryButton
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaLightInk3
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun LoginScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    val vm: LoginViewModel = viewModel()
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onNavigateToMain()
    }

    val ink3 = if (isSystemInDarkTheme()) BaDarkInk3 else BaLightInk3

    Scaffold(
        snackbarHost     = { SnackbarHost(snackbarHostState) },
        containerColor   = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {

            // ── Top space ──────────────────────────────────────────────────────
            Spacer(Modifier.height(48.dp))

            // ── Title ─────────────────────────────────────────────────────────
            Text(
                text  = "Bun venit",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            // ── Subtitle ──────────────────────────────────────────────────────
            Text(
                text  = "Conectați-vă la contul dvs.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(40.dp))

            // ── Email input ───────────────────────────────────────────────────
            BaInput(
                value         = uiState.email,
                onValueChange = vm::onEmailChange,
                label         = "Email",
                keyboardType  = KeyboardType.Email,
                error         = uiState.emailError,
                enabled       = !uiState.isLoading,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── Password input ────────────────────────────────────────────────
            var passwordVisible by remember { mutableStateOf(false) }
            BaInput(
                value                       = uiState.password,
                onValueChange               = vm::onPasswordChange,
                label                       = "Parolă",
                keyboardType                = KeyboardType.Password,
                isPassword                  = true,
                passwordVisible             = passwordVisible,
                onTogglePasswordVisibility  = { passwordVisible = !passwordVisible },
                error                       = uiState.passwordError,
                enabled                     = !uiState.isLoading,
                modifier                    = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // ── Forgot password ───────────────────────────────────────────────
            TextButton(
                onClick  = { /* UI only — no API */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text  = "Ați uitat parola?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Primary button ────────────────────────────────────────────────
            BaPrimaryButton(
                text      = "Conectare",
                onClick   = vm::login,
                isLoading = uiState.isLoading,
                modifier  = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ── "sau" separator ───────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    thickness = 0.5.dp,
                    color     = ink3
                )
                Text(
                    text      = "  sau  ",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = ink3,
                    textAlign = TextAlign.Center
                )
                HorizontalDivider(
                    modifier  = Modifier.weight(1f),
                    thickness = 0.5.dp,
                    color     = ink3
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Secondary button ──────────────────────────────────────────────
            BaSecondaryButton(
                text     = "Nu aveți cont? Înregistrați-vă",
                onClick  = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
