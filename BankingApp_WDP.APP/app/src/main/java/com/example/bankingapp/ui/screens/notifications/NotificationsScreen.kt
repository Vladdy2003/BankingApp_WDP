package com.example.bankingapp.ui.screens.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankingapp.data.model.notification.NotificationResponse
import com.example.bankingapp.ui.theme.BaDanger
import com.example.bankingapp.ui.theme.BaDangerDark
import com.example.bankingapp.ui.theme.BaDarkBg
import com.example.bankingapp.ui.theme.BaDarkInk3
import com.example.bankingapp.ui.theme.BaGold
import com.example.bankingapp.ui.theme.BaGoldDark
import com.example.bankingapp.ui.theme.BaLightBg
import com.example.bankingapp.ui.theme.BaLightInk3
import com.example.bankingapp.ui.theme.BaSuccess
import com.example.bankingapp.ui.theme.BaSuccessDark
import java.text.SimpleDateFormat
import java.util.Locale

// ── Time formatting ───────────────────────────────────────────────────────────

private fun formatNotificationTime(isoDate: String): String {
    return try {
        val sdf    = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date   = sdf.parse(isoDate) ?: return isoDate
        val diffMs = System.currentTimeMillis() - date.time
        val diffMin  = diffMs / 60_000L
        val diffHour = diffMs / 3_600_000L
        val diffDay  = diffMs / 86_400_000L
        when {
            diffMin < 1   -> "acum"
            diffMin < 60  -> "acum $diffMin min"
            diffHour < 24 -> "acum $diffHour ore"
            diffDay == 1L -> "ieri"
            else          -> SimpleDateFormat("d MMM", Locale("ro", "RO")).format(date)
        }
    } catch (_: Exception) { isoDate }
}

// ── Icon helpers ──────────────────────────────────────────────────────────────

private fun notificationIcon(type: String): ImageVector = when (type.lowercase()) {
    "transaction" -> Icons.Default.SwapHoriz
    "security"    -> Icons.Default.Security
    "system"      -> Icons.Default.Info
    else          -> Icons.Default.Star
}

@Composable
private fun notificationIconColor(type: String, isDark: Boolean): Color = when (type.lowercase()) {
    "transaction" -> if (isDark) BaSuccessDark else BaSuccess
    "security"    -> if (isDark) BaDangerDark  else BaDanger
    "system"      -> if (isDark) BaGoldDark    else BaGold
    else          -> if (isDark) BaDarkInk3    else BaLightInk3
}

// ── Filter chips ──────────────────────────────────────────────────────────────

private val FILTERS = listOf("TOATE", "TRANZACȚIE", "SECURITATE", "SISTEM", "MARKETING")

// ── Main Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val uiState          by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDark            = isSystemInDarkTheme()
    val listState         = rememberLazyListState()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= layout.totalItemsCount - 4 && lastVisible >= 0
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "Notificări",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Înapoi",
                            tint               = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::markAllAsRead) {
                        Text(
                            text  = "Marchează toate",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isDark) BaGoldDark else BaGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        containerColor  = MaterialTheme.colorScheme.background
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

        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            // Filter chips
            item {
                LazyRow(
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FILTERS) { filter ->
                        FilterChip(
                            selected = uiState.activeFilter == filter,
                            onClick  = { viewModel.setFilter(filter) },
                            label    = {
                                Text(
                                    text  = filter,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isDark) BaGoldDark else BaGold,
                                selectedLabelColor     = if (isDark) BaDarkBg   else BaLightBg
                            )
                        )
                    }
                }
            }

            // Empty state
            if (uiState.filteredNotifications.isEmpty()) {
                item {
                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.NotificationsNone,
                                contentDescription = null,
                                modifier           = Modifier.size(48.dp),
                                tint               = if (isDark) BaDarkInk3 else BaLightInk3
                            )
                            Text(
                                text      = "Nicio notificare",
                                style     = MaterialTheme.typography.bodyLarge,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredNotifications,
                    key   = { it.id }
                ) { notification ->
                    SwipeableNotificationItem(
                        notification = notification,
                        isDark       = isDark,
                        onTap        = { viewModel.markAsRead(notification.id) },
                        onDelete     = { viewModel.deleteNotification(notification.id) }
                    )
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outline,
                        thickness = 0.5.dp
                    )
                }

                // Load-more spinner
                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color    = if (isDark) BaGoldDark else BaGold,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Swipeable wrapper ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNotificationItem(
    notification: NotificationResponse,
    isDark: Boolean,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state                        = dismissState,
        enableDismissFromStartToEnd  = false,
        enableDismissFromEndToStart  = true,
        backgroundContent            = { DeleteBackground(dismissState.targetValue, isDark) }
    ) {
        NotificationItemContent(
            notification = notification,
            isDark       = isDark,
            onTap        = onTap
        )
    }
}

@Composable
private fun DeleteBackground(targetValue: SwipeToDismissBoxValue, isDark: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = if (targetValue == SwipeToDismissBoxValue.EndToStart)
            (if (isDark) BaDangerDark else BaDanger)
        else
            (if (isDark) BaDangerDark else BaDanger).copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 180),
        label = "deleteBackground"
    )
    Box(
        modifier         = Modifier.fillMaxSize().background(bgColor),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier          = Modifier.padding(end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Șterge",
                tint               = Color.White,
                modifier           = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text  = "Șterge",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

// ── Notification item content ─────────────────────────────────────────────────

@Composable
private fun NotificationItemContent(
    notification: NotificationResponse,
    isDark: Boolean,
    onTap: () -> Unit
) {
    val bgColor = if (!notification.isRead)
        MaterialTheme.colorScheme.surface
    else
        MaterialTheme.colorScheme.background

    val dotColor = if (!notification.isRead)
        (if (isDark) BaGoldDark else BaGold)
    else
        Color.Transparent

    val titleColor = if (!notification.isRead)
        MaterialTheme.colorScheme.onBackground
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    val iconColor = notificationIconColor(notification.type, isDark)

    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable(onClick = onTap)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Unread dot
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(Modifier.width(10.dp))

        // Type icon
        Box(
            modifier         = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = notificationIcon(notification.type),
                contentDescription = null,
                tint               = iconColor,
                modifier           = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Text block
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = notification.title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text     = notification.message,
                style    = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // Timestamp
        Text(
            text  = formatNotificationTime(notification.createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = if (isDark) BaDarkInk3 else BaLightInk3
        )
    }
}
