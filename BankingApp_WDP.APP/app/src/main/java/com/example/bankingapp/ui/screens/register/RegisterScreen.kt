package com.example.bankingapp.ui.screens.register

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaPrimaryButton
import com.example.bankingapp.ui.components.BaSecondaryButton
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaGoldDark
import com.example.bankingapp.ui.theme.BaLightInk3
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: (email: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val vm: RegisterViewModel = viewModel()
    val uiState by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    // Intercept system back: go to prev step or exit screen
    BackHandler(enabled = uiState.currentStep > 1) { vm.goBack() }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (uiState.isSuccess) {
        SuccessDialog(
            email          = uiState.registeredEmail,
            onConnectClick = onNavigateToLogin
        )
    }

    if (showTermsDialog) {
        TermsDialog(onDismiss = { showTermsDialog = false })
    }

    if (showDatePicker) {
        val eighteenYearsAgo = remember {
            java.util.Calendar.getInstance().apply { add(java.util.Calendar.YEAR, -18) }.timeInMillis
        }
        val dpState = rememberDatePickerState(initialDisplayedMonthMillis = eighteenYearsAgo)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { vm.onDateOfBirthSelected(it) }
                    showDatePicker = false
                }) { Text("Confirmă") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Anulează") }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    // ── Main scaffold ─────────────────────────────────────────────────────────

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Cont Nou",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 1) vm.goBack() else onNavigateBack()
                    }) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint               = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Progress segments ─────────────────────────────────────────
                StepProgressIndicator(currentStep = uiState.currentStep)

                // ── Animated step content ─────────────────────────────────────
                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "register_step"
                ) { step ->
                    when (step) {
                        1 -> Step1PersonalData(
                            uiState          = uiState,
                            onFirstNameChange= vm::onFirstNameChange,
                            onLastNameChange = vm::onLastNameChange,
                            onCnpChange      = vm::onCnpChange,
                            onShowDatePicker = { showDatePicker = true },
                            onContinue       = vm::proceedFromStep1
                        )
                        2 -> Step2Contact(
                            uiState             = uiState,
                            onEmailChange       = vm::onEmailChange,
                            onPhoneNumberChange = vm::onPhoneNumberChange,
                            onAddressChange     = vm::onAddressChange,
                            onBack              = vm::goBack,
                            onContinue          = vm::proceedFromStep2
                        )
                        3 -> Step3Security(
                            uiState                 = uiState,
                            onPasswordChange        = vm::onPasswordChange,
                            onConfirmPasswordChange = vm::onConfirmPasswordChange,
                            onTermsAcceptedChange   = vm::onTermsAcceptedChange,
                            onShowTerms             = { showTermsDialog = true },
                            onBack                  = vm::goBack,
                            onRegister              = vm::register
                        )
                    }
                }
            }

            // ── Loading overlay ───────────────────────────────────────────────
            if (uiState.isLoading) {
                val isDark = isSystemInDarkTheme()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = if (isDark) BaGoldDark else BaGold,
                        strokeWidth = 2.5.dp
                    )
                }
            }
        }
    }
}

// ── Progress indicator ─────────────────────────────────────────────────────────

@Composable
private fun StepProgressIndicator(currentStep: Int) {
    val isDark       = isSystemInDarkTheme()
    val activeColor  = if (isDark) BaGoldDark else BaGold
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier             = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(3) { index ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (index < currentStep) activeColor else inactiveColor
            ) {}
        }
    }
}

// ── Step 1 — Date Personale ────────────────────────────────────────────────────

@Composable
private fun Step1PersonalData(
    uiState: RegisterUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onCnpChange: (String) -> Unit,
    onShowDatePicker: () -> Unit,
    onContinue: () -> Unit
) {
    val ink3 = if (isSystemInDarkTheme()) BaDarkInk3 else BaLightInk3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text  = "01 / 03   DATE PERSONALE",
            style = MaterialTheme.typography.labelSmall,
            color = ink3
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "Spuneți-ne cine sunteți",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(32.dp))

        BaInput(
            value         = uiState.firstName,
            onValueChange = onFirstNameChange,
            label         = "Prenume",
            keyboardType  = KeyboardType.Text,
            error         = uiState.firstNameError,
            enabled       = !uiState.isLoading,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        BaInput(
            value         = uiState.lastName,
            onValueChange = onLastNameChange,
            label         = "Nume",
            keyboardType  = KeyboardType.Text,
            error         = uiState.lastNameError,
            enabled       = !uiState.isLoading,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        DateField(
            value    = uiState.dateOfBirthDisplay,
            label    = "Data nașterii",
            error    = uiState.dateOfBirthError,
            onClick  = onShowDatePicker,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // CNP — DM Mono style via custom field
        CnpField(
            value         = uiState.cnp,
            onValueChange = onCnpChange,
            error         = uiState.cnpError,
            enabled       = !uiState.isLoading,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        BaPrimaryButton(
            text      = "Continuă",
            onClick   = onContinue,
            isLoading = uiState.isLoading,
            modifier  = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ── Step 2 — Contact ──────────────────────────────────────────────────────────

@Composable
private fun Step2Contact(
    uiState: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val ink3 = if (isSystemInDarkTheme()) BaDarkInk3 else BaLightInk3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text  = "02 / 03   CONTACT",
            style = MaterialTheme.typography.labelSmall,
            color = ink3
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "Cum vă contactăm?",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(32.dp))

        BaInput(
            value         = uiState.email,
            onValueChange = onEmailChange,
            label         = "Email",
            keyboardType  = KeyboardType.Email,
            error         = uiState.emailError,
            enabled       = !uiState.isLoading,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        PhoneField(
            value         = uiState.phoneNumber,
            onValueChange = onPhoneNumberChange,
            error         = uiState.phoneNumberError,
            enabled       = !uiState.isLoading,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        AddressField(
            value         = uiState.address,
            onValueChange = onAddressChange,
            error         = uiState.addressError,
            enabled       = !uiState.isLoading,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        Row(
            modifier             = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BaSecondaryButton(
                text     = "Înapoi",
                onClick  = onBack,
                modifier = Modifier.weight(1f)
            )
            BaPrimaryButton(
                text     = "Continuă",
                onClick  = onContinue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Step 3 — Securitate ───────────────────────────────────────────────────────

@Composable
private fun Step3Security(
    uiState: RegisterUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onShowTerms: () -> Unit,
    onBack: () -> Unit,
    onRegister: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val ink3   = if (isDark) BaDarkInk3 else BaLightInk3
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text  = "03 / 03   SECURITATE",
            style = MaterialTheme.typography.labelSmall,
            color = ink3
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text  = "Creați parola",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(32.dp))

        BaInput(
            value                      = uiState.password,
            onValueChange              = onPasswordChange,
            label                      = "Parolă",
            keyboardType               = KeyboardType.Password,
            isPassword                 = true,
            passwordVisible            = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            error                      = uiState.passwordError,
            enabled                    = !uiState.isLoading,
            modifier                   = Modifier.fillMaxWidth()
        )

        if (uiState.password.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PasswordStrengthBar(strength = uiState.passwordStrength, isDark = isDark)
        }

        Spacer(Modifier.height(12.dp))

        BaInput(
            value                      = uiState.confirmPassword,
            onValueChange              = onConfirmPasswordChange,
            label                      = "Confirmare parolă",
            keyboardType               = KeyboardType.Password,
            isPassword                 = true,
            passwordVisible            = confirmVisible,
            onTogglePasswordVisibility = { confirmVisible = !confirmVisible },
            error                      = uiState.confirmPasswordError,
            enabled                    = !uiState.isLoading,
            modifier                   = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Terms checkbox
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier          = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked        = uiState.termsAccepted,
                    onCheckedChange = onTermsAcceptedChange,
                    enabled        = !uiState.isLoading,
                    colors         = CheckboxDefaults.colors(
                        checkedColor   = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = buildAnnotatedString {
                        append("Accept ")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("termenii și condițiile")
                        }
                    },
                    style   = MaterialTheme.typography.bodyMedium,
                    color   = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { onShowTerms() }
                )
            }
            if (uiState.termsError != null) {
                Text(
                    text     = uiState.termsError,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier             = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BaSecondaryButton(
                text     = "Înapoi",
                onClick  = onBack,
                modifier = Modifier.weight(1f)
            )
            BaPrimaryButton(
                text      = "Creează Cont",
                onClick   = onRegister,
                isLoading = uiState.isLoading,
                modifier  = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

// ── Shared custom fields ───────────────────────────────────────────────────────

@Composable
private fun DateField(
    value: String,
    label: String,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayValue = value.ifEmpty { "" }
    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value           = displayValue,
                onValueChange   = {},
                label           = { Text(label, style = MaterialTheme.typography.bodySmall) },
                enabled         = false,
                isError         = error != null,
                trailingIcon    = {
                    Icon(
                        imageVector        = Icons.Default.DateRange,
                        contentDescription = "Selectați data",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape  = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor       = MaterialTheme.colorScheme.onBackground,
                    disabledBorderColor     = if (error != null) MaterialTheme.colorScheme.error
                                             else MaterialTheme.colorScheme.outline,
                    disabledLabelColor      = if (error != null) MaterialTheme.colorScheme.error
                                             else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
            // Transparent overlay to capture clicks on disabled field
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onClick() }
            )
        }
        if (error != null) {
            Text(
                text     = error,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun CnpField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val dmMono = MaterialTheme.typography.bodyLarge.copy(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
    Column(modifier = modifier) {
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            label           = { Text("CNP", style = MaterialTheme.typography.bodySmall) },
            singleLine      = true,
            enabled         = enabled,
            isError         = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle       = dmMono,
            shape           = RoundedCornerShape(10.dp),
            colors          = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                errorContainerColor     = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                errorBorderColor        = MaterialTheme.colorScheme.error,
                cursorColor             = MaterialTheme.colorScheme.primary
            ),
            supportingText = if (error != null) {
                { Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            label           = { Text("Număr Telefon", style = MaterialTheme.typography.bodySmall) },
            singleLine      = true,
            enabled         = enabled,
            isError         = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            prefix          = {
                Text(
                    text  = "+373 ",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            shape  = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                errorContainerColor     = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                errorBorderColor        = MaterialTheme.colorScheme.error,
                cursorColor             = MaterialTheme.colorScheme.primary
            ),
            supportingText = if (error != null) {
                { Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AddressField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            label           = { Text("Adresă", style = MaterialTheme.typography.bodySmall) },
            singleLine      = false,
            maxLines        = 3,
            enabled         = enabled,
            isError         = error != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape           = RoundedCornerShape(10.dp),
            colors          = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                errorContainerColor     = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor  = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                focusedBorderColor      = MaterialTheme.colorScheme.primary,
                errorBorderColor        = MaterialTheme.colorScheme.error,
                cursorColor             = MaterialTheme.colorScheme.primary
            ),
            supportingText = if (error != null) {
                { Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ── Password strength bar ──────────────────────────────────────────────────────

@Composable
private fun PasswordStrengthBar(strength: PasswordStrength, isDark: Boolean) {
    val (barColor, label) = when (strength) {
        PasswordStrength.NONE   -> Color.Transparent to ""
        PasswordStrength.WEAK   -> (if (isDark) BaDangerDark else BaDanger) to "Slabă"
        PasswordStrength.MEDIUM -> (if (isDark) BaGoldDark else BaGold) to "Medie"
        PasswordStrength.STRONG -> (if (isDark) BaSuccessDark else BaSuccess) to "Puternică"
    }
    val filledCount = when (strength) {
        PasswordStrength.NONE   -> 0
        PasswordStrength.WEAK   -> 1
        PasswordStrength.MEDIUM -> 2
        PasswordStrength.STRONG -> 3
    }
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Column {
        Row(
            modifier             = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (index < filledCount) barColor else inactiveColor
                ) {}
            }
        }
        if (label.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.bodySmall,
                color = barColor
            )
        }
    }
}

// ── Dialogs ────────────────────────────────────────────────────────────────────

@Composable
private fun SuccessDialog(
    email: String,
    onConnectClick: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    AlertDialog(
        onDismissRequest = {},
        icon = {
            Icon(
                imageVector        = Icons.Default.CheckCircle,
                contentDescription = null,
                tint               = if (isDark) BaSuccessDark else BaSuccess,
                modifier           = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text  = "Cont creat!",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text  = "Contul dvs. a fost creat cu succes. Vă puteți autentifica acum.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            BaPrimaryButton(
                text     = "Conectare",
                onClick  = { onConnectClick(email) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun TermsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text  = "Termeni și Condiții",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text  = "Prin crearea unui cont în aplicația BankingApp, acceptați că datele " +
                            "dvs. personale vor fi procesate în conformitate cu legislația " +
                            "în vigoare privind protecția datelor (GDPR).\n\n" +
                            "Contul dvs. este destinat exclusiv uzului personal. Este interzisă " +
                            "utilizarea contului în scopuri ilegale sau frauduloase.\n\n" +
                            "BankingApp își rezervă dreptul de a suspenda sau închide conturi " +
                            "care încalcă acești termeni, fără notificare prealabilă.\n\n" +
                            "Pentru orice întrebări, contactați suportul la support@bankingapp.md.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Închide", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
