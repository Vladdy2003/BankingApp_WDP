package com.example.bankingapp.ui.screens.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaPrimaryButton
import com.example.bankingapp.ui.components.BaSecondaryButton
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaDarkSurface
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaGoldDark
import com.example.bankingapp.ui.theme.BaGoldLight
import com.example.bankingapp.ui.theme.BaLightBg
import com.example.bankingapp.ui.theme.BaLightInk3
import com.example.bankingapp.ui.theme.BaObsidian
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    cardId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAccountDetail: (String) -> Unit = {},
    viewModel: CardDetailViewModel = viewModel()
) {
    LaunchedEffect(cardId) { viewModel.init(cardId) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.cardDeleted) {
        if (uiState.cardDeleted) onNavigateBack()
    }
    LaunchedEffect(uiState.actionError) {
        uiState.actionError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionError()
        }
    }

    // card hero fade-in trigger
    var cardVisible by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.card) {
        if (uiState.card != null) {
            delay(80)
            cardVisible = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Detalii Card",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi"
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

                uiState.card == null -> {
                    Text(
                        text     = uiState.error ?: "Card indisponibil.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp),
                        style    = MaterialTheme.typography.bodyLarge,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> {
                    val card = uiState.card!!
                    val isDark = isSystemInDarkTheme()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // ── Card Hero ───────────────────────────────────────────
                        AnimatedVisibility(
                            visible = cardVisible,
                            enter   = fadeIn(animationSpec = tween(280)) +
                                      scaleIn(animationSpec = tween(280), initialScale = 0.95f)
                        ) {
                            BankCardHero(card = card)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ── Informații Card ────────────────────────────────────
                        InfoSection(
                            card                     = card,
                            associatedIban           = uiState.associatedAccount?.iban,
                            isDark                   = isDark,
                            onNavigateToAccountDetail = {
                                onNavigateToAccountDetail(card.accountId)
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Limite ─────────────────────────────────────────────
                        LimitsSection(
                            dailyLimit   = card.dailyLimit,
                            monthlyLimit = card.monthlyLimit,
                            onModify     = { viewModel.showLimitsSheet() }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Acțiuni ────────────────────────────────────────────
                        ActionsSection(
                            status     = card.status,
                            isBlocking = uiState.isBlocking,
                            isDeleting = uiState.isDeleting,
                            isDark     = isDark,
                            onBlock    = { viewModel.showBlockDialog() },
                            onUnblock  = { viewModel.unblockCard() },
                            onClose    = { viewModel.showCloseDialog() }
                        )
                    }
                }
            }
        }
    }

    // ── EditLimitsBottomSheet ────────────────────────────────────────────────────
    if (uiState.showLimitsSheet) {
        EditLimitsBottomSheet(
            dailyLimit        = uiState.editDailyLimit,
            monthlyLimit      = uiState.editMonthlyLimit,
            isLoading         = uiState.isUpdatingLimits,
            onDailyChanged    = { viewModel.setEditDailyLimit(it) },
            onMonthlyChanged  = { viewModel.setEditMonthlyLimit(it) },
            onDismiss         = { viewModel.hideLimitsSheet() },
            onSave            = { viewModel.updateLimits() }
        )
    }

    // ── AlertDialog Blocare ───────────────────────────────────────────────────────
    if (uiState.showBlockDialog) {
        val dangerColor = if (isSystemInDarkTheme()) BaDangerDark else BaDanger
        AlertDialog(
            onDismissRequest = { viewModel.hideBlockDialog() },
            title            = { Text("Blocare card") },
            text             = {
                Text(
                    "Sigur doriți să blocați acest card? Tranzacțiile vor fi refuzate.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.blockCard() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = dangerColor)
                ) { Text("Blochează") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideBlockDialog() }) {
                    Text("Anulează")
                }
            }
        )
    }

    // ── AlertDialog Închidere ─────────────────────────────────────────────────────
    if (uiState.showCloseDialog) {
        val dangerColor = if (isSystemInDarkTheme()) BaDangerDark else BaDanger
        AlertDialog(
            onDismissRequest = { viewModel.hideCloseDialog() },
            title            = { Text("Închideți cardul?") },
            text             = {
                Text(
                    "Sigur doriți să închideți cardul? Acesta va fi dezactivat permanent.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.closeCard() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = dangerColor)
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(16.dp),
                            color       = dangerColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Închide")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCloseDialog() }) {
                    Text("Anulează")
                }
            }
        )
    }
}

// ─── BankCardHero (340×192dp, centered) ──────────────────────────────────────

@Composable
private fun BankCardHero(card: CardResponse) {
    val isDark     = isSystemInDarkTheme()
    val goldAccent = if (isDark) BaGoldDark else BaGold
    val isBlocked  = card.status == "Blocked" || card.status == "2"
    val isExpired  = card.status == "Expired"  || card.status == "3"
    val last4      = card.maskedCardNumber.filter { it.isDigit() }.takeLast(4).ifEmpty { "••••" }

    Box(
        modifier = Modifier
            .width(340.dp)
            .height(192.dp)
            .clip(RoundedCornerShape(24.dp))
            .drawBehind {
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(BaObsidian, BaDarkSurface),
                        start  = Offset.Zero,
                        end    = Offset(size.width, size.height)
                    )
                )
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
            Text(
                text  = cardTypeLabel(card.type),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = goldAccent
            )
            Text(
                text      = "•••• •••• •••• $last4",
                style     = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 16.sp
                ),
                color     = BaLightBg,
                modifier  = Modifier.align(Alignment.CenterHorizontally)
            )
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
    }
}

// ─── InfoSection ──────────────────────────────────────────────────────────────

@Composable
private fun InfoSection(
    card: CardResponse,
    associatedIban: String?,
    isDark: Boolean,
    onNavigateToAccountDetail: () -> Unit
) {
    val last4  = card.maskedCardNumber.filter { it.isDigit() }.takeLast(4).ifEmpty { "••••" }
    val ink3   = if (isDark) BaDarkInk3 else BaLightInk3

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoRow(
                label = "Număr card",
                value = "•••• •••• •••• $last4",
                valueFamily = FontFamily.Monospace
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            InfoRow(
                label = "Expiră",
                value = card.expiryDate,
                valueFamily = FontFamily.Monospace
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            // Cont asociat — navigabil
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAccountDetail),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Cont asociat",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment  = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text       = if (associatedIban != null) maskIban(associatedIban) else maskAccountId(card.accountId),
                        style      = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Icon(
                        imageVector        = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier           = Modifier.size(16.dp),
                        tint               = ink3
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            InfoRow(label = "Emis la", value = formatCardDate(card.createdAt))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = "Status",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CardStatusPill(status = card.status, isDark = isDark)
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueFamily: FontFamily = FontFamily.Default
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = valueFamily),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun CardStatusPill(status: String, isDark: Boolean) {
    val (label, textColor, bgColor) = when (status) {
        "Active",  "0" -> Triple("ACTIV",   if (isDark) BaSuccessDark else BaSuccess, (if (isDark) BaSuccessDark else BaSuccess).copy(alpha = 0.15f))
        "Blocked", "2" -> Triple("BLOCAT",  if (isDark) BaDangerDark  else BaDanger,  (if (isDark) BaDangerDark  else BaDanger).copy(alpha = 0.15f))
        "Expired", "3" -> Triple("EXPIRAT", Color.Gray, Color.Gray.copy(alpha = 0.15f))
        "Pending", "1" -> Triple("PENDING", BaGold, BaGold.copy(alpha = 0.15f))
        else           -> Triple(status.uppercase(), Color.Gray, Color.Gray.copy(alpha = 0.15f))
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

// ─── LimitsSection ────────────────────────────────────────────────────────────

@Composable
private fun LimitsSection(
    dailyLimit: Double,
    monthlyLimit: Double,
    onModify: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text  = "Limite de utilizare",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            LimitRow(label = "Zilnic", limitMdl = dailyLimit)

            Spacer(modifier = Modifier.height(12.dp))

            LimitRow(label = "Lunar", limitMdl = monthlyLimit)

            Spacer(modifier = Modifier.height(16.dp))

            BaSecondaryButton(
                text     = "Modifică Limitele",
                onClick  = onModify,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LimitRow(label: String, limitMdl: Double) {
    Column {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text  = "0 MDL / ${formatLimit(limitMdl)} MDL",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Progress bar (0% used — no spending data from this endpoint)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BaGoldLight)
        )
    }
}

// ─── ActionsSection ───────────────────────────────────────────────────────────

@Composable
private fun ActionsSection(
    status: String,
    isBlocking: Boolean,
    isDeleting: Boolean,
    isDark: Boolean,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onClose: () -> Unit
) {
    val dangerColor  = if (isDark) BaDangerDark else BaDanger
    val successColor = if (isDark) BaSuccessDark else BaSuccess

    Column(modifier = Modifier.fillMaxWidth()) {
        when (status) {
            "Active", "0" -> {
                OutlinedButton(
                    onClick  = onBlock,
                    enabled  = !isBlocking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = dangerColor
                    ),
                    border   = BorderStroke(1.dp, dangerColor)
                ) {
                    if (isBlocking) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = dangerColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text  = "Blochează Cardul",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            "Blocked", "2" -> {
                Button(
                    onClick  = onUnblock,
                    enabled  = !isBlocking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = successColor,
                        contentColor   = Color.White
                    )
                ) {
                    if (isBlocking) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text  = "Deblochează Cardul",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick  = onClose,
            enabled  = !isDeleting,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.textButtonColors(contentColor = dangerColor)
        ) {
            if (isDeleting) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(18.dp),
                    color       = dangerColor,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text  = "Închide Cardul",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// ─── EditLimitsBottomSheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLimitsBottomSheet(
    dailyLimit: String,
    monthlyLimit: String,
    isLoading: Boolean,
    onDailyChanged: (String) -> Unit,
    onMonthlyChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
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
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text     = "Modifică Limitele",
                style    = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            BaInput(
                value         = dailyLimit,
                onValueChange = onDailyChanged,
                label         = "Limită zilnică (MDL)",
                keyboardType  = KeyboardType.Decimal,
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            BaInput(
                value         = monthlyLimit,
                onValueChange = onMonthlyChanged,
                label         = "Limită lunară (MDL)",
                keyboardType  = KeyboardType.Decimal,
                modifier      = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            BaPrimaryButton(
                text      = "Salvează",
                onClick   = onSave,
                isLoading = isLoading,
                modifier  = Modifier.fillMaxWidth()
            )
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

private fun formatLimit(value: Double): String =
    if (value % 1.0 == 0.0) "%,.0f".format(value) else "%,.2f".format(value)

private fun formatCardDate(isoDate: String): String {
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
