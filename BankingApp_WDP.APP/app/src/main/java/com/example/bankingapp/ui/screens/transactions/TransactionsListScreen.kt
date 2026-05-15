package com.example.bankingapp.ui.screens.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.ui.components.BaGhostButton
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaPrimaryButton
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaGoldDark
import com.example.bankingapp.ui.theme.BaLightInk3
import com.example.bankingapp.ui.theme.BaObsidian
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsListScreen(
    viewModel: TransactionsListViewModel = viewModel(),
    sheetViewModel: TransactionSheetViewModel = viewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val snackbar     = remember { SnackbarHostState() }
    val listState    = rememberLazyListState()
    var fabExpanded  by remember { mutableStateOf(false) }

    // Refresh la fiecare revenire în tab (după prima încărcare)
    LaunchedEffect(Unit) { viewModel.refreshOnResume() }

    // Infinite scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.canScrollForward }
            .collect { can ->
                if (!can && uiState.groups.isNotEmpty() && uiState.hasMore && !uiState.isLoadingMore) {
                    viewModel.loadMore()
                }
            }
    }

    // Transaction sheet success → snackbar + close sheet + refresh
    val sheetState by sheetViewModel.uiState.collectAsState()
    LaunchedEffect(sheetState.successMessage) {
        sheetState.successMessage?.let { msg ->
            snackbar.showSnackbar(msg)
            sheetViewModel.clearSuccess()
            viewModel.refresh()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tranzacții", style = MaterialTheme.typography.displaySmall)
                },
                actions = {
                    IconButton(onClick = { viewModel.showFilterSheet(); fabExpanded = false }) {
                        Icon(Icons.Default.FilterList,
                            contentDescription = "Filtre",
                            tint = if (uiState.appliedFilter.isActive) (if (isSystemInDarkTheme()) BaGoldDark else BaGold)
                                   else MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            SpeedDialFab(
                expanded   = fabExpanded,
                onToggle   = { fabExpanded = !fabExpanded },
                onDeposit  = { fabExpanded = false; sheetViewModel.resetSheet(); viewModel.showDeposit() },
                onWithdraw = { fabExpanded = false; sheetViewModel.resetSheet(); viewModel.showWithdraw() },
                onTransfer = { fabExpanded = false; sheetViewModel.resetSheet(); viewModel.showTransfer() }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->

        // Tap outside FAB to collapse
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

                uiState.groups.isEmpty() -> {
                    Column(
                        modifier              = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment   = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Receipt, null,
                            modifier = Modifier.size(56.dp),
                            tint     = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text  = if (uiState.appliedFilter.isActive) "Nicio tranzacție cu filtrele aplicate." else "Nicio tranzacție.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.appliedFilter.isActive) {
                            Spacer(Modifier.height(16.dp))
                            BaGhostButton(text = "Resetează filtrele", onClick = { viewModel.resetFilters() })
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state               = listState,
                        contentPadding      = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        uiState.groups.forEach { group ->
                            stickyHeader(key = group.dateLabel) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text  = group.dateLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            items(items = group.transactions, key = { it.id }) { tx ->
                                TransactionRow(tx)
                                HorizontalDivider(
                                    modifier  = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color     = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        if (uiState.isLoadingMore) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = BaGold, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Sheets ────────────────────────────────────────────────────────────────
    if (uiState.showFilterSheet) {
        FilterBottomSheet(
            filter        = uiState.pendingFilter,
            onTypeChange  = { viewModel.setPendingType(it) },
            onStatusChange = { viewModel.setPendingStatus(it) },
            onFromChange  = { viewModel.setPendingFrom(it) },
            onToChange    = { viewModel.setPendingTo(it) },
            onApply       = { viewModel.applyFilters() },
            onReset       = { viewModel.resetFilters() },
            onDismiss     = { viewModel.hideFilterSheet() }
        )
    }
    if (uiState.showDepositSheet) {
        DepositBottomSheet(
            onDismiss    = { viewModel.hideDeposit() },
            onSuccess    = { msg -> /* handled via LaunchedEffect on sheetState.successMessage */ },
            sheetViewModel = sheetViewModel
        )
    }
    if (uiState.showWithdrawSheet) {
        WithdrawBottomSheet(
            onDismiss    = { viewModel.hideWithdraw() },
            onSuccess    = { _ -> },
            sheetViewModel = sheetViewModel
        )
    }
    if (uiState.showTransferSheet) {
        TransferBottomSheet(
            onDismiss    = { viewModel.hideTransfer() },
            onSuccess    = { _ -> },
            sheetViewModel = sheetViewModel
        )
    }
}

// ─── SpeedDialFab ─────────────────────────────────────────────────────────────

@Composable
private fun SpeedDialFab(
    expanded: Boolean,
    onToggle: () -> Unit,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onTransfer: () -> Unit
) {
    val isDark   = isSystemInDarkTheme()
    val rotation by animateFloatAsState(targetValue = if (expanded) 45f else 0f, label = "fab_rotate")

    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn(tween(180)) + slideInVertically(tween(180)) { it / 2 },
            exit    = fadeOut(tween(120)) + slideOutVertically(tween(120)) { it / 2 }
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniFabItem(
                    icon    = Icons.Default.ArrowDownward,
                    label   = "Depozit",
                    color   = if (isDark) BaSuccessDark else BaSuccess,
                    onClick = onDeposit
                )
                MiniFabItem(
                    icon    = Icons.Default.ArrowUpward,
                    label   = "Retragere",
                    color   = if (isDark) BaDangerDark else BaDanger,
                    onClick = onWithdraw
                )
                MiniFabItem(
                    icon    = Icons.Default.SwapHoriz,
                    label   = "Transfer",
                    color   = if (isDark) BaGoldDark else BaGold,
                    onClick = onTransfer
                )
            }
        }

        FloatingActionButton(
            onClick          = onToggle,
            containerColor   = BaObsidian,
            contentColor     = if (isDark) BaGoldDark else BaGold,
            shape            = CircleShape
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = "Acțiuni",
                modifier           = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
private fun MiniFabItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground)
        }
        SmallFloatingActionButton(
            onClick        = onClick,
            containerColor = color,
            contentColor   = Color.White,
            shape          = CircleShape
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
    }
}

// ─── TransactionRow ───────────────────────────────────────────────────────────

@Composable
private fun TransactionRow(tx: TransactionResponse) {
    val isDark      = isSystemInDarkTheme()
    val isCredit    = tx.type.equals("Deposit", ignoreCase = true) || tx.type == "0"
    val amountColor = when {
        isCredit                                                                      -> if (isDark) BaSuccessDark else BaSuccess
        tx.type.equals("Withdrawal", ignoreCase = true) || tx.type == "1"             -> if (isDark) BaDangerDark  else BaDanger
        else                                                                          -> MaterialTheme.colorScheme.onBackground
    }
    val icon = when {
        tx.type.equals("Deposit",    ignoreCase = true) || tx.type == "0" -> Icons.Default.ArrowDownward
        tx.type.equals("Withdrawal", ignoreCase = true) || tx.type == "1" -> Icons.Default.ArrowUpward
        else                                                               -> Icons.Default.SwapHoriz
    }
    val iconTint = when {
        tx.type.equals("Deposit",    ignoreCase = true) || tx.type == "0" -> if (isDark) BaSuccessDark else BaSuccess
        tx.type.equals("Withdrawal", ignoreCase = true) || tx.type == "1" -> if (isDark) BaDangerDark  else BaDanger
        else                                                               -> if (isDark) BaGoldDark    else BaGold
    }
    val sign   = if (isCredit) "+" else "-"
    val prefix = if (isCredit) "+" else "-"

    Row(
        modifier             = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = tx.description?.ifBlank { null } ?: typeLabel(tx.type),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text  = formatRelativeDate(tx.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) BaDarkInk3 else BaLightInk3
            )
        }
        Text(
            text  = "$prefix${formatAmt(tx.amount)} ${tx.currency}",
            style = MaterialTheme.typography.titleMedium,
            color = amountColor
        )
    }
}

// ─── FilterBottomSheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    filter: FilterState,
    onTypeChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark     = isSystemInDarkTheme()
    val gold       = if (isDark) BaGoldDark else BaGold
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
            Text("Filtre", style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 20.dp))

            // Tip
            SectionLabel("TIP TRANZACȚIE")
            Spacer(Modifier.height(8.dp))
            val types = listOf("" to "TOATE", "Deposit" to "DEPOZIT", "Withdrawal" to "RETRAGERE",
                "Transfer" to "TRANSFER", "Payment" to "PLATĂ")
            ChipRow(options = types, selected = filter.type, onSelect = onTypeChange, gold = gold)

            Spacer(Modifier.height(16.dp))

            // Status
            SectionLabel("STATUS")
            Spacer(Modifier.height(8.dp))
            val statuses = listOf("" to "TOATE", "Completed" to "COMPLETAT", "Pending" to "ÎN AȘTEPTARE",
                "Failed" to "EȘUAT", "Cancelled" to "ANULAT")
            ChipRow(options = statuses, selected = filter.status, onSelect = onStatusChange, gold = gold)

            Spacer(Modifier.height(16.dp))

            // Interval dată
            SectionLabel("INTERVAL DATĂ")
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BaInput(
                    value         = filter.from,
                    onValueChange = onFromChange,
                    label         = "De la (yyyy-MM-dd)",
                    keyboardType  = KeyboardType.Text,
                    modifier      = Modifier.weight(1f)
                )
                BaInput(
                    value         = filter.to,
                    onValueChange = onToChange,
                    label         = "Până la (yyyy-MM-dd)",
                    keyboardType  = KeyboardType.Text,
                    modifier      = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            BaPrimaryButton(text = "Aplică Filtrele", onClick = onApply, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            BaGhostButton(text = "Resetează", onClick = onReset, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ChipRow(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    gold: Color
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options.size) { i ->
            val (value, label) = options[i]
            FilterChip(
                selected = selected == value,
                onClick  = { onSelect(value) },
                label    = { Text(label, style = MaterialTheme.typography.labelMedium) },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = gold.copy(alpha = 0.15f),
                    selectedLabelColor     = gold
                )
            )
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun typeLabel(type: String) = when (type) {
    "Deposit",    "0" -> "Depozit"
    "Withdrawal", "1" -> "Retragere"
    "Transfer",   "2" -> "Transfer"
    "Payment",    "3" -> "Plată"
    else              -> type
}

private fun formatRelativeDate(isoDate: String): String {
    val parsers = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",     Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",        Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd",                   Locale.getDefault())
    )
    val date = parsers.firstNotNullOfOrNull { p -> runCatching { p.parse(isoDate) }.getOrNull() }
        ?: return isoDate.take(10)
    val now   = System.currentTimeMillis()
    val diff  = now - date.time
    val mins  = diff / 60_000
    val hours = diff / 3_600_000
    return when {
        mins  < 60  -> "acum ${mins}min"
        hours < 24  -> "acum ${hours}ore"
        else        -> SimpleDateFormat("d MMM", Locale("ro", "RO")).format(date)
    }
}
