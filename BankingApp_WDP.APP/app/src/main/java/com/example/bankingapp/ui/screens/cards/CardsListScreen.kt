package com.example.bankingapp.ui.screens.cards

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.ui.components.BaGhostButton
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaPrimaryButton
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaDarkSurface
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaGoldDark
import com.example.bankingapp.ui.theme.BaLightBg
import com.example.bankingapp.ui.theme.BaLightInk3
import com.example.bankingapp.ui.theme.BaObsidian
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsListScreen(
    onNavigateToCardDetail: (String) -> Unit = {},
    viewModel: CardsListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Refresh la fiecare revenire în tab (după prima încărcare)
    LaunchedEffect(Unit) { viewModel.refreshOnResume() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
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
                        text  = "Carduri",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateSheet() }) {
                        Icon(
                            imageVector        = Icons.Default.Add,
                            contentDescription = "Card nou",
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
                        modifier    = Modifier.align(Alignment.Center),
                        color       = BaGold,
                        strokeWidth = 2.dp
                    )
                }

                uiState.cards.isEmpty() -> {
                    Column(
                        modifier              = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment   = Alignment.CenterHorizontally,
                        verticalArrangement   = Arrangement.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.CreditCard,
                            contentDescription = null,
                            modifier           = Modifier.size(56.dp),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text  = "Niciun card emis",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        BaPrimaryButton(
                            text     = "Emite primul card",
                            onClick  = { viewModel.showCreateSheet() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items = uiState.cards, key = { it.id }) { card ->
                            CardItem(
                                card              = card,
                                isBlocking        = uiState.blockingCardId == card.id,
                                onBlock           = { viewModel.blockCard(card.id) },
                                onUnblock         = { viewModel.unblockCard(card.id) },
                                onNavigateToDetail = { onNavigateToCardDetail(card.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showCreateSheet) {
        CreateCardBottomSheet(
            uiState              = uiState,
            onDismiss            = { viewModel.hideCreateSheet() },
            onAccountSelected    = { viewModel.setSelectedAccount(it) },
            onTypeSelected       = { viewModel.setSelectedType(it) },
            onDailyLimitChanged  = { viewModel.setDailyLimit(it) },
            onMonthlyLimitChanged = { viewModel.setMonthlyLimit(it) },
            onSubmit             = { viewModel.createCard() }
        )
    }
}

// ─── CardItem ─────────────────────────────────────────────────────────────────

@Composable
private fun CardItem(
    card: CardResponse,
    isBlocking: Boolean,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onNavigateToDetail: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Column {
        BankCard(card = card)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            CardStatusBadge(status = card.status, isDark = isDark)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (card.status) {
                    "Active", "0" -> {
                        val dangerColor = if (isDark) BaDangerDark else BaDanger
                        OutlinedButton(
                            onClick  = onBlock,
                            enabled  = !isBlocking,
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = dangerColor
                            ),
                            border   = androidx.compose.foundation.BorderStroke(1.dp, dangerColor)
                        ) {
                            if (isBlocking) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(16.dp),
                                    color       = dangerColor,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text  = "Blochează",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                    "Blocked", "1" -> {
                        val successColor = if (isDark) BaSuccessDark else BaSuccess
                        OutlinedButton(
                            onClick  = onUnblock,
                            enabled  = !isBlocking,
                            shape    = RoundedCornerShape(8.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(
                                contentColor = successColor
                            ),
                            border   = androidx.compose.foundation.BorderStroke(1.dp, successColor)
                        ) {
                            if (isBlocking) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(16.dp),
                                    color       = successColor,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text  = "Deblochează",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                    // AdminBlocked: card locked by admin — user cannot unblock, no action buttons
                    "AdminBlocked", "5" -> { /* no action buttons — only Detalii shown below */ }
                    // Cancelled / Expired: card is closed — no action buttons
                    "Cancelled", "4", "Expired", "2" -> { /* no action buttons */ }
                }
                BaGhostButton(
                    text    = "Detalii",
                    onClick = onNavigateToDetail
                )
            }
        }
    }
}

// ─── BankCard ─────────────────────────────────────────────────────────────────

@Composable
private fun BankCard(card: CardResponse) {
    val goldAccent     = if (isSystemInDarkTheme()) BaGoldDark else BaGold
    val isBlocked      = card.status == "Blocked"      || card.status == "1"
    val isAdminBlocked = card.status == "AdminBlocked"  || card.status == "5"
    val isExpired      = card.status == "Expired"       || card.status == "2"
    val isCancelled    = card.status == "Cancelled"     || card.status == "4"
    val last4      = card.maskedCardNumber.filter { it.isDigit() }.takeLast(4).ifEmpty { "••••" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(24.dp))
            .drawBehind {
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(BaObsidian, BaDarkSurface),
                        start  = Offset.Zero,
                        end    = Offset(size.width, size.height)
                    )
                )
                // shimmer auriu subtil — linie diagonală decorativă
                drawLine(
                    color       = goldAccent.copy(alpha = 0.18f),
                    start       = Offset(size.width * 0.55f, 0f),
                    end         = Offset(size.width, size.height * 0.55f),
                    strokeWidth = 80f
                )
            }
            .padding(20.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Eyebrow tip card
            Text(
                text  = cardTypeLabel(card.type),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = goldAccent
            )

            // Număr card centrat
            Text(
                text       = "•••• •••• •••• $last4",
                style      = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 16.sp
                ),
                color      = BaLightBg,
                modifier   = Modifier.align(Alignment.CenterHorizontally)
            )

            // Bottom row: expiry + IBAN cont
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Bottom
            ) {
                Text(
                    text  = "EXPIRĂ ${card.expiryDate}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 11.sp
                    ),
                    color = BaLightBg.copy(alpha = 0.60f)
                )
                Text(
                    text      = maskAccountId(card.accountId),
                    style     = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 11.sp
                    ),
                    color     = BaLightBg.copy(alpha = 0.50f),
                    textAlign = TextAlign.End
                )
            }
        }

        // Overlay BLOCAT (user-blocked)
        if (isBlocked) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BaDanger.copy(alpha = 0.30f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "BLOCAT",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }

        // Overlay BLOCAT ADMIN
        if (isAdminBlocked) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BaDanger.copy(alpha = 0.50f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "BLOCAT",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text  = "DE ADMINISTRATOR",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.80f)
                    )
                }
            }
        }

        // Overlay EXPIRAT
        if (isExpired) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BaDarkInk3.copy(alpha = 0.40f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "EXPIRAT",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }

        // Overlay ANULAT
        if (isCancelled) {
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BaDarkInk3.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "ANULAT",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }
    }
}

// ─── CardStatusBadge ──────────────────────────────────────────────────────────

@Composable
private fun CardStatusBadge(status: String, isDark: Boolean) {
    val (label, textColor, bgColor) = when (status) {
        "Active",       "0" -> Triple("ACTIV",        if (isDark) BaSuccessDark else BaSuccess, (if (isDark) BaSuccessDark else BaSuccess).copy(alpha = 0.15f))
        "Blocked",      "1" -> Triple("BLOCAT",       if (isDark) BaDangerDark  else BaDanger,  (if (isDark) BaDangerDark  else BaDanger).copy(alpha = 0.15f))
        "Expired",      "2" -> Triple("EXPIRAT",      Color.Gray, Color.Gray.copy(alpha = 0.15f))
        "Pending",      "3" -> Triple("PENDING",      BaGold, BaGold.copy(alpha = 0.15f))
        "Cancelled",    "4" -> Triple("ANULAT",       Color.Gray, Color.Gray.copy(alpha = 0.15f))
        "AdminBlocked", "5" -> Triple("BLOCAT ADMIN", if (isDark) BaDangerDark  else BaDanger,  (if (isDark) BaDangerDark  else BaDanger).copy(alpha = 0.20f))
        else                -> Triple(status.uppercase(), Color.Gray, Color.Gray.copy(alpha = 0.15f))
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

// ─── CreateCardBottomSheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCardBottomSheet(
    uiState: CardsListUiState,
    onDismiss: () -> Unit,
    onAccountSelected: (String) -> Unit,
    onTypeSelected: (Int) -> Unit,
    onDailyLimitChanged: (String) -> Unit,
    onMonthlyLimitChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text     = "Card Nou",
                style    = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // ── Cont asociat ──────────────────────────────────────────────────
            Text(
                text     = "CONT ASOCIAT",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            AccountDropdown(
                accounts          = uiState.accounts,
                selectedAccountId = uiState.selectedAccountId,
                onAccountSelected = onAccountSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Tip card ──────────────────────────────────────────────────────
            Text(
                text     = "TIP CARD",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val cardTypes = listOf(
                Triple(0, "DEBIT",   "Standard, limită zilnică 1.000 MDL"),
                Triple(1, "CREDIT",  "Premium, limită zilnică 5.000 MDL"),
                Triple(2, "PREPAID", "Prepaid, limită zilnică personalizată")
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cardTypes.forEach { (typeInt, displayName, description) ->
                    CardTypeCard(
                        displayName = displayName,
                        description = description,
                        isSelected  = uiState.selectedType == typeInt,
                        onClick     = { onTypeSelected(typeInt) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Limite ────────────────────────────────────────────────────────
            Text(
                text     = "LIMITE",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            BaInput(
                value         = uiState.dailyLimit,
                onValueChange = onDailyLimitChanged,
                label         = "Limită zilnică (MDL)",
                keyboardType  = KeyboardType.Decimal,
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            BaInput(
                value         = uiState.monthlyLimit,
                onValueChange = onMonthlyLimitChanged,
                label         = "Limită lunară (MDL)",
                keyboardType  = KeyboardType.Decimal,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            BaPrimaryButton(
                text      = "Emite Card",
                onClick   = onSubmit,
                isLoading = uiState.isCreating,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── AccountDropdown ──────────────────────────────────────────────────────────

@Composable
private fun AccountDropdown(
    accounts: List<AccountResponse>,
    selectedAccountId: String,
    onAccountSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.firstOrNull { it.id == selectedAccountId }
    val isDark   = isSystemInDarkTheme()
    val ink3     = if (isDark) BaDarkInk3 else BaLightInk3

    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            if (selected != null) {
                Column {
                    Text(
                        text  = accountTypeDisplay(selected.type),
                        style = MaterialTheme.typography.labelSmall,
                        color = BaGold
                    )
                    Text(
                        text  = maskIban(selected.iban),
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Text(
                    text  = if (accounts.isEmpty()) "Niciun cont activ disponibil" else "Selectați un cont",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ink3
                )
            }
        }

        DropdownMenu(
            expanded        = expanded,
            onDismissRequest = { expanded = false }
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text  = accountTypeDisplay(account.type),
                                style = MaterialTheme.typography.labelSmall,
                                color = BaGold
                            )
                            Text(
                                text  = maskIban(account.iban),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    },
                    onClick = {
                        onAccountSelected(account.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─── CardTypeCard ─────────────────────────────────────────────────────────────

@Composable
private fun CardTypeCard(
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

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun cardTypeLabel(type: String): String = when (type) {
    "Debit",   "0" -> "DEBIT"
    "Credit",  "1" -> "CREDIT"
    "Prepaid", "2" -> "PREPAID"
    else           -> type.uppercase()
}

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

private fun maskAccountId(accountId: String): String {
    if (accountId.length <= 4) return accountId
    return "••••${accountId.takeLast(4)}"
}
