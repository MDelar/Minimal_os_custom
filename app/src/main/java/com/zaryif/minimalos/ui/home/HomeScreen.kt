package com.zaryif.minimalos.ui.home

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.data.repository.ContactRepository
import com.zaryif.minimalos.data.repository.StorageRepository
import com.zaryif.minimalos.domain.model.AppInfo
import com.zaryif.minimalos.domain.model.Contact
import com.zaryif.minimalos.domain.usecase.CommandResult
import com.zaryif.minimalos.domain.usecase.ExecuteCommandUseCase
import com.zaryif.minimalos.ui.components.CommandBar
import com.zaryif.minimalos.ui.components.CommandHelpDialog
import com.zaryif.minimalos.ui.components.CommandHistoryChatStream
import com.zaryif.minimalos.ui.components.MinimalDynamicHomeWidgets
import com.zaryif.minimalos.ui.components.MinimalLiveTimerCard
import com.zaryif.minimalos.ui.components.MinimalNotificationBanner
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    installedApps: List<AppInfo>,
    storageRepository: StorageRepository,
    contactRepository: ContactRepository,
    userPreferences: UserPreferences,
    executeCommandUseCase: ExecuteCommandUseCase,
    appWidgetManager: AppWidgetManager? = null,
    appWidgetHost: AppWidgetHost? = null,
    onAddHomeWidget: (() -> Unit)? = null,
    onDeleteHomeWidget: ((Int) -> Unit)? = null,
    onNavigateToApps: () -> Unit,
    onNavigateToHub: () -> Unit,
    onNavigateToWidgets: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onOpenMemo: () -> Unit,
    onOpenClock: () -> Unit
) {
    val context = LocalContext.current
    val todos by storageRepository.todos.collectAsState()
    val notes by storageRepository.notes.collectAsState()
    val wallpaperUriString by userPreferences.customWallpaperUri.collectAsState(initial = null)
    val favoriteAppPackages by userPreferences.favoriteApps.collectAsState(initial = emptySet())
    val homeWidgetsOrder by userPreferences.homeWidgets.collectAsState(initial = UserPreferences.DEFAULT_HOME_WIDGETS)
    val homeSystemWidgets by userPreferences.homeSystemWidgets.collectAsState(initial = emptySet())
    val showAllTodosPref by userPreferences.homeShowAllTodos.collectAsState(initial = false)
    val showAllNotesPref by userPreferences.homeShowAllNotes.collectAsState(initial = false)
    val showCommandFeedback by userPreferences.showCommandFeedback.collectAsState(initial = false)
    val assistantPackage by userPreferences.assistantPackage.collectAsState(initial = null)
    val commandHistoryEnabled by userPreferences.commandHistoryEnabled.collectAsState(initial = true)
    val commandHistory by userPreferences.commandHistory.collectAsState(initial = emptyList())
    val customCommands by userPreferences.customCommands.collectAsState(initial = emptyMap())

    val scope = rememberCoroutineScope()
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var matchingApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var matchingContacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var hasContactPermission by remember { mutableStateOf(contactRepository.hasPermission()) }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasContactPermission = isGranted
    }

    val isImeVisible = WindowInsets.isImeVisible
    val isTyping = isImeVisible || query.isNotEmpty()

    val favoriteApps = remember(favoriteAppPackages, installedApps) {
        installedApps.filter { it.packageName in favoriteAppPackages }
    }

    // Live clock
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTime = timeFormat.format(now)
            currentDate = dateFormat.format(now).lowercase()
            delay(1000)
        }
    }

    // Dynamic autocomplete for apps and contacts as query changes
    LaunchedEffect(query, hasContactPermission) {
        val lower = query.lowercase().trim()

        // 1. Contacts search for calling/messaging/whatsapp
        matchingContacts = if ((lower.startsWith("#") || lower.startsWith("call") || lower.startsWith("dial") ||
            lower.startsWith("@") || lower.startsWith("msg") || lower.startsWith("sms") ||
            lower.startsWith("message") || lower.startsWith("=") || lower.startsWith("wa") || lower.startsWith("whatsapp")) &&
            !lower.contains(":")
        ) {
            val contactQuery = when {
                lower.startsWith("#") || lower.startsWith("@") || lower.startsWith("=") -> lower.drop(1).trim()
                lower.startsWith("call ") -> lower.removePrefix("call ").trim()
                lower.startsWith("dial ") -> lower.removePrefix("dial ").trim()
                lower.startsWith("msg ") -> lower.removePrefix("msg ").trim()
                lower.startsWith("sms ") -> lower.removePrefix("sms ").trim()
                lower.startsWith("message ") -> lower.removePrefix("message ").trim()
                lower.startsWith("wa ") -> lower.removePrefix("wa ").trim()
                lower.startsWith("whatsapp ") -> lower.removePrefix("whatsapp ").trim()
                lower == "call" || lower == "dial" || lower == "msg" || lower == "sms" || lower == "message" || lower == "wa" || lower == "whatsapp" -> ""
                else -> ""
            }
            if (hasContactPermission) {
                contactRepository.searchContacts(contactQuery)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }

        // 2. Apps search
        val isUninstallCommand = lower.startsWith("uninstall ") || lower.startsWith("remove ") ||
                lower.startsWith("rmapp ") || lower == "uninstall" || lower == "remove" || lower == "rmapp" ||
                (query.startsWith("/") && query.length >= 1)
        val uninstallQuery = when {
            lower.startsWith("uninstall ") -> lower.substring(10).trim()
            lower.startsWith("remove ") -> lower.substring(7).trim()
            lower.startsWith("rmapp ") -> lower.substring(6).trim()
            query.startsWith("/") -> lower.substring(1).trim()
            else -> ""
        }

        matchingApps = if (isUninstallCommand) {
            if (uninstallQuery.isNotEmpty()) {
                installedApps.filter {
                    it.label.lowercase().contains(uninstallQuery) ||
                    it.packageName.lowercase().contains(uninstallQuery)
                }
            } else {
                installedApps.take(10)
            }
        } else if (query.isNotBlank() && !query.startsWith("@") &&
            !query.startsWith("#") && !query.startsWith("-") &&
            !query.startsWith("!") && !query.startsWith("*") &&
            !query.startsWith("+") && !query.startsWith(":") &&
            !query.startsWith("?") && !query.startsWith("^") &&
            !query.startsWith("&") && !query.startsWith("=") &&
            !query.startsWith("$") && !query.startsWith("%") &&
            !query.startsWith("/") &&
            !lower.startsWith("call ") && !lower.startsWith("dial ") &&
            !lower.startsWith("msg ") && !lower.startsWith("sms ") &&
            !lower.startsWith("wa ") && !lower.startsWith("whatsapp ") &&
            !lower.startsWith("note ") && !lower.startsWith("todo ") &&
            !lower.startsWith("event ") && !lower.startsWith("alarm ") &&
            !lower.startsWith("timer ") && !lower.startsWith("pomo") &&
            !lower.startsWith("pomodoro ") && !lower.startsWith("yt ") &&
            !lower.startsWith("youtube ") && !lower.startsWith("assistant ") &&
            !lower.startsWith("ai ") && !lower.startsWith("ask ") &&
            !lower.startsWith("drive ") && !lower.startsWith("email ")
        ) {
            installedApps.filter { it.label.lowercase().contains(lower) }
        } else {
            emptyList()
        }
    }

    // Auto-dismiss banner after 3 seconds
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            delay(3000)
            notificationMessage = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBlack)
    ) {
        // Custom or Preset Wallpaper background
        if (!wallpaperUriString.isNullOrBlank()) {
            when (wallpaperUriString) {
                "DEFAULT_MIDNIGHT" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF04060E), Color(0xFF0B1222), Color(0xFF04060E))
                                )
                            )
                    )
                }
                "DEFAULT_SPACE_GRAY" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF101012), Color(0xFF1C1C1E), Color(0xFF0E0E10))
                                )
                            )
                    )
                }
                "DEFAULT_TITANIUM" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF090611), Color(0xFF171026), Color(0xFF07050E))
                                )
                            )
                    )
                }
                "DEFAULT_AMOLED" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }
                else -> {
                    val bitmap = remember(wallpaperUriString) {
                        try {
                            val path = wallpaperUriString!!
                            val file = File(path)
                            if (file.exists()) {
                                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                BitmapFactory.decodeFile(file.absolutePath, opts)
                                var sampleSize = 1
                                val maxDim = 2048
                                while (opts.outWidth / sampleSize > maxDim || opts.outHeight / sampleSize > maxDim) {
                                    sampleSize *= 2
                                }
                                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                                BitmapFactory.decodeFile(file.absolutePath, decodeOpts)
                            } else {
                                val uri = Uri.parse(path)
                                context.contentResolver.openInputStream(uri)?.use { stream ->
                                    BitmapFactory.decodeStream(stream)
                                }
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Custom Wallpaper",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Dark scrim over wallpaper to guarantee high contrast
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.72f))
                        )
                    }
                }
            }
        }

        // Main Home Screen Layout anchored perfectly above keyboard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        val (x, y) = dragAmount
                        if (!isTyping) {
                            // Clear horizontal swipes for Hub / Widgets Deck
                            if (x > 50 && kotlin.math.abs(x) > kotlin.math.abs(y) * 1.5f) {
                                onNavigateToHub()
                            } else if (x < -50 && kotlin.math.abs(x) > kotlin.math.abs(y) * 1.5f) {
                                onNavigateToWidgets()
                            }
                        }
                    }
                }
                .padding(horizontal = 22.dp, vertical = 8.dp)
        ) {
            // 1. Top Bar: Title & Mini Apps Shortcuts (only when not typing for maximum vertical clearance)
            if (!isTyping) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "minimal",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 1.sp
                    )

                    // Mini Apps Shortcuts (Assistant, Clock, Memo, Settings)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { launchNativeAssistant(context, assistantPackage) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Assistant",
                                tint = MinimalRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onOpenClock, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Clock",
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onOpenMemo, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Memo",
                                tint = MinimalGray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Center Area: WEIGHT(1f) ensures it shrinks when keyboard opens so suggestions and CommandBar are NEVER cut off
            if (!isTyping) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Full home screen widgets & large clock
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentTime,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-2).sp,
                            color = MinimalWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentDate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = MinimalGray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live countdown card for active Pomodoro or Timer sessions
                        // Only display if Pomodoro is not already a configured home widget to avoid duplicate cards
                        val hasPomodoroHomeWidget = homeWidgetsOrder.any { it in listOf("POMODORO", "TIMERS", "LIVE_TIMER") }
                        if (!hasPomodoroHomeWidget) {
                            MinimalLiveTimerCard(modifier = Modifier.padding(bottom = 12.dp))
                        }

                        // Dynamic Customizable Home Screen Widgets
                        MinimalDynamicHomeWidgets(
                            widgetsOrder = homeWidgetsOrder,
                            todos = todos,
                            notes = notes,
                            favoriteApps = favoriteApps,
                            homeSystemWidgetIds = homeSystemWidgets,
                            appWidgetManager = appWidgetManager,
                            appWidgetHost = appWidgetHost,
                            showAllTodosPreference = showAllTodosPref,
                            showAllNotesPreference = showAllNotesPref,
                            onToggleTodo = { id -> storageRepository.toggleTodo(id) },
                            onOpenMemo = onOpenMemo,
                            onOpenCalendar = {
                                val calIntent = Intent(Intent.ACTION_VIEW).apply {
                                    data = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                try {
                                    context.startActivity(calIntent)
                                } catch (_: Exception) {}
                            },
                            onOpenClock = onOpenClock,
                            onLaunchApp = { app ->
                                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                intent?.let { context.startActivity(it) }
                            },
                            onAddHomeWidget = onAddHomeWidget,
                            onDeleteHomeWidget = onDeleteHomeWidget
                        )
                    }
                }
            } else if (commandHistoryEnabled) {
                CommandHistoryChatStream(
                    history = commandHistory,
                    currentQuery = query,
                    onSelectCommand = { selected ->
                        query = selected
                    },
                    onClearHistory = {
                        scope.launch {
                            userPreferences.clearCommandHistory()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // 3. Bottom Area: Gesture hints (only when idle) + CommandBar (ALWAYS VISIBLE ABOVE KEYBOARD)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            val (x, y) = dragAmount
                            if (!isTyping && y < -30 && kotlin.math.abs(y) > kotlin.math.abs(x)) {
                                onNavigateToApps()
                            }
                        }
                    }
            ) {
                if (!isTyping) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "<- hub",
                            fontSize = 11.sp,
                            color = MinimalDarkGray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigateToHub() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "↑ apps",
                            fontSize = 11.sp,
                            color = MinimalDarkGray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigateToApps() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "widgets ->",
                            fontSize = 11.sp,
                            color = MinimalDarkGray,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onNavigateToWidgets() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Command Bar anchored firmly at bottom of visible window
                CommandBar(
                    query = query,
                    onQueryChange = { query = it },
                    onExecute = { input ->
                        if (commandHistoryEnabled) {
                            scope.launch {
                                userPreferences.addCommandToHistory(input)
                            }
                        }
                        val result = executeCommandUseCase.execute(input)
                        when (result) {
                            is CommandResult.Success -> {
                                query = ""
                                if (showCommandFeedback) {
                                    notificationMessage = result.message
                                }
                            }
                            is CommandResult.LaunchedApp -> {
                                query = ""
                            }
                            is CommandResult.ShowHelp -> {
                                query = ""
                                showHelpDialog = true
                            }
                            is CommandResult.Error -> {
                                notificationMessage = result.error
                            }
                        }
                    },
                    matchingApps = matchingApps,
                    onAppSelected = { app ->
                        val trimmedLower = query.trim().lowercase()
                        val isUninstall = trimmedLower.startsWith("uninstall") ||
                                trimmedLower.startsWith("remove") ||
                                trimmedLower.startsWith("rmapp") ||
                                query.trim().startsWith("/")
                        query = ""
                        if (isUninstall) {
                            val result = executeCommandUseCase.execute("uninstall ${app.packageName}")
                            when (result) {
                                is CommandResult.Success -> notificationMessage = result.message
                                is CommandResult.Error -> notificationMessage = result.error
                                else -> {}
                            }
                        } else {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            launchIntent?.let { context.startActivity(it) }
                        }
                    },
                    matchingContacts = matchingContacts,
                    onContactSelected = { contact ->
                        if (query.startsWith("wa") || query.startsWith("=")) {
                            query = "wa ${contact.name}: "
                        } else if (query.startsWith("#") || query.startsWith("call") || query.startsWith("dial")) {
                            query = ""
                            executeCommandUseCase.execute("#${contact.phoneNumber}")
                        } else {
                            query = "@ ${contact.name}: "
                        }
                    },
                    commandHistory = commandHistory,
                    isHistoryEnabled = commandHistoryEnabled,
                    customAliases = customCommands.keys,
                    hasContactPermission = hasContactPermission,
                    onRequestContactPermission = {
                        contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    onHelpRequested = { showHelpDialog = true }
                )
            }
        }

        // Minimal OS Pop Banner (Top aligned)
        MinimalNotificationBanner(
            message = notificationMessage,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Interactive Command Box Guide & Cheat Sheet
        if (showHelpDialog) {
            CommandHelpDialog(
                onDismiss = { showHelpDialog = false },
                onSelectExample = { example ->
                    query = example
                }
            )
        }
    }
}

private fun launchNativeAssistant(context: Context, assistantPackage: String?) {
    val pm = context.packageManager

    // 1. If user chose a specific installed assistant app
    if (!assistantPackage.isNullOrBlank() && assistantPackage != "SYSTEM_DEFAULT") {
        try {
            val assistIntent = Intent(Intent.ACTION_ASSIST).apply {
                `package` = assistantPackage
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (pm.resolveActivity(assistIntent, 0) != null) {
                context.startActivity(assistIntent)
                return
            }
        } catch (_: Exception) {}

        val launchIntent = pm.getLaunchIntentForPackage(assistantPackage)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (launchIntent != null) {
            try {
                context.startActivity(launchIntent)
                return
            } catch (_: Exception) {}
        }
    }

    // 2. Default to Android OS digital assistant / voice command
    val voiceIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    try {
        context.startActivity(voiceIntent)
    } catch (_: Exception) {
        val assistIntent = Intent(Intent.ACTION_ASSIST).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(assistIntent)
        } catch (_: Exception) {
            val geminiIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.bard")
            if (geminiIntent != null) {
                geminiIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(geminiIntent)
            } else {
                val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra(SearchManager.QUERY, "")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(searchIntent)
                } catch (_: Exception) {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://gemini.google.com/app")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(browserIntent)
                }
            }
        }
    }
}
