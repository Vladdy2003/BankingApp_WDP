package com.example.bankingapp.ui.screens.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
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
import com.example.bankingapp.ui.theme.BaGoldDark
import com.example.bankingapp.ui.theme.BaLightInk3
import kotlin.math.max
import kotlin.math.min

// ─── DepositBottomSheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositBottomSheet(
    preselectedAccountId: String = "",
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
    sheetViewModel: TransactionSheetViewModel = viewModel()
) {
    val uiState by sheetViewModel.uiState.collectAsState()

    LaunchedEffect(preselectedAccountId) {
        if (preselectedAccountId.isNotEmpty()) sheetViewModel.preselect(preselectedAccountId)
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            sheetViewModel.clearSuccess()
            sheetViewModel.resetSheet()
            onSuccess(msg)
            onDismiss()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { sheetViewModel.resetSheet(); onDismiss() },
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Depozit", style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

            AccountSelector(
                label             = "CONT DESTINAȚIE",
                accounts          = uiState.accounts,
                selectedAccountId = uiState.selectedAccountId,
                onSelected        = { sheetViewModel.setSelectedAccount(it) }
            )

            Spacer(Modifier.height(20.dp))

            AmountInput(
                amount       = uiState.amount,
                amountError  = uiState.amountError,
                onAmountChange = { sheetViewModel.setAmount(it) }
            )

            val balance = uiState.accounts.firstOrNull { it.id == uiState.selectedAccountId }?.balance
            if (balance != null) {
                Text(
                    text  = "Sold curent: ${formatAmt(balance)} ${uiState.accounts.firstOrNull { it.id == uiState.selectedAccountId }?.currency ?: "MDL"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSystemInDarkTheme()) BaDarkInk3 else BaLightInk3
                )
            }

            Spacer(Modifier.height(12.dp))

            BaInput(
                value         = uiState.description,
                onValueChange = { sheetViewModel.setDescription(it) },
                label         = "Descriere (opțional)",
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            uiState.error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = if (isSystemInDarkTheme()) BaDangerDark else BaDanger,
                    modifier = Modifier.padding(bottom = 8.dp))
            }

            BaPrimaryButton(
                text      = "Confirmă Depozitul",
                onClick   = { sheetViewModel.deposit() },
                isLoading = uiState.isSubmitting,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── WithdrawBottomSheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawBottomSheet(
    preselectedAccountId: String = "",
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
    sheetViewModel: TransactionSheetViewModel = viewModel()
) {
    val uiState by sheetViewModel.uiState.collectAsState()

    LaunchedEffect(preselectedAccountId) {
        if (preselectedAccountId.isNotEmpty()) sheetViewModel.preselect(preselectedAccountId)
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            sheetViewModel.clearSuccess()
            sheetViewModel.resetSheet()
            onSuccess(msg)
            onDismiss()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { sheetViewModel.resetSheet(); onDismiss() },
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Retragere", style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

            AccountSelector(
                label             = "CONT SURSĂ",
                accounts          = uiState.accounts,
                selectedAccountId = uiState.selectedAccountId,
                onSelected        = { sheetViewModel.setSelectedAccount(it) }
            )

            Spacer(Modifier.height(20.dp))

            AmountInput(
                amount        = uiState.amount,
                amountError   = uiState.amountError,
                onAmountChange = { sheetViewModel.setAmount(it) }
            )

            val account = uiState.accounts.firstOrNull { it.id == uiState.selectedAccountId }
            if (account != null) {
                val insufficient = (uiState.amount.toDoubleOrNull() ?: 0.0) > account.balance
                Text(
                    text  = "Disponibil: ${formatAmt(account.balance)} ${account.currency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (insufficient) (if (isSystemInDarkTheme()) BaDangerDark else BaDanger)
                            else (if (isSystemInDarkTheme()) BaDarkInk3 else BaLightInk3)
                )
            }

            Spacer(Modifier.height(12.dp))

            BaInput(
                value         = uiState.description,
                onValueChange = { sheetViewModel.setDescription(it) },
                label         = "Descriere (opțional)",
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            uiState.error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = if (isSystemInDarkTheme()) BaDangerDark else BaDanger,
                    modifier = Modifier.padding(bottom = 8.dp))
            }

            BaPrimaryButton(
                text      = "Confirmă Retragerea",
                onClick   = { sheetViewModel.withdraw() },
                isLoading = uiState.isSubmitting,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── TransferBottomSheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferBottomSheet(
    preselectedAccountId: String = "",
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit,
    sheetViewModel: TransactionSheetViewModel = viewModel()
) {
    val uiState by sheetViewModel.uiState.collectAsState()
    val isDark  = isSystemInDarkTheme()
    val gold    = if (isDark) BaGoldDark else BaGold
    val ink3    = if (isDark) BaDarkInk3 else BaLightInk3

    LaunchedEffect(preselectedAccountId) {
        if (preselectedAccountId.isNotEmpty()) sheetViewModel.preselect(preselectedAccountId)
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            sheetViewModel.clearSuccess()
            sheetViewModel.resetSheet()
            onSuccess(msg)
            onDismiss()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { sheetViewModel.resetSheet(); onDismiss() },
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Transfer", style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp))

            // ── Cont sursă ─────────────────────────────────────────────────
            AccountSelector(
                label             = "DIN CONTUL",
                accounts          = uiState.accounts,
                selectedAccountId = uiState.selectedAccountId,
                onSelected        = { sheetViewModel.setSelectedAccount(it) }
            )

            Spacer(Modifier.height(16.dp))

            // ── Toggle mod transfer ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("OWN" to "Conturile mele", "IBAN" to "Alt IBAN").forEach { (mode, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (uiState.transferMode == mode) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { sheetViewModel.setTransferMode(mode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (uiState.transferMode == mode) gold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Destinatar ─────────────────────────────────────────────────
            if (uiState.transferMode == "OWN") {
                val others = uiState.accounts.filter { it.id != uiState.selectedAccountId }
                AccountSelector(
                    label             = "CĂTRE CONTUL",
                    accounts          = others,
                    selectedAccountId = uiState.toOwnAccountId,
                    onSelected        = { sheetViewModel.setToOwnAccount(it) }
                )
            } else {
                // IBAN input cu auto-espaciere la grupe de 4
                val rawIban = uiState.toIban
                val displayed = rawIban.chunked(4).joinToString(" ")
                Column {
                    Text("IBAN DESTINATAR", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                0.5.dp,
                                if (uiState.ibanError != null) (if (isDark) BaDangerDark else BaDanger)
                                else MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(16.dp)
                    ) {
                        BasicTextField(
                            value          = displayed,
                            onValueChange  = { raw ->
                                sheetViewModel.setToIban(raw.filter { it.isLetterOrDigit() })
                            },
                            textStyle      = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color      = MaterialTheme.colorScheme.onBackground
                            ),
                            cursorBrush    = SolidColor(gold),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier       = Modifier.fillMaxWidth(),
                            decorationBox  = { inner ->
                                if (rawIban.isEmpty()) {
                                    Text("MD00 0000 0000 0000 0000 0000",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                        color = ink3)
                                }
                                inner()
                            }
                        )
                    }
                    uiState.ibanError?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) BaDangerDark else BaDanger,
                            modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            AmountInput(
                amount        = uiState.amount,
                amountError   = uiState.amountError,
                onAmountChange = { sheetViewModel.setAmount(it) }
            )

            val account = uiState.accounts.firstOrNull { it.id == uiState.selectedAccountId }
            if (account != null) {
                Text(
                    text  = "Disponibil: ${formatAmt(account.balance)} ${account.currency}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ink3
                )
            }

            // Comision preview
            val amt = uiState.amount.toDoubleOrNull() ?: 0.0
            if (amt > 0) {
                val commission = min(max(amt * 0.005, 1.0), 50.0)
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Comision: ~${formatAmt(commission)} MDL",
                    style = MaterialTheme.typography.bodySmall,
                    color = ink3
                )
            }

            Spacer(Modifier.height(12.dp))

            BaInput(
                value         = uiState.description,
                onValueChange = { sheetViewModel.setDescription(it) },
                label         = "Descriere (opțional)",
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            uiState.error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) BaDangerDark else BaDanger,
                    modifier = Modifier.padding(bottom = 8.dp))
            }

            BaPrimaryButton(
                text      = "Confirmă Transferul",
                onClick   = { sheetViewModel.transfer() },
                isLoading = uiState.isSubmitting,
                modifier  = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─── AmountInput ──────────────────────────────────────────────────────────────

@Composable
internal fun AmountInput(
    amount: String,
    amountError: String?,
    onAmountChange: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val gold   = if (isDark) BaGoldDark else BaGold

    Column(
        modifier            = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BasicTextField(
                value          = amount,
                onValueChange  = { v -> onAmountChange(v.filter { it.isDigit() || it == '.' }) },
                textStyle      = MaterialTheme.typography.displayLarge.copy(
                    fontSize  = 36.sp,
                    color     = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                ),
                cursorBrush    = SolidColor(gold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine     = true,
                modifier       = Modifier.weight(1f, fill = false),
                decorationBox  = { inner ->
                    if (amount.isEmpty()) {
                        Text("0", style = MaterialTheme.typography.displayLarge.copy(fontSize = 36.sp),
                            color = if (isDark) BaDarkInk3 else BaLightInk3, textAlign = TextAlign.Center)
                    }
                    inner()
                }
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text  = "MDL",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = if (isDark) BaDarkInk3 else BaLightInk3
            )
        }

        amountError?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, style = MaterialTheme.typography.bodySmall,
                color = if (isDark) BaDangerDark else BaDanger,
                textAlign = TextAlign.Center)
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ─── AccountSelector ─────────────────────────────────────────────────────────

@Composable
internal fun AccountSelector(
    label: String,
    accounts: List<AccountResponse>,
    selectedAccountId: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.firstOrNull { it.id == selectedAccountId }
    val isDark   = isSystemInDarkTheme()
    val ink3     = if (isDark) BaDarkInk3 else BaLightInk3

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp))

        Box {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                    .clickable { if (accounts.isNotEmpty()) expanded = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    if (selected != null) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = accountTypeDisplay(selected.type),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) BaGoldDark else BaGold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text  = maskIban(selected.iban),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                                )
                                Text(
                                    text  = "${formatAmt(selected.balance)} ${selected.currency}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ink3
                                )
                            }
                        }
                    } else {
                        Text(
                            text  = if (accounts.isEmpty()) "Niciun cont disponibil" else "Selectați un cont",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ink3,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Icon(Icons.Default.ExpandMore, null, tint = ink3)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                accounts.forEach { acc ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(accountTypeDisplay(acc.type),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isDark) BaGoldDark else BaGold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(maskIban(acc.iban),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace))
                                    Text("${formatAmt(acc.balance)} ${acc.currency}",
                                        style = MaterialTheme.typography.bodySmall, color = ink3)
                                }
                            }
                        },
                        onClick = { onSelected(acc.id); expanded = false }
                    )
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun accountTypeDisplay(type: String) = when (type) {
    "Current",  "0" -> "CURENT"
    "Savings",  "1" -> "ECONOMII"
    "Business", "2" -> "BUSINESS"
    else            -> type.uppercase()
}

private fun maskIban(iban: String): String {
    if (iban.length <= 6) return iban
    val masked = iban.take(4) + "•".repeat(iban.length - 6) + iban.takeLast(2)
    return masked.chunked(4).joinToString(" ")
}
