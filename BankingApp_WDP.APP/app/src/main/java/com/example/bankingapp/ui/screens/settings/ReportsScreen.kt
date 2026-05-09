package com.example.bankingapp.ui.screens.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.data.model.account.AccountResponse
import com.example.bankingapp.data.model.report.AnnualSummaryResponse
import com.example.bankingapp.data.model.report.CategoryBreakdown
import com.example.bankingapp.data.model.report.IncomeVsExpensesResponse
import com.example.bankingapp.data.model.report.MonthlyBreakdown
import com.example.bankingapp.data.model.report.MonthlyReportResponse
import com.example.bankingapp.data.model.report.SpendingByCategoryResponse
import com.example.bankingapp.ui.components.BaInput
import com.example.bankingapp.ui.components.BaSecondaryButton
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaLightInk3
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark
import java.util.Calendar

// Report-specific chart colour palette
private val chartPalette = listOf(
    Color(0xFFB8965A), Color(0xFF3A7D5C), Color(0xFFB84040),
    Color(0xFF4A7FB5), Color(0xFF9B6DB5), Color(0xFFD4874E),
    Color(0xFF4AADAD), Color(0xFF7F8C8D)
)

private val monthNames = listOf(
    "Ianuarie", "Februarie", "Martie", "Aprilie", "Mai", "Iunie",
    "Iulie", "August", "Septembrie", "Octombrie", "Noiembrie", "Decembrie"
)
private val shortMonths = listOf("I","F","M","A","M","I","I","A","S","O","N","D")
private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
private val yearRange   = (2020..currentYear + 1).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); viewModel.clearError() }
    }
    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let { snackbarHostState.showSnackbar(it); viewModel.clearExportMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Rapoarte",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Înapoi")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoadingAccounts) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = BaGold) }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AccountSelector(
                    accounts   = uiState.accounts,
                    selectedId = uiState.selectedAccountId,
                    onSelect   = { viewModel.selectAccount(it) },
                    isDark     = isDark
                )

                Spacer(Modifier.height(16.dp))
                MonthlyStatementSection(uiState, viewModel, isDark)
                Spacer(Modifier.height(12.dp))
                AnnualSummarySection(uiState, viewModel, isDark)
                Spacer(Modifier.height(12.dp))
                SpendingByCategorySection(uiState, viewModel, isDark)
                Spacer(Modifier.height(12.dp))
                IncomeVsExpensesSection(uiState, viewModel, isDark)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Account Selector ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelector(
    accounts: List<AccountResponse>,
    selectedId: String,
    onSelect: (String) -> Unit,
    isDark: Boolean
) {
    val ink3     = if (isDark) BaDarkInk3 else BaLightInk3
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.firstOrNull { it.id == selectedId }

    Column {
        Text(
            text  = "CONT",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = ink3
        )
        Spacer(Modifier.height(6.dp))

        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text  = "Nu există conturi disponibile",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Column
        }

        ExposedDropdownMenuBox(
            expanded         = expanded,
            onExpandedChange = { expanded = it },
            modifier         = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = selected?.let { "${accountTypeLabel(it.type)} • ${maskIban(it.iban)}" }
                               ?: "Selectați un cont",
                    style    = MaterialTheme.typography.bodyLarge,
                    color    = if (selected != null) MaterialTheme.colorScheme.onBackground
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false }
            ) {
                accounts.forEach { acc ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text  = accountTypeLabel(acc.type),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text  = maskIban(acc.iban),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick               = { onSelect(acc.id); expanded = false },
                        contentPadding        = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

// ── Section 1: Extras Lunar ───────────────────────────────────────────────────

@Composable
private fun MonthlyStatementSection(
    uiState: ReportsUiState,
    viewModel: ReportsViewModel,
    isDark: Boolean
) {
    ReportCard(title = "Extras de Cont Lunar") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactDropdown(
                label    = "An",
                current  = uiState.monthlyYear.toString(),
                options  = yearRange.map { it.toString() },
                onSelect = { viewModel.setMonthlyYear(it.toInt()) },
                modifier = Modifier.weight(1f)
            )
            CompactDropdown(
                label    = "Lună",
                current  = monthNames[uiState.monthlyMonth - 1],
                options  = monthNames,
                onSelect = { viewModel.setMonthlyMonth(monthNames.indexOf(it) + 1) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        BaSecondaryButton(
            text     = if (uiState.isLoadingMonthly) "Se încarcă…" else "Previzualizare",
            onClick  = { viewModel.loadMonthlyStatement() },
            enabled  = !uiState.isLoadingMonthly,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.monthlyReport?.let { report ->
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))
            MonthlyPreview(report = report, isDark = isDark)
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BaSecondaryButton(
                text     = if (uiState.isExporting) "…" else "Export PDF",
                onClick  = { viewModel.exportReport("pdf") },
                enabled  = !uiState.isExporting,
                modifier = Modifier.weight(1f)
            )
            BaSecondaryButton(
                text     = if (uiState.isExporting) "…" else "Export CSV",
                onClick  = { viewModel.exportReport("csv") },
                enabled  = !uiState.isExporting,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthlyPreview(report: MonthlyReportResponse, isDark: Boolean) {
    val success = if (isDark) BaSuccessDark else BaSuccess
    val danger  = if (isDark) BaDangerDark  else BaDanger
    val neutral = MaterialTheme.colorScheme.onBackground

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ReportRow("Sold deschidere",   formatAmount(report.openingBalance, report.currency), neutral)
        ReportRow("Sold închidere",    formatAmount(report.closingBalance, report.currency), neutral)
        ReportRow("Total credite",     formatAmount(report.totalCredits,   report.currency), success)
        ReportRow("Total debite",      formatAmount(report.totalDebits,    report.currency), danger)
        ReportRow("Total comisioane",  formatAmount(report.totalFees,      report.currency), danger)
        ReportRow("Nr. tranzacții",    report.transactionCount.toString(),                  neutral)
    }
}

// ── Section 2: Sumar Anual ────────────────────────────────────────────────────

@Composable
private fun AnnualSummarySection(
    uiState: ReportsUiState,
    viewModel: ReportsViewModel,
    isDark: Boolean
) {
    val success = if (isDark) BaSuccessDark else BaSuccess
    val danger  = if (isDark) BaDangerDark  else BaDanger

    ReportCard(title = "Sumar Anual") {
        CompactDropdown(
            label    = "An",
            current  = uiState.annualYear.toString(),
            options  = yearRange.map { it.toString() },
            onSelect = { viewModel.setAnnualYear(it.toInt()) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        BaSecondaryButton(
            text     = if (uiState.isLoadingAnnual) "Se încarcă…" else "Generează",
            onClick  = { viewModel.loadAnnualSummary() },
            enabled  = !uiState.isLoadingAnnual,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.annualSummary?.let { summary ->
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            ReportRow("Total credite",  formatAmount(summary.totalCredits, summary.currency), success)
            Spacer(Modifier.height(6.dp))
            ReportRow("Total debite",   formatAmount(summary.totalDebits,  summary.currency), danger)
            Spacer(Modifier.height(6.dp))
            val netColor = if (summary.netChange >= 0) success else danger
            ReportRow("Modificare netă", formatAmount(summary.netChange,   summary.currency), netColor)

            if (summary.monthlyBreakdowns.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                AnnualBarChart(
                    breakdowns = summary.monthlyBreakdowns,
                    isDark     = isDark,
                    modifier   = Modifier.fillMaxWidth().height(120.dp)
                )
            }
        }
    }
}

@Composable
private fun AnnualBarChart(
    breakdowns: List<MonthlyBreakdown>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val labelColor = (if (isDark) BaDarkInk3 else BaLightInk3).toArgb()
    val maxVal     = breakdowns.maxOfOrNull { it.totalDebits }?.takeIf { it > 0 } ?: 1.0

    Canvas(modifier = modifier) {
        val chartH = size.height - 20.dp.toPx()
        val n      = 12
        val barW   = (size.width / n) * 0.55f
        val step   = size.width / n

        breakdowns.take(n).forEachIndexed { idx, mb ->
            val x     = idx * step + (step - barW) / 2f
            val ratio = (mb.totalDebits / maxVal).toFloat().coerceIn(0f, 1f)
            val barH  = (ratio * chartH).coerceAtLeast(2.dp.toPx())

            drawRoundRect(
                color        = BaGold.copy(alpha = 0.85f),
                topLeft      = Offset(x, chartH - barH),
                size         = Size(barW, barH),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(
                shortMonths.getOrElse(idx) { "" },
                x + barW / 2f,
                size.height,
                android.graphics.Paint().apply {
                    color     = labelColor
                    textSize  = 9.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

// ── Section 3: Cheltuieli pe Categorii ───────────────────────────────────────

@Composable
private fun SpendingByCategorySection(
    uiState: ReportsUiState,
    viewModel: ReportsViewModel,
    isDark: Boolean
) {
    val danger = if (isDark) BaDangerDark else BaDanger

    ReportCard(title = "Cheltuieli pe Perioadă") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BaInput(
                value         = uiState.spendingFrom,
                onValueChange = { viewModel.setSpendingFrom(it) },
                label         = "De la (YYYY-MM-DD)",
                modifier      = Modifier.weight(1f)
            )
            BaInput(
                value         = uiState.spendingTo,
                onValueChange = { viewModel.setSpendingTo(it) },
                label         = "Până la",
                modifier      = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        BaSecondaryButton(
            text     = if (uiState.isLoadingSpending) "Se încarcă…" else "Analizează",
            onClick  = { viewModel.loadSpending() },
            enabled  = !uiState.isLoadingSpending,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.spendingData?.let { data ->
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            if (data.categories.isEmpty()) {
                Text(
                    "Nicio cheltuială în această perioadă.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DonutChart(
                        categories = data.categories,
                        modifier   = Modifier.size(180.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                ReportRow(
                    label      = "Total cheltuieli",
                    value      = formatAmount(data.totalSpending, "MDL"),
                    valueColor = danger
                )
                Spacer(Modifier.height(8.dp))
                data.categories.forEachIndexed { idx, cat ->
                    CategoryLegendRow(
                        color      = chartPalette.getOrElse(idx) { BaGold },
                        label      = cat.category,
                        amount     = formatAmount(cat.amount, "MDL"),
                        percentage = cat.percentage
                    )
                    if (idx < data.categories.lastIndex) Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    categories: List<CategoryBreakdown>,
    modifier: Modifier = Modifier
) {
    val total = categories.sumOf { it.amount }.takeIf { it > 0 } ?: 1.0

    Canvas(modifier = modifier) {
        val strokeW    = 36.dp.toPx()
        val radius     = (size.minDimension / 2f) - strokeW / 2f
        val center     = Offset(size.width / 2f, size.height / 2f)
        var startAngle = -90f

        categories.forEachIndexed { idx, cat ->
            val sweep = ((cat.amount / total) * 360f).toFloat()
            val color = chartPalette.getOrElse(idx) { BaGold }
            drawArc(
                color      = color,
                startAngle = startAngle,
                sweepAngle = (sweep - 2f).coerceAtLeast(0f),
                useCenter  = false,
                topLeft    = Offset(center.x - radius, center.y - radius),
                size       = Size(radius * 2, radius * 2),
                style      = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun CategoryLegendRow(
    color: Color,
    label: String,
    amount: String,
    percentage: Double
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text  = "$amount (${String.format("%.1f", percentage)}%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Section 4: Venituri vs Cheltuieli ────────────────────────────────────────

@Composable
private fun IncomeVsExpensesSection(
    uiState: ReportsUiState,
    viewModel: ReportsViewModel,
    isDark: Boolean
) {
    val success = if (isDark) BaSuccessDark else BaSuccess
    val danger  = if (isDark) BaDangerDark  else BaDanger

    ReportCard(title = "Venituri vs Cheltuieli") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BaInput(
                value         = uiState.incomeFrom,
                onValueChange = { viewModel.setIncomeFrom(it) },
                label         = "De la (YYYY-MM-DD)",
                modifier      = Modifier.weight(1f)
            )
            BaInput(
                value         = uiState.incomeTo,
                onValueChange = { viewModel.setIncomeTo(it) },
                label         = "Până la",
                modifier      = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        BaSecondaryButton(
            text     = if (uiState.isLoadingIncome) "Se încarcă…" else "Analizează",
            onClick  = { viewModel.loadIncomeVsExpenses() },
            enabled  = !uiState.isLoadingIncome,
            modifier = Modifier.fillMaxWidth()
        )

        uiState.incomeData?.let { data ->
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            if (data.periods.isNotEmpty()) {
                GroupedBarChart(
                    data         = data,
                    successColor = success,
                    dangerColor  = danger,
                    isDark       = isDark,
                    modifier     = Modifier.fillMaxWidth().height(140.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(color = success, label = "Venituri")
                    LegendDot(color = danger,  label = "Cheltuieli")
                }
                Spacer(Modifier.height(12.dp))
            }

            val netColor = if (data.netBalance >= 0) success else danger
            Text(
                text  = formatAmount(data.netBalance, "MDL"),
                style = MaterialTheme.typography.displaySmall.copy(fontStyle = FontStyle.Italic),
                color = netColor
            )
            Text(
                text  = "Bilanț net",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GroupedBarChart(
    data: IncomeVsExpensesResponse,
    successColor: Color,
    dangerColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val labelColor = (if (isDark) BaDarkInk3 else BaLightInk3).toArgb()
    val periods    = data.periods
    val maxVal     = periods.maxOfOrNull { maxOf(it.income, it.expenses) }?.takeIf { it > 0 } ?: 1.0

    Canvas(modifier = modifier) {
        val chartH  = size.height - 20.dp.toPx()
        val n       = periods.size.coerceAtLeast(1)
        val groupW  = size.width / n
        val barW    = (groupW * 0.38f)
        val sep     = groupW * 0.04f

        periods.forEachIndexed { idx, period ->
            val gx = idx * groupW + groupW * 0.06f

            val ih = ((period.income   / maxVal) * chartH).toFloat().coerceAtLeast(2f)
            val eh = ((period.expenses / maxVal) * chartH).toFloat().coerceAtLeast(2f)

            drawRoundRect(
                color        = successColor.copy(alpha = 0.85f),
                topLeft      = Offset(gx, chartH - ih),
                size         = Size(barW, ih),
                cornerRadius = CornerRadius(3.dp.toPx())
            )
            drawRoundRect(
                color        = dangerColor.copy(alpha = 0.85f),
                topLeft      = Offset(gx + barW + sep, chartH - eh),
                size         = Size(barW, eh),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(
                period.period.take(7),
                gx + barW,
                size.height,
                android.graphics.Paint().apply {
                    color     = labelColor
                    textSize  = 8.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Shared components ─────────────────────────────────────────────────────────

@Composable
private fun ReportCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun CompactDropdown(
    label: String,
    current: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(current, style = MaterialTheme.typography.bodyMedium)
                Icon(
                    imageVector        = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text    = { Text(opt, style = MaterialTheme.typography.bodyMedium) },
                        onClick = { onSelect(opt); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun maskIban(iban: String): String =
    if (iban.length < 6) iban else "${iban.take(4)} ••• ${iban.takeLast(4)}"

private fun accountTypeLabel(type: String) = when (type) {
    "Current"  -> "Curent"
    "Savings"  -> "Economii"
    "Business" -> "Business"
    else       -> type
}

private fun formatAmount(amount: Double, currency: String): String =
    "${String.format("%,.2f", amount)} $currency"
