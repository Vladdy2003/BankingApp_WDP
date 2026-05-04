package com.example.bankingapp.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.card.CardResponse
import com.example.bankingapp.data.model.report.MonthlyBreakdown
import com.example.bankingapp.data.model.transaction.TransactionResponse
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaDarkBg
import com.example.bankingapp.ui.theme.BaDarkGoldDeep
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaDarkSurface
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaGoldDark
import com.example.bankingapp.ui.theme.BaGoldLight
import com.example.bankingapp.ui.theme.BaLightBg
import com.example.bankingapp.ui.theme.BaLightInk
import com.example.bankingapp.ui.theme.BaLightInk3
import com.example.bankingapp.ui.theme.BaObsidian
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ── Utility ──────────────────────────────────────────────────────────────────

private fun Double.formatCurrency(currency: String = "MDL"): String {
    val wholePart = this.toLong()
    val fracPart  = Math.abs(((this - wholePart) * 100).toLong())
    val formatted = buildString {
        val wholeStr = wholePart.toString().reversed()
        wholeStr.forEachIndexed { i, c ->
            if (i > 0 && i % 3 == 0) append(".")
            append(c)
        }
    }.reversed()
    return "$formatted,${fracPart.toString().padStart(2, '0')} $currency"
}

private fun String.maskIban(): String =
    if (length >= 8) "${take(4)} *** ${takeLast(4)}" else this

private fun formatRelativeDate(isoDate: String): String {
    return try {
        val sdf  = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(isoDate) ?: return isoDate
        val cal  = Calendar.getInstance()
        val now  = cal.clone() as Calendar
        cal.time = date
        val monthName = SimpleDateFormat("d MMM", Locale("ro", "RO")).format(date)
        when {
            now.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
                    now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) -> "Astăzi"
            now.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR) == 1 &&
                    now.get(Calendar.YEAR) == cal.get(Calendar.YEAR) -> "Ieri"
            else -> monthName
        }
    } catch (_: Exception) { isoDate }
}

private val monthNames = listOf("Ian", "Feb", "Mar", "Apr", "Mai", "Iun",
    "Iul", "Aug", "Sep", "Oct", "Nov", "Dec")

// ── Main Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAccountDetail: (String) -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()

    // Refresh de fiecare dată când ecranul intră în compoziție (switch tab sau revenire din detalii)
    LaunchedEffect(Unit) {
        viewModel.refreshOnResume()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Staggered reveal animation
    var revealStep by remember { mutableStateOf(0) }
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            delay(50);  revealStep = 1
            delay(60);  revealStep = 2
            delay(60);  revealStep = 3
            delay(60);  revealStep = 4
        }
    }
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing && revealStep > 0) {
            revealStep = 0
            delay(50);  revealStep = 1
            delay(60);  revealStep = 2
            delay(60);  revealStep = 3
            delay(60);  revealStep = 4
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Bună ziua, ${uiState.firstName}",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (uiState.unreadNotificationCount > 0) {
                                Badge(
                                    containerColor = if (isDark) BaDangerDark else BaDanger
                                ) {
                                    Text(
                                        text  = uiState.unreadNotificationCount.toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(onClick = onNavigateToNotifications) {
                            Icon(
                                imageVector        = Icons.Default.NotificationsNone,
                                contentDescription = "Notificări",
                                tint               = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = if (isDark) BaGoldDark else BaGold)
            }
            return@Scaffold
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh    = viewModel::refresh,
            modifier     = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier            = Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // ── Block 1: Conturi ─────────────────────────────────────────
                item {
                    AnimatedSection(visible = revealStep >= 1) {
                        AccountsSection(
                            accounts                = uiState.accounts,
                            isDark                  = isDark,
                            onNavigateToAccountDetail = onNavigateToAccountDetail
                        )
                    }
                }

                // ── Block 2: Carduri ─────────────────────────────────────────
                item {
                    AnimatedSection(visible = revealStep >= 2) {
                        CardsSection(
                            cards  = uiState.cards,
                            isDark = isDark
                        )
                    }
                }

                // ── Block 3: Grafic cheltuieli ───────────────────────────────
                item {
                    AnimatedSection(visible = revealStep >= 3) {
                        SpendingChartSection(
                            monthlyBreakdowns = uiState.monthlyBreakdowns,
                            isDark            = isDark
                        )
                    }
                }

                // ── Block 4: Tranzacții recente ──────────────────────────────
                item {
                    AnimatedSection(visible = revealStep >= 4) {
                        RecentTransactionsSection(
                            transactions     = uiState.recentTransactions,
                            onViewAll        = {},
                            isDark           = isDark
                        )
                    }
                }
            }
        }
    }
}

// ── Animation wrapper ─────────────────────────────────────────────────────────

@Composable
private fun AnimatedSection(visible: Boolean, content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(240)) + slideInVertically(tween(240)) { it / 5 }
    ) {
        content()
    }
}

// ── Block 1: Carousel Conturi ─────────────────────────────────────────────────

@Composable
private fun AccountsSection(
    accounts: List<AccountResponse>,
    isDark: Boolean,
    onNavigateToAccountDetail: (String) -> Unit = {}
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        SectionEyebrow(
            text     = "CONTURILE MELE",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (accounts.isEmpty()) {
            EmptyCarousel(
                message  = "Niciun cont deschis",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            val pagerState = rememberPagerState { accounts.size }
            HorizontalPager(
                state            = pagerState,
                contentPadding   = PaddingValues(horizontal = 16.dp),
                pageSpacing      = 12.dp,
                modifier         = Modifier.fillMaxWidth()
            ) { page ->
                AccountCard(
                    account = accounts[page],
                    isDark  = isDark,
                    onClick = { onNavigateToAccountDetail(accounts[page].id) }
                )
            }
            if (accounts.size > 1) {
                PagerDots(
                    count    = accounts.size,
                    current  = pagerState.currentPage,
                    isDark   = isDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AccountCard(
    account: AccountResponse,
    isDark: Boolean,
    onClick: () -> Unit = {}
) {
    val gradientColors = listOf(BaObsidian, if (isDark) BaDarkSurface else Color(0xFF2A2620))
    val accentColor = if (isDark) BaGoldDark else BaGold

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start  = Offset.Zero,
                        end    = Offset(size.width, size.height)
                    )
                )
            }
            .border(
                width = 0.5.dp,
                color = accentColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        // Top-left: account type eyebrow
        Text(
            text     = account.type.uppercase(),
            style    = MaterialTheme.typography.labelSmall.copy(
                color        = accentColor,
                fontSize     = 9.sp,
                letterSpacing = 1.2.sp
            ),
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Center: balance
        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text      = account.balance.formatCurrency(""),
                style     = MaterialTheme.typography.displayLarge.copy(
                    color      = BaLightBg,
                    fontSize   = 30.sp,
                    fontStyle  = FontStyle.Italic
                )
            )
            Text(
                text  = account.currency,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = BaLightBg.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            )
        }

        // Bottom-left: masked IBAN
        Text(
            text     = account.iban.maskIban(),
            style    = MaterialTheme.typography.bodySmall.copy(
                color        = BaLightBg.copy(alpha = 0.6f),
                fontFamily   = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize     = 11.sp
            ),
            modifier = Modifier.align(Alignment.BottomStart)
        )

        // Bottom-right: status badge
        AccountStatusBadge(
            status   = account.status,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun AccountStatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "active"    -> Triple(BaSuccess.copy(alpha = 0.2f), BaSuccessDark, "ACTIV")
        "suspended" -> Triple(BaGold.copy(alpha = 0.2f), BaGoldDark, "SUSPENDAT")
        "closed"    -> Triple(BaDanger.copy(alpha = 0.2f), BaDangerDark, "ÎNCHIS")
        else        -> Triple(BaLightInk3.copy(alpha = 0.2f), BaLightInk3, status.uppercase())
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color    = textColor,
                fontSize = 8.sp,
                letterSpacing = 0.8.sp
            )
        )
    }
}

// ── Block 2: Carousel Carduri ─────────────────────────────────────────────────

@Composable
private fun CardsSection(cards: List<CardResponse>, isDark: Boolean) {
    Column {
        SectionEyebrow(
            text     = "CARDURILE MELE",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (cards.isEmpty()) {
            EmptyCarousel(
                message  = "Niciun card emis",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            val pagerState = rememberPagerState { cards.size }
            HorizontalPager(
                state          = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing    = 12.dp,
                modifier       = Modifier.fillMaxWidth()
            ) { page ->
                BankCardMini(card = cards[page], isDark = isDark)
            }
            if (cards.size > 1) {
                PagerDots(
                    count    = cards.size,
                    current  = pagerState.currentPage,
                    isDark   = isDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BankCardMini(card: CardResponse, isDark: Boolean) {
    val accentColor = if (isDark) BaGoldDark else BaGold
    val isBlocked = card.status.equals("Blocked", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(BaObsidian, Color(0xFF1C1A16))))
            .border(
                width = 0.5.dp,
                color = accentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        if (isBlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BaDanger.copy(alpha = 0.3f))
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = "BLOCAT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = card.type.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color        = accentColor,
                    fontSize     = 8.sp,
                    letterSpacing = 1.2.sp
                )
            )
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment   = Alignment.Bottom
            ) {
                Text(
                    text  = card.maskedCardNumber,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color      = BaLightBg,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize   = 11.sp
                    )
                )
                Text(
                    text  = card.expiryDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color      = BaLightBg.copy(alpha = 0.6f),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize   = 11.sp
                    )
                )
            }
        }
    }
}

// ── Block 3: Grafic Cheltuieli ────────────────────────────────────────────────

@Composable
private fun SpendingChartSection(
    monthlyBreakdowns: List<MonthlyBreakdown>,
    isDark: Boolean
) {
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    val last6Months  = (5 downTo 0).map { i ->
        val m = ((currentMonth - 1 - i + 12) % 12) + 1
        m
    }
    val chartData = last6Months.map { month ->
        val breakdown = monthlyBreakdowns.firstOrNull { it.month == month }
        ChartBar(
            label   = monthNames[month - 1],
            value   = breakdown?.totalDebits ?: 0.0,
            isLast  = month == currentMonth
        )
    }
    val totalSpending = chartData.sumOf { it.value }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionEyebrow(
            text     = "CHELTUIELI — ULTIMELE 6 LUNI",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                SpendingBarChart(
                    data   = chartData,
                    isDark = isDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text  = "Total: ${totalSpending.formatCurrency("MDL")}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private data class ChartBar(val label: String, val value: Double, val isLast: Boolean)

@Composable
private fun SpendingBarChart(
    data: List<ChartBar>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val barColor        = if (isDark) BaGoldDark else BaGold
    val currentBarColor = if (isDark) BaDarkGoldDeep else Color(0xFFC9A96A)
    val textColor       = (if (isDark) BaDarkInk3 else BaLightInk3).toArgb()
    val maxValue        = data.maxOfOrNull { it.value }.takeIf { it != null && it > 0.0 } ?: 1.0

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val n         = data.size
        val spacing   = 8.dp.toPx()
        val barWidth  = (size.width - spacing * (n - 1)) / n
        val chartH    = size.height - 20.dp.toPx()
        val textY     = size.height - 4.dp.toPx()

        data.forEachIndexed { i, bar ->
            val x         = i * (barWidth + spacing)
            val barH      = (bar.value / maxValue * chartH).toFloat().coerceAtLeast(2f)
            val y         = chartH - barH
            val color     = if (bar.isLast) currentBarColor else barColor

            drawRoundRect(
                color       = color,
                topLeft     = Offset(x, y),
                size        = Size(barWidth, barH),
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(
                bar.label,
                x + barWidth / 2,
                textY,
                android.graphics.Paint().apply {
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize  = 11.sp.toPx()
                    this.color = textColor
                }
            )
        }
    }
}

// ── Block 4: Tranzacții Recente ───────────────────────────────────────────────

@Composable
private fun RecentTransactionsSection(
    transactions: List<TransactionResponse>,
    onViewAll: () -> Unit,
    isDark: Boolean
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier            = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Text(
                text  = "Tranzacții recente",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onViewAll) {
                Text(
                    text  = "Vezi toate",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) BaGoldDark else BaGold
                )
            }
        }

        if (transactions.isEmpty()) {
            Box(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment    = Alignment.Center
            ) {
                Text(
                    text      = "Nicio tranzacție încă",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            transactions.forEachIndexed { index, tx ->
                TransactionRowItem(transaction = tx, isDark = isDark)
                if (index < transactions.lastIndex) {
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outline,
                        thickness = 0.5.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRowItem(transaction: TransactionResponse, isDark: Boolean) {
    val (icon, amountColor, prefix) = when (transaction.type.lowercase()) {
        "deposit"    -> Triple(Icons.Default.ArrowDownward, if (isDark) BaSuccessDark else BaSuccess, "+ ")
        "withdrawal" -> Triple(Icons.Default.ArrowUpward, if (isDark) BaDangerDark else BaDanger, "– ")
        "transfer"   -> Triple(Icons.Default.SwapHoriz, MaterialTheme.colorScheme.onBackground, "")
        else         -> Triple(Icons.Default.AccountBalance, if (isDark) BaDangerDark else BaDanger, "– ")
    }

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
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

        // Name + date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = transaction.description
                    ?.takeIf { it.isNotBlank() }
                    ?: transaction.type.replaceFirstChar { it.uppercase() },
                style    = MaterialTheme.typography.titleMedium,
                color    = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text  = formatRelativeDate(transaction.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Amount
        Text(
            text  = "$prefix${transaction.amount.formatCurrency(transaction.currency)}",
            style = MaterialTheme.typography.titleMedium,
            color = amountColor
        )
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun SectionEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        style    = MaterialTheme.typography.labelSmall,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
private fun EmptyCarousel(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PagerDots(
    count: Int,
    current: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor   = if (isDark) BaGoldDark else BaGold
    val inactiveColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier            = modifier.wrapContentWidth(Alignment.CenterHorizontally),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) { i ->
            val color = if (i == current) activeColor else inactiveColor
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (i == current) 6.dp else 4.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
