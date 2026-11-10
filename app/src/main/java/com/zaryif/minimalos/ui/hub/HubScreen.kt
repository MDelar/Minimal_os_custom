package com.zaryif.minimalos.ui.hub

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.data.repository.NotificationHistoryRepository
import com.zaryif.minimalos.domain.media.MediaTrackInfo
import com.zaryif.minimalos.domain.media.MinimalMediaManager
import com.zaryif.minimalos.domain.model.NotificationCategory
import com.zaryif.minimalos.domain.model.NotificationItem
import com.zaryif.minimalos.service.MinimalNotificationService
import com.zaryif.minimalos.ui.components.MinimalCard
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.util.AppStrings
import com.zaryif.minimalos.util.NotificationExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun HubScreen(
    userPreferences: UserPreferences? = null,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember(context, userPreferences) { userPreferences ?: UserPreferences(context) }
    val isHistoryEnabled by prefs.notificationHistoryEnabled.collectAsState(initial = true)
    val liveNotifications by MinimalNotificationService.notifications.collectAsState()
    val historyRepo = remember { NotificationHistoryRepository.getInstance(context) }
    val archivedNotifications by historyRepo.history.collectAsState()
    val currentTrack by MinimalMediaManager.currentTrack.collectAsState()

    var isArchiveMode by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(NotificationCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showExportSheet by remember { mutableStateOf(false) }

    val isPermissionGranted = remember { MinimalNotificationService.isPermissionGranted(context) }

    // Refresh media on Hub entry
    LaunchedEffect(Unit) {
        MinimalMediaManager.refresh(context)
        historyRepo.reloadHistory()
    }

    val categories = listOf(
        NotificationCategory.ALL,
        NotificationCategory.MESSAGE,
        NotificationCategory.CALLS,
        NotificationCategory.EMAIL
    )

    // Filter current list (live or archive)
    val activeSource = if (isArchiveMode) archivedNotifications else liveNotifications
    val filteredList = remember(activeSource, selectedCategory, searchQuery) {
        var list = activeSource
        if (selectedCategory != NotificationCategory.ALL) {
            list = list.filter { it.category == selectedCategory }
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.content.lowercase().contains(q) ||
                it.appName.lowercase().contains(q)
            }
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    val (x, y) = dragAmount
                    // Swipe left -> Home
                    if (x < -30 && kotlin.math.abs(x) > kotlin.math.abs(y)) {
                        onBackToHome()
                    }
                }
            }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Header (Apple-Style Clean Hierarchy) ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button + Title & Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackToHome,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MinimalWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = AppStrings.hubTitle,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isPermissionGranted) MinimalRed else MinimalDarkGray)
                            )
                        }
                        Text(
                            text = if (isArchiveMode) AppStrings.hubSubtitleArchive else AppStrings.hubSubtitleLive,
                            fontSize = 11.sp,
                            color = MinimalGray,
                            letterSpacing = 0.2.sp
                        )
                    }
                }

                // Right: High-contrast Search Button & AI Summary Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Visible Search Toggle Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSearchActive) MinimalCardBg else MinimalDarkCardBg)
                            .border(
                                1.dp,
                                if (isSearchActive) MinimalRed else MinimalDarkGray,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                isSearchActive = !isSearchActive
                                if (!isSearchActive) searchQuery = ""
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (isSearchActive) MinimalRed else MinimalWhite,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // AI Intelligence & Export Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                            .clickable { showExportSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Export",
                            tint = MinimalRed,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // ── Inline Search Field ─────────────────────────────────
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MinimalGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = AppStrings.hubSearchPlaceholder,
                                fontSize = 13.sp,
                                color = MinimalDarkGray
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(
                                color = MinimalWhite,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(MinimalRed),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MinimalGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ── Permission Required Banner ──────────────────────────
            if (!isPermissionGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                MinimalCard(
                    title = AppStrings.hubPermissionTitle,
                    subtitle = AppStrings.hubPermissionSubtitle,
                    icon = Icons.Default.Warning,
                    iconColor = MinimalRed,
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // ── Live Music / Media Player Card ──────────────────────
            currentTrack?.let { track ->
                Spacer(modifier = Modifier.height(12.dp))
                MinimalNowPlayingCard(
                    track = track,
                    onTogglePlayPause = { MinimalMediaManager.togglePlayPause(context) },
                    onNext = { MinimalMediaManager.skipToNext(context) },
                    onPrev = { MinimalMediaManager.skipToPrevious(context) },
                    onOpenApp = { MinimalMediaManager.openPlayer(context) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Mode Switcher & Filter Row (Zero Horizontal Scroll, Fits All Screens) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Segmented Control Capsule (Live / Archive)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(MinimalDarkCardBg)
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val liveCount = liveNotifications.size
                    val archiveCount = archivedNotifications.size

                    // Live Tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (!isArchiveMode) MinimalCardBg else Color.Transparent)
                            .clickable { isArchiveMode = false }
                            .padding(horizontal = 11.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (liveCount > 0) "${AppStrings.hubTabLive} $liveCount" else AppStrings.hubTabLive,
                            fontSize = 11.sp,
                            fontWeight = if (!isArchiveMode) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (!isArchiveMode) MinimalWhite else MinimalGray
                        )
                    }

                    // Archive Tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (isArchiveMode) MinimalCardBg else Color.Transparent)
                            .clickable { isArchiveMode = true }
                            .padding(horizontal = 11.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (archiveCount > 0) "${AppStrings.hubTabArchive} $archiveCount" else AppStrings.hubTabArchive,
                            fontSize = 11.sp,
                            fontWeight = if (isArchiveMode) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isArchiveMode) MinimalWhite else MinimalGray
                        )
                    }
                }

                // Category Filter Icons (ALL, MESSAGE, CALLS, EMAIL) - No swiping needed
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        val (icon, label) = when (cat) {
                            NotificationCategory.ALL -> Icons.Default.Notifications to "All"
                            NotificationCategory.MESSAGE -> Icons.Default.ChatBubbleOutline to "Messages"
                            NotificationCategory.CALLS -> Icons.Default.Phone to "Calls"
                            NotificationCategory.EMAIL -> Icons.Default.Email to "Email"
                        }

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MinimalCardBg else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) MinimalRed.copy(alpha = 0.8f) else MinimalDarkGray.copy(alpha = 0.4f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedCategory = cat },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) MinimalWhite else MinimalGray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // ── 1-Tap Quick Action: Send to AI Assistant for Instant Summary ──
            if (activeSource.isNotEmpty() && !isSearchActive) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable {
                            scope.launch {
                                val currentAssistant = userPreferences?.assistantPackage?.first()
                                val itemsToExport = if (filteredList.isNotEmpty()) filteredList else activeSource
                                NotificationExporter.shareExport(
                                    context = context,
                                    items = itemsToExport,
                                    forAssistant = true,
                                    assistantPackage = currentAssistant
                                )
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Summarize ${activeSource.size} alerts with AI Assistant",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MinimalWhite
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Summarize",
                        tint = MinimalRed,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Notifications List / Empty State ────────────────────
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalDarkGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No alerts matching \"$searchQuery\""
                                   else if (isArchiveMode) AppStrings.hubEmptyArchiveTitle
                                   else AppStrings.hubEmptyTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MinimalWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArchiveMode) AppStrings.hubEmptyArchiveSubtitle
                                   else AppStrings.hubEmptySubtitle,
                            fontSize = 11.sp,
                            color = MinimalGray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.key.ifEmpty { "${it.id}_${it.timestamp}" } }) { item ->
                        val icon = when (item.category) {
                            NotificationCategory.MESSAGE -> Icons.Default.ChatBubbleOutline
                            NotificationCategory.CALLS -> Icons.Default.Phone
                            NotificationCategory.EMAIL -> Icons.Default.Email
                            else -> Icons.Default.NotificationsNone
                        }

                        val timeDisplay = if (isArchiveMode) {
                            formatFullTimestamp(item.timestamp)
                        } else {
                            formatRelativeTimestamp(item.timestamp)
                        }

                        MinimalHubNotificationCard(
                            item = item,
                            icon = icon,
                            timeDisplay = timeDisplay,
                            isArchive = isArchiveMode,
                            onItemClick = {
                                if (item.pendingIntent != null) {
                                    try {
                                        item.pendingIntent.send()
                                    } catch (_: Exception) {
                                        val launchIntent = context.packageManager.getLaunchIntentForPackage(item.packageName)
                                        launchIntent?.let { context.startActivity(it) }
                                    }
                                } else {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(item.packageName)
                                    launchIntent?.let { context.startActivity(it) }
                                }
                            },
                            onDismiss = {
                                if (isArchiveMode) {
                                    historyRepo.deleteNotification(item.key)
                                } else {
                                    MinimalNotificationService.dismiss(item.key)
                                }
                            },
                            onToggleStar = {
                                if (isArchiveMode) {
                                    historyRepo.toggleFlag(item.key)
                                } else {
                                    MinimalNotificationService.toggleFlag(item.key)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Professional Apple-Style Bottom Footer ──────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Button Capsule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                        .clickable { onBackToHome() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Home",
                        tint = MinimalGray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.hubHomeBtn,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalWhite
                    )
                }

                // Actions: Export & Clear
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Export Log Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                            .clickable {
                                val itemsToExport = if (filteredList.isNotEmpty()) filteredList else activeSource
                                NotificationExporter.downloadToDevice(context, itemsToExport)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download log",
                            tint = MinimalGray,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = AppStrings.hubDownloadLogBtn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MinimalWhite
                        )
                    }

                    // Clear / Clear All Pill (Properly sized and perfectly fitting)
                    if (filteredList.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isArchiveMode) {
                                        showClearHistoryDialog = true
                                    } else {
                                        filteredList.forEach { MinimalNotificationService.dismiss(it.key) }
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear",
                                tint = MinimalGray,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArchiveMode) AppStrings.hubClearBtn else AppStrings.hubClearAllBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalGray
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Confirmation Dialog for Clearing Archive ────────────────────
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Clear notification archive?",
                    color = MinimalWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all stored local notifications from your device.",
                    color = MinimalGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyRepo.clearHistory()
                        showClearHistoryDialog = false
                    }
                ) {
                    Text(text = "Clear All", color = MinimalRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(text = "Cancel", color = MinimalWhite)
                }
            },
            containerColor = MinimalCardBg
        )
    }

    // ── AI Assistant & Export Options Dialog ────────────────────────
    if (showExportSheet) {
        AlertDialog(
            onDismissRequest = { showExportSheet = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hub Intelligence & Export",
                        color = MinimalWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Summarize or export notification logs for AI analysis:",
                        color = MinimalGray,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Option 1: Send to Assistant
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable {
                                showExportSheet = false
                                scope.launch {
                                    val currentAssistant = userPreferences?.assistantPackage?.first()
                                    val itemsToExport = if (filteredList.isNotEmpty()) filteredList else activeSource
                                    NotificationExporter.shareExport(
                                        context = context,
                                        items = itemsToExport,
                                        forAssistant = true,
                                        assistantPackage = currentAssistant
                                    )
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Send to AI Assistant",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                            Text(
                                text = "Formats full log with executive summary prompt",
                                fontSize = 11.sp,
                                color = MinimalGray
                            )
                        }
                    }

                    // Option 2: Export .txt file
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                            .clickable {
                                showExportSheet = false
                                val itemsToExport = if (filteredList.isNotEmpty()) filteredList else activeSource
                                NotificationExporter.shareExport(
                                    context = context,
                                    items = itemsToExport,
                                    forAssistant = false
                                )
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MinimalWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Share plain text log",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                            Text(
                                text = "Share log via messaging or storage apps",
                                fontSize = 11.sp,
                                color = MinimalGray
                            )
                        }
                    }

                    // Option 3: Download to Device
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                            .clickable {
                                showExportSheet = false
                                val itemsToExport = if (filteredList.isNotEmpty()) filteredList else activeSource
                                NotificationExporter.downloadToDevice(
                                    context = context,
                                    items = itemsToExport,
                                    forAssistant = false
                                )
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MinimalWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Save to Downloads",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                            Text(
                                text = "Saves file directly to device Downloads",
                                fontSize = 11.sp,
                                color = MinimalGray
                            )
                        }
                    }

                    // Option 4: Copy to clipboard
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                            .clickable {
                                showExportSheet = false
                                val itemsToExport = if (filteredList.isNotEmpty()) filteredList else activeSource
                                NotificationExporter.copyToClipboard(
                                    context = context,
                                    items = itemsToExport,
                                    forAssistant = true
                                )
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = MinimalWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Copy prompt & log",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                            Text(
                                text = "Copies formatted prompt to clipboard",
                                fontSize = 11.sp,
                                color = MinimalGray
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportSheet = false }) {
                    Text(text = "Close", color = MinimalWhite)
                }
            },
            containerColor = MinimalCardBg
        )
    }
}

// ── Now Playing Card ────────────────────────────────────────────────
@Composable
private fun MinimalNowPlayingCard(
    track: MediaTrackInfo,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onOpenApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onOpenApp() }
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (track.isPlaying) "NOW PLAYING" else "PAUSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (track.isPlaying) MinimalRed else MinimalGray
                    )
                }

                Text(
                    text = track.appName,
                    fontSize = 11.sp,
                    color = MinimalGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onOpenApp() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Track info
            Text(
                text = track.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { onOpenApp() }
            )

            if (track.artist.isNotBlank()) {
                Text(
                    text = track.artist,
                    fontSize = 12.sp,
                    color = MinimalGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable { onOpenApp() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Playback controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrev,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = MinimalWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(if (track.isPlaying) MinimalRed else MinimalDarkCardBg)
                        .border(1.dp, if (track.isPlaying) MinimalRed else MinimalDarkGray, RoundedCornerShape(21.dp))
                        .clickable { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (track.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (track.isPlaying) "Pause" else "Play",
                        tint = MinimalWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Track",
                        tint = MinimalWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Notification Item Card ──────────────────────────────────────────
@Composable
private fun MinimalHubNotificationCard(
    item: NotificationItem,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    timeDisplay: String,
    isArchive: Boolean,
    onItemClick: () -> Unit,
    onDismiss: () -> Unit,
    onToggleStar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
            .clickable { onItemClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.appName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalGray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeDisplay,
                        fontSize = 11.sp,
                        color = MinimalDarkGray
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Star / Flag button
                    Icon(
                        imageVector = if (item.isFlagged) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (item.isFlagged) "Unstar" else "Star",
                        tint = if (item.isFlagged) MinimalRed else MinimalDarkGray,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onToggleStar() }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Dismiss / delete button
                    Icon(
                        imageVector = if (isArchive) Icons.Default.DeleteOutline else Icons.Default.Close,
                        contentDescription = if (isArchive) "Delete from archive" else "Dismiss alert",
                        tint = MinimalDarkGray,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDismiss() }
                    )
                }
            }

            if (item.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MinimalWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (item.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.content,
                    fontSize = 12.sp,
                    color = MinimalGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────
private fun formatRelativeTimestamp(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        else -> "${hours / 24}d"
    }
}

private fun formatFullTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return if (days == 0L) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        "Today " + sdf.format(Date(timestamp))
    } else if (days == 1L) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        "Yesterday " + sdf.format(Date(timestamp))
    } else {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    }
}
