package com.example.bankingapp.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaPrimaryButton
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaLightInk3
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsListScreen(
    onNavigateToAccountDetail: (String) -> Unit = {},
    viewModel: AccountsListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Refresh silențios de fiecare dată când tab-ul intră în compoziție
    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.createSuccess) {
        uiState.createSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCreateSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Conturi",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateSheet() }) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = "Cont nou",
                            tint               = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color    = BaGold,
                        strokeWidth = 2.dp
                    )
                }

                uiState.accounts.isEmpty() -> {
                    Column(
                        modifier              = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment   = Alignment.CenterHorizontally,
                        verticalArrangement   = Arrangement.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier           = Modifier.size(56.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text  = "Niciun cont",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        BaPrimaryButton(
                            text    = "Deschide primul cont",
                            onClick = { viewModel.showCreateSheet() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = uiState.accounts, key = { it.id }) { account ->
                            AccountListItem(
                                account = account,
                                onClick = { onNavigateToAccountDetail(account.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateSheet) {
        CreateAccountBottomSheet(
            uiState                = uiState,
            onDismiss              = { viewModel.hideCreateSheet() },
            onTypeSelected         = { viewModel.setSelectedType(it) },
            onCurrencySelected     = { viewModel.setSelectedCurrency(it) },
            onOverdraftLimitChanged = { viewModel.setOverdraftLimit(it) },
            onCompanyNameChanged   = { viewModel.setCompanyName(it) },
            onSubmit               = { viewModel.createAccount() }
        )
    }
}

// ─── AccountListItem ─────────────────────────────────────────────────────────

@Composable
private fun AccountListItem(
    account: AccountResponse,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val ink3   = if (isDark) BaDarkInk3 else BaLightInk3

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            // Row 1: type eyebrow + status badge
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                Text(
                    text  = accountTypeDisplay(account.type),
                    style = MaterialTheme.typography.labelSmall,
                    color = BaGold
                )
                AccountStatusBadge(status = account.status, isDark = isDark)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Balance
            Text(
                text  = formatBalance(account.balance),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontStyle = FontStyle.Italic,
                    fontSize  = 28.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text  = account.currency,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Masked IBAN (monospace)
            Text(
                text       = maskIban(account.iban),
                style      = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                fontSize   = 13.sp,
                color      = ink3
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outline,
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Created date
            Text(
                text  = "Creat ${formatAccountDate(account.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = ink3
            )
        }
    }
}

@Composable
private fun AccountStatusBadge(status: String, isDark: Boolean) {
    // Backend sends AccountStatus as integer: 0=Active, 1=Inactive, 2=Suspended, 3=Closed
    val (label, textColor, bgColor) = when (status) {
        "Active",    "0" -> Triple("ACTIV",     if (isDark) BaSuccessDark else BaSuccess, (if (isDark) BaSuccessDark else BaSuccess).copy(alpha = 0.15f))
        "Inactive",  "1" -> Triple("INACTIV",   Color.Gray,                               Color.Gray.copy(alpha = 0.15f))
        "Suspended", "2" -> Triple("SUSPENDAT", BaGold,                                   BaGold.copy(alpha = 0.15f))
        "Closed",    "3" -> Triple("ÎNCHIS",    if (isDark) BaDangerDark  else BaDanger,  (if (isDark) BaDangerDark  else BaDanger).copy(alpha = 0.15f))
        else             -> Triple(status.uppercase(), Color.Gray, Color.Gray.copy(alpha = 0.15f))
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

// ─── CreateAccountBottomSheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAccountBottomSheet(
    uiState: AccountsListUiState,
    onDismiss: () -> Unit,
    onTypeSelected: (String) -> Unit,
    onCurrencySelected: (String) -> Unit,
    onOverdraftLimitChanged: (String) -> Unit,
    onCompanyNameChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest    = onDismiss,
        sheetState          = sheetState,
        containerColor      = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text  = "Cont Nou",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // ── Tip cont ────────────────────────────────────────────────────
            Text(
                text  = "TIP CONT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val types = listOf(
                Triple("Current",  "CURENT",   "Tranzacții zilnice, overdraft disponibil"),
                Triple("Savings",  "ECONOMII", "Dobândă lunară automată"),
                Triple("Business", "BUSINESS", "Pentru activitate comercială")
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { (apiValue, displayName, description) ->
                    TypeCard(
                        displayName = displayName,
                        description = description,
                        isSelected  = uiState.selectedType == apiValue,
                        onClick     = { onTypeSelected(apiValue) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Monedă ───────────────────────────────────────────────────────
            Text(
                text  = "MONEDĂ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("MDL", "EUR", "USD").forEach { currency ->
                    CurrencyChip(
                        label      = currency,
                        isSelected = uiState.selectedCurrency == currency,
                        onClick    = { onCurrencySelected(currency) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Câmpuri condiționale ──────────────────────────────────────────
            when (uiState.selectedType) {
                "Current" -> {
                    BaInput(
                        value          = uiState.overdraftLimit,
                        onValueChange  = onOverdraftLimitChanged,
                        label          = "Limită overdraft (opțional)",
                        keyboardType   = KeyboardType.Decimal,
                        modifier       = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                "Business" -> {
                    BaInput(
                        value         = uiState.companyName,
                        onValueChange = onCompanyNameChanged,
                        label         = "Nume companie",
                        error         = uiState.companyNameError,
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── Buton submit ─────────────────────────────────────────────────
            BaPrimaryButton(
                text      = "Deschide Cont",
                onClick   = onSubmit,
                isLoading = uiState.isCreating,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TypeCard(
    displayName: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) BaGold else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) BaGold else MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text  = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BaGold)
                )
            }
        }
    }
}

@Composable
private fun CurrencyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) BaGold.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) BaGold else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text      = label,
            style     = MaterialTheme.typography.titleMedium,
            color     = if (isSelected) BaGold else MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

// Backend sends enums as integers (0/1/2) which Gson converts to strings "0"/"1"/"2"
private fun accountTypeDisplay(type: String): String = when (type) {
    "Current",  "0" -> "CURENT"
    "Savings",  "1" -> "ECONOMII"
    "Business", "2" -> "BUSINESS"
    else            -> type.uppercase()
}

private fun maskIban(iban: String): String {
    if (iban.length <= 6) return iban
    val prefix       = iban.take(4)
    val suffix       = iban.takeLast(2)
    val middleLength = iban.length - 6
    val masked       = prefix + "•".repeat(middleLength) + suffix
    return masked.chunked(4).joinToString(" ")
}

private fun formatBalance(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        "%,.0f".format(amount)
    } else {
        "%,.2f".format(amount)
    }
}

private fun formatAccountDate(isoDate: String): String {
    return try {
        val parsers = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",     Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",        Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd",                   Locale.getDefault())
        )
        val date = parsers.firstNotNullOfOrNull { fmt ->
            runCatching { fmt.parse(isoDate) }.getOrNull()
        } ?: return isoDate.take(10)
        SimpleDateFormat("d MMM yyyy", Locale("ro", "RO")).format(date)
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
