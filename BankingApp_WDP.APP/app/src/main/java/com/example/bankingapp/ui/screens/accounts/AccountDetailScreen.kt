package com.example.bankingapp.ui.screens.accounts

import android.widget.Toast
import com.example.bankingapp.ui.screens.transactions.DepositBottomSheet
import com.example.bankingapp.ui.screens.transactions.TransactionSheetViewModel
import com.example.bankingapp.ui.screens.transactions.WithdrawBottomSheet
import com.example.bankingapp.ui.screens.transactions.TransferBottomSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaPrimaryButton
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaGoldDark
import com.example.bankingapp.ui.theme.BaLightBg
import com.example.bankingapp.ui.theme.BaObsidian
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    accountId: String,
    onNavigateBack: () -> Unit,
    viewModel: AccountDetailViewModel = viewModel(),
    sheetViewModel: TransactionSheetViewModel = viewModel()
) {
    val uiState  by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val context  = LocalContext.current
    val isDark   = isSystemInDarkTheme()
    val listState = rememberLazyListState()

    var showDepositSheet  by remember { mutableStateOf(false) }
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showTransferSheet by remember { mutableStateOf(false) }

    val sheetState by sheetViewModel.uiState.collectAsState()
    LaunchedEffect(sheetState.successMessage) {
        sheetState.successMessage?.let { msg ->
            snackbar.showSnackbar(msg)
            sheetViewModel.clearSuccess()
            viewModel.loadAccount()
            viewModel.loadTransactions(reset = true)
        }
    }

    // Init (guard against double-init on recompose)
    LaunchedEffect(accountId) { viewModel.init(accountId) }

    // Navigate back after successful account closure
    LaunchedEffect(uiState.accountClosed) {
        if (uiState.accountClosed) onNavigateBack()
    }

    // Snackbars
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(uiState.updateSuccess) {
        uiState.updateSuccess?.let { snackbar.showSnackbar(it); viewModel.clearUpdateSuccess() }
    }

    // Infinite scroll — trigger load-more when user reaches bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward }
            .collect { canScroll ->
                if (!canScroll
                    && uiState.transactions.isNotEmpty()
                    && uiState.hasMore
                    && !uiState.isLoadingMore
                ) {
                    viewModel.loadTransactions()
                }
            }
    }

    // Client-side filter
    val filteredTx = remember(uiState.transactions, uiState.selectedFilter) {
        when (uiState.selectedFilter) {
            "Deposit"    -> uiState.transactions.filter { t ->
                t.type.equals("Deposit",    ignoreCase = true) || t.type == "0"
            }
            "Withdrawal" -> uiState.transactions.filter { t ->
                t.type.equals("Withdrawal", ignoreCase = true) || t.type == "1"
            }
            "Transfer"   -> uiState.transactions.filter { t ->
                t.type.equals("Transfer",   ignoreCase = true) || t.type == "2"
            }
            else -> uiState.transactions
        }
    }

    // ── TopBar ⋮ menu state
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Înapoi",
                            tint               = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                title = {
                    Text(
                        text  = accountTypeTitle(uiState.account?.type ?: ""),
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector        = Icons.Default.MoreVert,
                                contentDescription = "Opțiuni",
                                tint               = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded         = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text    = { Text("Editează detalii") },
                                onClick = { menuExpanded = false; viewModel.showEditSheet() }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Închide Cont",
                                        color = if (isDark) BaDangerDark else BaDanger
                                    )
                                },
                                onClick = { menuExpanded = false; viewModel.showCloseDialog() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost   = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = if (isDark) BaGoldDark else BaGold)
            }
            return@Scaffold
        }

        val account = uiState.account
        if (account == null) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "Contul nu a putut fi încărcat.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── 1: Hero header ───────────────────────────────────
            item {
                HeroHeader(account = account, isDark = isDark, context = context)
            }

            // ── 2: Quick actions ─────────────────────────────────
            item {
                QuickActionsRow(
                    isDark     = isDark,
                    onDeposit  = { sheetViewModel.resetSheet(); showDepositSheet  = true },
                    onWithdraw = { sheetViewModel.resetSheet(); showWithdrawSheet = true },
                    onTransfer = { sheetViewModel.resetSheet(); showTransferSheet = true }
                )
            }

            // ── 3: Info card ─────────────────────────────────────
            item {
                InfoCard(account = account, isDark = isDark)
            }

            // ── 4: Transaction list header + filters ─────────────
            item {
                TransactionSectionHeader(
                    selectedFilter   = uiState.selectedFilter,
                    onFilterSelected = viewModel::setFilter,
                    isDark           = isDark
                )
            }

            // ── 5: Transactions ───────────────────────────────────
            if (filteredTx.isEmpty() && !uiState.isLoadingMore) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "Nicio tranzacție",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredTx, key = { it.id }) { tx ->
                    TransactionDetailItem(transaction = tx, isDark = isDark)
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outline,
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // ── 6: Load-more indicator ────────────────────────────
            if (uiState.isLoadingMore) {
                item {
                    Box(
                        modifier         = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(24.dp),
                            color       = if (isDark) BaGoldDark else BaGold,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }

    // ── Edit bottom sheet ─────────────────────────────────────────────────────
    if (uiState.showEditSheet && uiState.account != null) {
        EditAccountBottomSheet(
            uiState               = uiState,
            account               = uiState.account!!,
            onDismiss             = viewModel::hideEditSheet,
            onCurrencyChanged     = viewModel::setEditCurrency,
            onOverdraftChanged    = viewModel::setEditOverdraftLimit,
            onInterestRateChanged = viewModel::setEditInterestRate,
            onCompanyNameChanged  = viewModel::setEditCompanyName,
            onSubmit              = viewModel::updateAccount
        )
    }

    // ── Transaction sheets ────────────────────────────────────────────────────
    val preselected = uiState.account?.id ?: ""
    if (showDepositSheet) {
        DepositBottomSheet(
            preselectedAccountId = preselected,
            onDismiss            = { showDepositSheet = false },
            onSuccess            = { _ -> },
            sheetViewModel       = sheetViewModel
        )
    }
    if (showWithdrawSheet) {
        WithdrawBottomSheet(
            preselectedAccountId = preselected,
            onDismiss            = { showWithdrawSheet = false },
            onSuccess            = { _ -> },
            sheetViewModel       = sheetViewModel
        )
    }
    if (showTransferSheet) {
        TransferBottomSheet(
            preselectedAccountId = preselected,
            onDismiss            = { showTransferSheet = false },
            onSuccess            = { _ -> },
            sheetViewModel       = sheetViewModel
        )
    }

    // ── Close account dialog ──────────────────────────────────────────────────
    if (uiState.showCloseDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideCloseDialog,
            title = {
                Text(
                    text  = "Închideți contul?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text  = "Această acțiune este ireversibilă. Asigurați-vă că soldul este 0.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::closeAccount,
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text  = "Închide Cont",
                            color = if (isDark) BaDangerDark else BaDanger
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideCloseDialog) {
                    Text("Anulează")
                }
            }
        )
    }
}

// ─── HeroHeader ───────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(
    account: AccountResponse,
    isDark: Boolean,
    context: android.content.Context
) {
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(BaObsidian, Color(0xFF2A2620)),
                        start  = Offset.Zero,
                        end    = Offset(size.width, size.height)
                    )
                )
            }
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Eyebrow: account type ALL CAPS
            Text(
                text  = accountTypeLabel(account.type),
                style = MaterialTheme.typography.labelSmall.copy(
                    color         = BaGold,
                    fontSize      = 10.sp,
                    letterSpacing = 1.2.sp
                )
            )
            Spacer(Modifier.height(8.dp))

            // Hero balance
            Text(
                text  = formatHeroBalance(account.balance),
                style = MaterialTheme.typography.displayLarge.copy(
                    color     = BaLightBg,
                    fontSize  = 36.sp,
                    fontStyle = FontStyle.Italic
                )
            )
            // Currency
            Text(
                text  = account.currency,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color    = BaLightBg.copy(alpha = 0.7f),
                    fontSize = 16.sp
                )
            )
            Spacer(Modifier.height(16.dp))

            // Full IBAN — tap to copy
            Row(
                modifier          = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        clipboardManager.setText(AnnotatedString(account.iban))
                        Toast.makeText(context, "IBAN copiat în clipboard", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = account.iban,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color      = BaLightBg.copy(alpha = 0.6f),
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 13.sp
                    )
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector        = Icons.Default.ContentCopy,
                    contentDescription = "Copiază IBAN",
                    modifier           = Modifier.size(13.dp),
                    tint               = BaLightBg.copy(alpha = 0.4f)
                )
            }
            Spacer(Modifier.height(12.dp))

            // Status pill
            AccountStatusBadgeDark(status = account.status)
        }
    }
}

// ─── QuickActionsRow ──────────────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(
    isDark: Boolean,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onTransfer: () -> Unit
) {
    val accentColor = if (isDark) BaGoldDark else BaGold
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            Triple("DEPOZIT",   Icons.Default.ArrowDownward, onDeposit),
            Triple("RETRAGERE", Icons.Default.ArrowUpward,   onWithdraw),
            Triple("TRANSFER",  Icons.Default.SwapHoriz,     onTransfer)
        ).forEach { (label, icon, action) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(onClick = action)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = label,
                        modifier           = Modifier.size(22.dp),
                        tint               = accentColor
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize      = 10.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

// ─── InfoCard ─────────────────────────────────────────────────────────────────

@Composable
private fun InfoCard(account: AccountResponse, isDark: Boolean) {
    val t  = account.type
    val isCurrent  = t == "0" || t.equals("Current",  ignoreCase = true)
    val isSavings  = t == "1" || t.equals("Savings",  ignoreCase = true)
    val isBusiness = t == "2" || t.equals("Business", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when {
            isCurrent && account.overdraftLimit != null -> {
                InfoRow(
                    label = "Limită overdraft",
                    value = "${formatHeroBalance(account.overdraftLimit)} ${account.currency}"
                )
            }
            isSavings && account.interestRate != null -> {
                InfoRow(label = "Rata dobânzii",  value = "${account.interestRate}%/an")
                InfoRow(label = "Dobândă",         value = "Aplicată lunar automat")
            }
            isBusiness && !account.companyName.isNullOrBlank() -> {
                InfoRow(label = "Companie", value = account.companyName!!)
            }
        }
        if (account.createdAt.isNotBlank()) {
            InfoRow(label = "Deschis la", value = formatDetailDate(account.createdAt))
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text     = value,
            style    = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color    = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f)
        )
    }
}

// ─── TransactionSectionHeader ─────────────────────────────────────────────────

@Composable
private fun TransactionSectionHeader(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp)
    ) {
        Text(
            text     = "Istoricul tranzacțiilor",
            style    = MaterialTheme.typography.titleMedium,
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier              = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "ALL"        to "TOATE",
                "Deposit"    to "DEPOZIT",
                "Withdrawal" to "RETRAGERE",
                "Transfer"   to "TRANSFER"
            ).forEach { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick  = { onFilterSelected(key) },
                    label    = {
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BaGold.copy(alpha = 0.15f),
                        selectedLabelColor     = BaGold
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ─── TransactionDetailItem ────────────────────────────────────────────────────

@Composable
private fun TransactionDetailItem(transaction: TransactionResponse, isDark: Boolean) {
    val (icon, amountColor, prefix) = when {
        transaction.type.equals("Deposit",    ignoreCase = true) || transaction.type == "0" ->
            Triple(Icons.Default.ArrowDownward, if (isDark) BaSuccessDark else BaSuccess, "+ ")
        transaction.type.equals("Withdrawal", ignoreCase = true) || transaction.type == "1" ->
            Triple(Icons.Default.ArrowUpward,   if (isDark) BaDangerDark  else BaDanger,  "– ")
        else ->
            Triple(Icons.Default.SwapHoriz, MaterialTheme.colorScheme.onBackground, "")
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier           = Modifier.size(18.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = transaction.description?.takeIf { it.isNotBlank() }
                    ?: transactionTypeLabel(transaction.type),
                style    = MaterialTheme.typography.titleMedium,
                color    = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text  = formatRelativeDateDetail(transaction.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text  = "$prefix${formatTransactionAmount(transaction.amount, transaction.currency)}",
            style = MaterialTheme.typography.titleMedium,
            color = amountColor
        )
    }
}

// ─── EditAccountBottomSheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountBottomSheet(
    uiState: AccountDetailUiState,
    account: AccountResponse,
    onDismiss: () -> Unit,
    onCurrencyChanged: (String) -> Unit,
    onOverdraftChanged: (String) -> Unit,
    onInterestRateChanged: (String) -> Unit,
    onCompanyNameChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark      = isSystemInDarkTheme()
    val t           = account.type
    val isCurrent   = t == "0" || t.equals("Current",  ignoreCase = true)
    val isSavings   = t == "1" || t.equals("Savings",  ignoreCase = true)
    val isBusiness  = t == "2" || t.equals("Business", ignoreCase = true)

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
                text     = "Editează Contul",
                style    = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // ── Monedă ───────────────────────────────────────────
            Text(
                text     = "MONEDĂ",
                style    = MaterialTheme.typography.labelSmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("MDL", "EUR", "USD").forEach { currency ->
                    val isSelected = uiState.editCurrency == currency
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
                            .clickable { onCurrencyChanged(currency) }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text  = currency,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) BaGold else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
            uiState.editCurrencyError?.let {
                Text(
                    text     = it,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = if (isDark) BaDangerDark else BaDanger,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Câmpuri specifice tipului ─────────────────────────
            when {
                isCurrent -> {
                    BaInput(
                        value         = uiState.editOverdraftLimit,
                        onValueChange = onOverdraftChanged,
                        label         = "Limită overdraft (opțional)",
                        keyboardType  = KeyboardType.Decimal,
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }
                isSavings -> {
                    BaInput(
                        value         = uiState.editInterestRate,
                        onValueChange = onInterestRateChanged,
                        label         = "Rata dobânzii (%/an, opțional)",
                        keyboardType  = KeyboardType.Decimal,
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }
                isBusiness -> {
                    BaInput(
                        value         = uiState.editCompanyName,
                        onValueChange = onCompanyNameChanged,
                        label         = "Nume companie",
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }

            BaPrimaryButton(
                text      = "Salvează",
                onClick   = onSubmit,
                isLoading = uiState.isUpdating,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── AccountStatusBadgeDark (pentru fundal obsidian) ─────────────────────────

@Composable
private fun AccountStatusBadgeDark(status: String) {
    val (label, textColor, bgColor) = when (status) {
        "Active",    "0" -> Triple("ACTIV",     BaSuccessDark, BaSuccessDark.copy(alpha = 0.2f))
        "Inactive",  "1" -> Triple("INACTIV",   Color.Gray,    Color.Gray.copy(alpha = 0.2f))
        "Suspended", "2" -> Triple("SUSPENDAT", BaGoldDark,    BaGoldDark.copy(alpha = 0.2f))
        "Closed",    "3" -> Triple("ÎNCHIS",    BaDangerDark,  BaDangerDark.copy(alpha = 0.2f))
        else             -> Triple(status.uppercase(), Color.Gray, Color.Gray.copy(alpha = 0.2f))
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color         = textColor,
                fontSize      = 9.sp,
                letterSpacing = 0.8.sp
            )
        )
    }
}

// ─── Helpers (pure functions) ─────────────────────────────────────────────────

private fun accountTypeTitle(type: String): String = when (type) {
    "0", "Current"  -> "Cont Curent"
    "1", "Savings"  -> "Cont Economii"
    "2", "Business" -> "Cont Business"
    else            -> "Cont"
}

private fun accountTypeLabel(type: String): String = when (type) {
    "0", "Current"  -> "CURENT"
    "1", "Savings"  -> "ECONOMII"
    "2", "Business" -> "BUSINESS"
    else            -> type.uppercase()
}

private fun transactionTypeLabel(type: String): String = when {
    type.equals("Deposit",    ignoreCase = true) || type == "0" -> "Depozit"
    type.equals("Withdrawal", ignoreCase = true) || type == "1" -> "Retragere"
    type.equals("Transfer",   ignoreCase = true) || type == "2" -> "Transfer"
    else -> type.replaceFirstChar { it.uppercase() }
}

private fun formatHeroBalance(amount: Double): String {
    val wholePart = amount.toLong()
    val fracPart  = Math.abs(((amount - wholePart) * 100).toLong())
    val formatted = buildString {
        val s = wholePart.toString().reversed()
        s.forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append(".")
            append(c)
        }
    }.reversed()
    return "$formatted,${fracPart.toString().padStart(2, '0')}"
}

private fun formatTransactionAmount(amount: Double, currency: String): String {
    val wholePart = amount.toLong()
    val fracPart  = Math.abs(((amount - wholePart) * 100).toLong())
    val formatted = buildString {
        val s = wholePart.toString().reversed()
        s.forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append(".")
            append(c)
        }
    }.reversed()
    return "$formatted,${fracPart.toString().padStart(2, '0')} $currency"
}

private fun formatDetailDate(isoDate: String): String = try {
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
} catch (_: Exception) {
    isoDate.take(10)
}

private fun formatRelativeDateDetail(isoDate: String): String = try {
    val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val date = sdf.parse(isoDate) ?: return isoDate.take(10)
    val cal  = Calendar.getInstance()
    val now  = cal.clone() as Calendar
    cal.time = date
    when {
        now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
            now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) -> "Astăzi"
        now.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR) == 1 &&
            now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) -> "Ieri"
        else -> SimpleDateFormat("d MMM yyyy", Locale("ro", "RO")).format(date)
    }
} catch (_: Exception) {
    isoDate.take(10)
}
