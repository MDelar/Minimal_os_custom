package com.zaryif.minimalos.ui.settings

import android.app.WallpaperManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.zaryif.minimalos.util.AlarmClockPermissionHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.zaryif.minimalos.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zaryif.minimalos.ui.components.CommandHelpDialog
import com.zaryif.minimalos.ui.onboarding.OnboardingScreen
import com.zaryif.minimalos.data.preferences.AiProvider
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.domain.model.AppInfo
import com.zaryif.minimalos.ui.theme.AppleSanFranciscoFontFamily
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalThemeRegistry
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.ui.theme.MinimalOnAccent
import com.zaryif.minimalos.util.AppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    installedApps: List<AppInfo>,
    userPreferences: UserPreferences,
    onNavigateToOnboarding: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Preferences collection
    val assistantPackage by userPreferences.assistantPackage.collectAsState(initial = null)
    val showCommandFeedback by userPreferences.showCommandFeedback.collectAsState(initial = false)
    val showCommandBar by userPreferences.showCommandBar.collectAsState(initial = true)
    val showCommandHelperIcon by userPreferences.showCommandHelperIcon.collectAsState(initial = true)
    val customWallpaperUri by userPreferences.customWallpaperUri.collectAsState(initial = null)
    val favoriteAppPackages by userPreferences.favoriteApps.collectAsState(initial = emptySet())
    val currentThemeId by userPreferences.themeId.collectAsState(initial = "AMOLED_RED")
    val homeWidgets by userPreferences.homeWidgets.collectAsState(initial = UserPreferences.DEFAULT_HOME_WIDGETS)
    val deckWidgetsOrder by userPreferences.deckWidgetsOrder.collectAsState(initial = UserPreferences.DEFAULT_DECK_WIDGETS)
    val customCommands by userPreferences.customCommands.collectAsState(initial = emptyMap())
    val commandHistoryEnabled by userPreferences.commandHistoryEnabled.collectAsState(initial = true)
    val commandHistory by userPreferences.commandHistory.collectAsState(initial = emptyList())
    val notificationHistoryEnabled by userPreferences.notificationHistoryEnabled.collectAsState(initial = true)
    val showAllTodosPref by userPreferences.homeShowAllTodos.collectAsState(initial = false)
    val showAllNotesPref by userPreferences.homeShowAllNotes.collectAsState(initial = false)
    val homeSystemWidgets by userPreferences.homeSystemWidgets.collectAsState(initial = emptySet())
    val deckSystemWidgets by userPreferences.deckSystemWidgets.collectAsState(initial = emptySet())

    // UI state
    var searchQuery by remember { mutableStateOf("") }
    var showAppPickerModal by remember { mutableStateOf(false) }
    var showAddCommandModal by remember { mutableStateOf(false) }
    var showAboutModal by remember { mutableStateOf(false) }
    var showOnboardingModal by remember { mutableStateOf(false) }
    var showCommandGuideModal by remember { mutableStateOf(false) }

    val (appVersionName, appVersionCode) = remember(context) {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val vName = pInfo.versionName ?: "1.8.0"
            val vCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toLong()
            }
            vName to vCode
        } catch (_: Exception) {
            "1.8.0" to 19L
        }
    }

    // Section expansion toggles (top-brand accordion style)
    var isThemeExpanded by remember { mutableStateOf(false) }
    var isWallpaperExpanded by remember { mutableStateOf(false) }
    var isHomeWidgetsExpanded by remember { mutableStateOf(false) }
    var isDeckWidgetsExpanded by remember { mutableStateOf(false) }
    var isAssistantExpanded by remember { mutableStateOf(false) }
    var isAliasesExpanded by remember { mutableStateOf(false) }

    // Photo Gallery Wallpaper Picker
    val wallpaperPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    // Clean up any legacy wallpaper files to avoid storage leakage
                    context.filesDir.listFiles { f -> f.name.startsWith("custom_wallpaper") }?.forEach { it.delete() }
                    val file = File(context.filesDir, "custom_wallpaper.jpg")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    userPreferences.setCustomWallpaperUri(file.absolutePath)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Markdown History File Exporter
    val exportHistoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        val mdContent = userPreferences.generateHistoryMarkdown(commandHistory)
                        output.write(mdContent.toByteArray(Charsets.UTF_8))
                    }
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Command history saved (.md)", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Export error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val favoriteAppsList = remember(favoriteAppPackages, installedApps) {
        installedApps.filter { it.packageName in favoriteAppPackages }
    }

    val activeTheme = remember(currentThemeId) {
        MinimalThemeRegistry.getTheme(currentThemeId)
    }

    // Dynamic Assistant discovery
    val availableAssistants = remember(context) {
        val pm = context.packageManager
        val list = mutableListOf<Triple<String, String, String>>()
        list.add(Triple("SYSTEM_DEFAULT", "System Default Assistant", "OS DEFAULT"))
        val addedPackages = mutableSetOf<String>()

        try {
            val assistIntent = Intent(Intent.ACTION_ASSIST)
            val resolutions = pm.queryIntentActivities(assistIntent, 0)
            for (res in resolutions) {
                val pkg = res.activityInfo.packageName
                if (pkg != context.packageName && !addedPackages.contains(pkg)) {
                    val label = res.loadLabel(pm).toString()
                    addedPackages.add(pkg)
                    list.add(Triple(pkg, label, "ALLOWED ASSIST"))
                }
            }
        } catch (_: Exception) {}

        val knownAi = listOf(
            "com.google.android.apps.bard" to "Google Gemini",
            "com.google.android.googlequicksearchbox" to "Google Assistant",
            "com.openai.chatgpt" to "ChatGPT",
            "com.microsoft.copilot" to "Microsoft Copilot",
            "com.anthropic.claude" to "Claude",
            "ai.perplexity.app" to "Perplexity"
        )

        for ((pkg, name) in knownAi) {
            if (!addedPackages.contains(pkg)) {
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    if (appInfo.enabled) {
                        val label = pm.getApplicationLabel(appInfo).toString().ifBlank { name }
                        addedPackages.add(pkg)
                        list.add(Triple(pkg, label, "INSTALLED AI"))
                    }
                } catch (_: Exception) {}
            }
        }
        list
    }

    val q = searchQuery.trim().lowercase()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MinimalWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = AppStrings.settingsTitle,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = AppStrings.settingsSubtitle,
                        fontSize = 11.sp,
                        color = MinimalGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SEARCH BAR (Top Brand Signature)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalCardBg)
                    .border(1.dp, MinimalDarkGray.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MinimalGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = AppStrings.settingsSearchPlaceholder,
                                fontSize = 14.sp,
                                color = MinimalGray
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            cursorBrush = SolidColor(MinimalRed),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = MinimalWhite,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MinimalGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================================================
            // CATEGORY 1: PERSONALIZATION & APPEARANCE
            // =========================================================
            val appearanceKeywords = listOf("appearance", "theme", "themes", "color", "accent", "wallpaper", "personalization")
            if (q.isEmpty() || appearanceKeywords.any { it.contains(q) || q.contains(it) }) {
                SettingsSectionHeader(title = AppStrings.settingsSecAppearance)
                Spacer(modifier = Modifier.height(8.dp))

                SettingsGroupCard {
                    // Item 1: Theme & Accent
                    SettingsItemRow(
                        icon = Icons.Default.Palette,
                        title = AppStrings.settingsThemeTitle,
                        subtitle = activeTheme.name,
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(activeTheme.accent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isThemeExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MinimalGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = { isThemeExpanded = !isThemeExpanded }
                    )

                    AnimatedVisibility(
                        visible = isThemeExpanded || q.contains("theme") || q.contains("accent"),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MinimalDarkCardBg.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MinimalThemeRegistry.THEMES.forEach { theme ->
                                    val isSelected = theme.id == currentThemeId
                                    Box(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(theme.card)
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) theme.accent else theme.border,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                scope.launch { userPreferences.setThemeId(theme.id) }
                                            }
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(theme.accent)
                                                )
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = theme.accent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = theme.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsInsetDivider()

                    // Item 2: Wallpaper & Background
                    val wallpaperLabel = when {
                        customWallpaperUri == null -> "Default Minimal Black"
                        customWallpaperUri == "DEFAULT_AMOLED" -> "AMOLED Black"
                        customWallpaperUri == "DEFAULT_MIDNIGHT" -> "Midnight"
                        customWallpaperUri == "DEFAULT_SPACE_GRAY" -> "Space Gray"
                        customWallpaperUri == "DEFAULT_TITANIUM" -> "Titanium"
                        else -> "Custom Gallery Photo"
                    }

                    SettingsItemRow(
                        icon = Icons.Default.Wallpaper,
                        title = AppStrings.settingsWallpaperTitle,
                        subtitle = wallpaperLabel,
                        trailingContent = {
                            Icon(
                                imageVector = if (isWallpaperExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { isWallpaperExpanded = !isWallpaperExpanded }
                    )

                    AnimatedVisibility(
                        visible = isWallpaperExpanded || q.contains("wallpaper"),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MinimalDarkCardBg.copy(alpha = 0.5f))
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "CURATED PRESETS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalGray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val wallpaperPresets = listOf(
                                Pair("DEFAULT_AMOLED", "AMOLED Black"),
                                Pair("DEFAULT_MIDNIGHT", "Midnight"),
                                Pair("DEFAULT_SPACE_GRAY", "Space Gray"),
                                Pair("DEFAULT_TITANIUM", "Titanium")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                wallpaperPresets.forEach { (presetKey, presetName) ->
                                    val isSelected = customWallpaperUri == presetKey
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MinimalRed else MinimalDarkCardBg)
                                            .border(1.dp, if (isSelected) MinimalRed else MinimalDarkGray, RoundedCornerShape(8.dp))
                                            .clickable {
                                                scope.launch { userPreferences.setCustomWallpaperUri(presetKey) }
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = presetName,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MinimalOnAccent else MinimalWhite
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Gallery photo and live wallpaper actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MinimalDarkCardBg)
                                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                                        .clickable { wallpaperPickerLauncher.launch("image/*") }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Photo Gallery",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalWhite
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MinimalDarkCardBg)
                                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                                        .clickable {
                                            try {
                                                val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                val intent = Intent(Intent.ACTION_SET_WALLPAPER)
                                                context.startActivity(Intent.createChooser(intent, "Choose Wallpaper"))
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Live Wallpaper",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalWhite
                                    )
                                }
                            }

                            if (customWallpaperUri != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "reset to default theme background",
                                    fontSize = 11.sp,
                                    color = MinimalRed,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable { scope.launch { userPreferences.setCustomWallpaperUri(null) } }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // =========================================================
            // CATEGORY 2: HOME SCREEN & SHORTCUTS
            // =========================================================
            val homeKeywords = listOf("home", "shortcut", "shortcuts", "widget", "widgets", "system", "todo", "task", "tasks", "note", "notes", "alarm", "pomodoro")
            if (q.isEmpty() || homeKeywords.any { it.contains(q) || q.contains(it) }) {
                SettingsSectionHeader(title = AppStrings.settingsSecHome)
                Spacer(modifier = Modifier.height(8.dp))

                SettingsGroupCard {
                    // Item 1: Pinned App Shortcuts
                    SettingsItemRow(
                        icon = Icons.Default.Apps,
                        title = AppStrings.settingsPinnedShortcutsTitle,
                        subtitle = if (favoriteAppsList.isEmpty()) "No shortcuts pinned" else "${favoriteAppsList.size} apps pinned to Home",
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { showAppPickerModal = true }
                    )

                    // Preview chips of pinned shortcuts
                    if (favoriteAppsList.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 56.dp, end = 16.dp, bottom = 12.dp)
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                favoriteAppsList.forEach { app ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MinimalDarkCardBg)
                                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = app.label, fontSize = 11.sp, color = MinimalWhite)
                                    }
                                }
                            }
                        }
                    }

                    SettingsInsetDivider()

                    // Item 2: Home Widgets Layout & Order
                    val allHomeWidgetOptions = listOf(
                        Pair("QUICK_NOTE", "quick note (pinned scratchpad in notification bar)"),
                        Pair("TODOS", "tasks (active focus tasks)"),
                        Pair("NOTES", "notes (recent memos)"),
                        Pair("CALENDAR", "calendar (glance & schedule)"),
                        Pair("SHORTCUTS", "shortcuts (favorite pinned apps)"),
                        Pair("ALARM_COUNTDOWN", "alarm countdown (live time until alarm)"),
                        Pair("POMODORO", "pomodoro (live focus & controls)"),
                        Pair("SYSTEM_WIDGETS", "widgets (native Android app widgets)")
                    )

                    SettingsItemRow(
                        icon = Icons.Default.Widgets,
                        title = AppStrings.settingsHomeWidgetsTitle,
                        subtitle = "${homeWidgets.size} active widgets on Home",
                        trailingContent = {
                            Icon(
                                imageVector = if (isHomeWidgetsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { isHomeWidgetsExpanded = !isHomeWidgetsExpanded }
                    )

                    AnimatedVisibility(
                        visible = isHomeWidgetsExpanded || q.contains("widget") || "widget".contains(q) || q.contains("home"),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MinimalDarkCardBg.copy(alpha = 0.5f))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            homeWidgets.forEachIndexed { index, widgetKey ->
                                val baseLabel = allHomeWidgetOptions.find { it.first == widgetKey }?.second ?: widgetKey
                                val label = if (widgetKey == "SYSTEM_WIDGETS") {
                                    if (homeSystemWidgets.isNotEmpty()) {
                                        "widgets (${homeSystemWidgets.size} app widget${if (homeSystemWidgets.size > 1) "s" else ""} hosted)"
                                    } else {
                                        "widgets (native Android app widgets)"
                                    }
                                } else {
                                    baseLabel
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MinimalCardBg)
                                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalWhite,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (index > 0) {
                                            IconButton(
                                                onClick = {
                                                    val mutable = homeWidgets.toMutableList()
                                                    val temp = mutable[index]
                                                    mutable[index] = mutable[index - 1]
                                                    mutable[index - 1] = temp
                                                    scope.launch { userPreferences.setHomeWidgets(mutable) }
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = MinimalRed, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        if (index < homeWidgets.size - 1) {
                                            IconButton(
                                                onClick = {
                                                    val mutable = homeWidgets.toMutableList()
                                                    val temp = mutable[index]
                                                    mutable[index] = mutable[index + 1]
                                                    mutable[index + 1] = temp
                                                    scope.launch { userPreferences.setHomeWidgets(mutable) }
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = MinimalRed, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                val updated = homeWidgets - widgetKey
                                                scope.launch { userPreferences.setHomeWidgets(updated) }
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = MinimalGray, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }

                            val unadded = allHomeWidgetOptions.filter { it.first !in homeWidgets }
                            if (unadded.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "TAP TO ADD WIDGET TO HOME:",
                                    fontFamily = AppleSanFranciscoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalGray,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    unadded.forEach { (key, name) ->
                                        val shortLabel = when (key) {
                                            "SYSTEM_WIDGETS" -> "widgets (system)"
                                            "ALARM_COUNTDOWN" -> "alarm"
                                            else -> name.split(" ").first()
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MinimalDarkCardBg)
                                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                                .clickable {
                                                    if (key == "ALARM_COUNTDOWN" && !AlarmClockPermissionHelper.canScheduleExactAlarms(context)) {
                                                        AlarmClockPermissionHelper.requestExactAlarmPermission(context)
                                                    }
                                                    scope.launch { userPreferences.setHomeWidgets((homeWidgets + key).distinct()) }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "+ $shortLabel",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MinimalRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsInsetDivider()

                    // Item 3: Show All Tasks on Home
                    SettingsItemRow(
                        icon = Icons.Default.Checklist,
                        title = AppStrings.settingsShowAllTasksTitle,
                        subtitle = if (showAllTodosPref) "Displays complete task list" else "Displays top 2 active focus tasks",
                        trailingContent = {
                            Switch(
                                checked = showAllTodosPref,
                                onCheckedChange = { scope.launch { userPreferences.setHomeShowAllTodos(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MinimalWhite,
                                    checkedTrackColor = MinimalRed,
                                    uncheckedThumbColor = MinimalGray,
                                    uncheckedTrackColor = MinimalDarkCardBg
                                )
                            )
                        }
                    )

                    SettingsInsetDivider()

                    // Item 4: Show All Notes on Home
                    SettingsItemRow(
                        icon = Icons.Default.Description,
                        title = AppStrings.settingsShowAllNotesTitle,
                        subtitle = if (showAllNotesPref) "Displays full memo notes list" else "Displays 2 most recent notes",
                        trailingContent = {
                            Switch(
                                checked = showAllNotesPref,
                                onCheckedChange = { scope.launch { userPreferences.setHomeShowAllNotes(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MinimalWhite,
                                    checkedTrackColor = MinimalRed,
                                    uncheckedThumbColor = MinimalGray,
                                    uncheckedTrackColor = MinimalDarkCardBg
                                )
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // =========================================================
            // CATEGORY 3: WIDGETS DECK
            // =========================================================
            val deckKeywords = listOf("deck", "widget", "widgets", "card", "cards", "pomodoro", "alarm", "alarms", "alias", "aliases", "timer", "timers", "system")
            if (q.isEmpty() || deckKeywords.any { it.contains(q) || q.contains(it) }) {
                SettingsSectionHeader(title = AppStrings.settingsSecDeck)
                Spacer(modifier = Modifier.height(8.dp))

                val allDeckWidgetOptions = listOf(
                    Pair("QUICK_NOTE", "quick note (pinned scratchpad)"),
                    Pair("NOTES", "notes card"),
                    Pair("TODOS", "tasks card"),
                    Pair("CALENDAR", "schedule & events card"),
                    Pair("POMODORO", "pomodoro focus card"),
                    Pair("ALARMS", "alarm clock card"),
                    Pair("ALIASES", "aliases & commands card"),
                    Pair("TIMERS", "quick timers card"),
                    Pair("SYSTEM_WIDGETS", "system app widgets")
                )

                SettingsGroupCard {
                    SettingsItemRow(
                        icon = Icons.Default.ViewAgenda,
                        title = AppStrings.settingsDeckCardsTitle,
                        subtitle = "${deckWidgetsOrder.size} interactive cards on Deck",
                        trailingContent = {
                            Icon(
                                imageVector = if (isDeckWidgetsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { isDeckWidgetsExpanded = !isDeckWidgetsExpanded }
                    )

                    AnimatedVisibility(
                        visible = isDeckWidgetsExpanded || q.contains("deck") || "deck".contains(q) || q.contains("widget") || "widget".contains(q),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MinimalDarkCardBg.copy(alpha = 0.5f))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            deckWidgetsOrder.forEachIndexed { index, deckKey ->
                                val baseLabel = allDeckWidgetOptions.find { it.first == deckKey }?.second ?: deckKey
                                val label = if (deckKey == "SYSTEM_WIDGETS") {
                                    if (deckSystemWidgets.isNotEmpty()) {
                                        "system app widgets (${deckSystemWidgets.size} active)"
                                    } else {
                                        "system app widgets"
                                    }
                                } else {
                                    baseLabel
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MinimalCardBg)
                                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MinimalWhite,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (index > 0) {
                                            IconButton(
                                                onClick = {
                                                    val mutable = deckWidgetsOrder.toMutableList()
                                                    val temp = mutable[index]
                                                    mutable[index] = mutable[index - 1]
                                                    mutable[index - 1] = temp
                                                    scope.launch { userPreferences.setDeckWidgetsOrder(mutable) }
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = MinimalRed, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        if (index < deckWidgetsOrder.size - 1) {
                                            IconButton(
                                                onClick = {
                                                    val mutable = deckWidgetsOrder.toMutableList()
                                                    val temp = mutable[index]
                                                    mutable[index] = mutable[index + 1]
                                                    mutable[index + 1] = temp
                                                    scope.launch { userPreferences.setDeckWidgetsOrder(mutable) }
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = MinimalRed, modifier = Modifier.size(15.dp))
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                val updated = deckWidgetsOrder - deckKey
                                                scope.launch { userPreferences.setDeckWidgetsOrder(updated) }
                                            },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = MinimalGray, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }

                            val unaddedDeck = allDeckWidgetOptions.filter { it.first !in deckWidgetsOrder }
                            if (unaddedDeck.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "TAP TO ADD CARD TO DECK:",
                                    fontFamily = AppleSanFranciscoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalGray,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    unaddedDeck.forEach { (key, name) ->
                                        val shortLabel = when (key) {
                                            "SYSTEM_WIDGETS" -> "system widgets"
                                            "ALARMS" -> "alarms"
                                            else -> name.split(" ").first()
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MinimalDarkCardBg)
                                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                                .clickable {
                                                    scope.launch { userPreferences.setDeckWidgetsOrder(deckWidgetsOrder + key) }
                                                }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "+ $shortLabel",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MinimalRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // =========================================================
            // CATEGORY 4: COMMANDS & SHORTCUTS
            // =========================================================
            val commandKeywords = listOf("command", "commands", "alias", "aliases", "feedback", "popup", "history", "terminal")
            if (q.isEmpty() || commandKeywords.any { it.contains(q) || q.contains(it) }) {
                SettingsSectionHeader(title = AppStrings.settingsSecCommand)
                Spacer(modifier = Modifier.height(8.dp))

                SettingsGroupCard {
                    // Item 0a: Show Command Bar on Home
                    SettingsItemRow(
                        icon = Icons.Default.Terminal,
                        title = AppStrings.settingsShowCommandBarTitle,
                        subtitle = AppStrings.settingsShowCommandBarDesc,
                        trailingContent = {
                            Switch(
                                checked = showCommandBar,
                                onCheckedChange = { scope.launch { userPreferences.setShowCommandBar(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MinimalWhite,
                                    checkedTrackColor = MinimalRed,
                                    uncheckedThumbColor = MinimalGray,
                                    uncheckedTrackColor = MinimalDarkCardBg
                                )
                            )
                        }
                    )

                    SettingsInsetDivider()

                    // Item 0b: Command Helper Icon
                    SettingsItemRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        title = AppStrings.settingsCommandHelperIconTitle,
                        subtitle = AppStrings.settingsCommandHelperIconDesc,
                        trailingContent = {
                            Switch(
                                checked = showCommandHelperIcon,
                                onCheckedChange = { scope.launch { userPreferences.setShowCommandHelperIcon(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MinimalWhite,
                                    checkedTrackColor = MinimalRed,
                                    uncheckedThumbColor = MinimalGray,
                                    uncheckedTrackColor = MinimalDarkCardBg
                                )
                            )
                        }
                    )

                    SettingsInsetDivider()

                    // Item 1: Custom Command Aliases
                    SettingsItemRow(
                        icon = Icons.Default.Terminal,
                        title = AppStrings.settingsAliasesTitle,
                        subtitle = if (customCommands.isEmpty()) "Tap to add triggers (yt, wifi, gh)" else "${customCommands.size} custom keywords active",
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MinimalRed)
                                        .clickable { showAddCommandModal = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "+ ADD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MinimalOnAccent)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (isAliasesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MinimalGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = { isAliasesExpanded = !isAliasesExpanded }
                    )

                    AnimatedVisibility(
                        visible = isAliasesExpanded || q.contains("alias") || q.contains("command"),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MinimalDarkCardBg.copy(alpha = 0.5f))
                                .padding(12.dp)
                        ) {
                            if (customCommands.isEmpty()) {
                                Text(
                                    text = "No custom aliases yet. Tap '+ ADD' to create shortcuts like 'yt' -> YouTube or 'wifi' -> Settings.",
                                    fontSize = 12.sp,
                                    color = MinimalGray,
                                    lineHeight = 16.sp
                                )
                            } else {
                                customCommands.forEach { (keyword, target) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = keyword,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MinimalRed,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "-> $target",
                                                fontSize = 11.sp,
                                                color = MinimalGray,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        IconButton(
                                            onClick = { scope.launch { userPreferences.removeCustomCommand(keyword) } },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = MinimalGray,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    SettingsInsetDivider()

                    // Item 2: Action Confirmation Feedback
                    SettingsItemRow(
                        icon = Icons.Default.NotificationsActive,
                        title = AppStrings.settingsFeedbackTitle,
                        subtitle = AppStrings.settingsFeedbackDesc,
                        trailingContent = {
                            Switch(
                                checked = showCommandFeedback,
                                onCheckedChange = { scope.launch { userPreferences.setShowCommandFeedback(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MinimalWhite,
                                    checkedTrackColor = MinimalRed,
                                    uncheckedThumbColor = MinimalGray,
                                    uncheckedTrackColor = MinimalDarkCardBg
                                )
                            )
                        }
                    )

                    SettingsInsetDivider()

                    // Item 3: Command Box History
                    SettingsItemRow(
                        icon = Icons.Default.History,
                        title = AppStrings.settingsHistoryTitle,
                        subtitle = if (commandHistoryEnabled) "Buffer and cycle buttons enabled in command bar" else "History buffer and cycle buttons disabled",
                        trailingContent = {
                            Switch(
                                checked = commandHistoryEnabled,
                                onCheckedChange = { scope.launch { userPreferences.setCommandHistoryEnabled(it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MinimalWhite,
                                    checkedTrackColor = MinimalRed,
                                    uncheckedThumbColor = MinimalGray,
                                    uncheckedTrackColor = MinimalDarkCardBg
                                )
                            )
                        }
                    )

                    SettingsInsetDivider()

                    // Item 4: Download History (.md)
                    SettingsItemRow(
                        icon = Icons.Default.Description,
                        title = AppStrings.settingsExportHistoryTitle,
                        subtitle = if (commandHistory.isEmpty()) "Buffer empty · No command history to export" else "${commandHistory.size} commands in buffer · Save as markdown file",
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (commandHistory.isEmpty()) MinimalDarkCardBg else MinimalRed)
                                    .clickable(enabled = commandHistory.isNotEmpty()) {
                                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                        exportHistoryLauncher.launch("minimal_command_history_$timestamp.md")
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "DOWNLOAD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (commandHistory.isEmpty()) MinimalGray else MinimalOnAccent
                                )
                            }
                        },
                        onClick = {
                            if (commandHistory.isNotEmpty()) {
                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                exportHistoryLauncher.launch("minimal_command_history_$timestamp.md")
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // =========================================================
            // CATEGORY 5: SYSTEM & ASSISTANT
            // =========================================================
            val systemKeywords = listOf("assistant", "assistants", "system", "launcher", "notification", "notifications", "clock", "alarm", "permission")
            if (q.isEmpty() || systemKeywords.any { it.contains(q) || q.contains(it) }) {
                SettingsSectionHeader(title = AppStrings.settingsSecSystem)
                Spacer(modifier = Modifier.height(8.dp))

                SettingsGroupCard {
                    // Item 1: Digital Assistant
                    val currentAssistantName = availableAssistants.find {
                        if (assistantPackage.isNullOrBlank() || assistantPackage == "SYSTEM_DEFAULT") {
                            it.first == "SYSTEM_DEFAULT"
                        } else {
                            it.first == assistantPackage
                        }
                    }?.second ?: "System Default Assistant"

                    SettingsItemRow(
                        icon = Icons.Default.AutoAwesome,
                        title = AppStrings.settingsAssistantTitle,
                        subtitle = currentAssistantName,
                        trailingContent = {
                            Icon(
                                imageVector = if (isAssistantExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { isAssistantExpanded = !isAssistantExpanded }
                    )

                    AnimatedVisibility(
                        visible = isAssistantExpanded || q.contains("assistant"),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MinimalDarkCardBg.copy(alpha = 0.5f))
                        ) {
                            availableAssistants.forEachIndexed { index, (id, name, tag) ->
                                val isSelected = if (assistantPackage.isNullOrBlank() || assistantPackage == "SYSTEM_DEFAULT") {
                                    id == "SYSTEM_DEFAULT"
                                } else {
                                    id == assistantPackage
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { scope.launch { userPreferences.setAssistantPackage(id) } }
                                        .padding(horizontal = 14.dp, vertical = 11.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = name,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (isSelected) MinimalWhite else MinimalGray
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(MinimalDarkCardBg)
                                                    .border(0.5.dp, if (tag == "OS DEFAULT") MinimalRed else MinimalDarkGray, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = tag,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (tag == "OS DEFAULT") MinimalRed else MinimalGray
                                                )
                                            }
                                        }
                                    }
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(MinimalRed),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MinimalOnAccent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                                if (index < availableAssistants.size - 1) {
                                    HorizontalDivider(color = MinimalDarkGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                                }
                            }
                        }
                    }

                    SettingsInsetDivider()

                    // Item 2: Set as Default Launcher
                    SettingsItemRow(
                        icon = Icons.Default.Home,
                        title = AppStrings.settingsLauncherTitle,
                        subtitle = "Configure Minimal OS as your primary home",
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )

                    SettingsInsetDivider()

                    // Item 3: Notification Hub Access
                    SettingsItemRow(
                        icon = Icons.Default.Notifications,
                        title = AppStrings.settingsHubAccessTitle,
                        subtitle = "Grant permissions for unified notifications",
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            context.startActivity(intent)
                        }
                    )

                    SettingsInsetDivider()

                    // Item 4: Save Notification History Toggle
                    SettingsItemRow(
                        icon = Icons.Default.History,
                        title = AppStrings.settingsSaveHistoryTitle,
                        subtitle = if (notificationHistoryEnabled) "Archive incoming alerts locally for Hub and AI export" else "Notification archiving is disabled",
                        trailingContent = {
                            Switch(
                                checked = notificationHistoryEnabled,
                                onCheckedChange = { isChecked ->
                                    scope.launch {
                                        userPreferences.setNotificationHistoryEnabled(isChecked)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MinimalOnAccent,
                                    checkedTrackColor = MinimalRed,
                                    uncheckedThumbColor = MinimalGray,
                                    uncheckedTrackColor = MinimalDarkCardBg
                                )
                            )
                        },
                        onClick = {
                            scope.launch {
                                userPreferences.setNotificationHistoryEnabled(!notificationHistoryEnabled)
                            }
                        }
                    )

                    SettingsInsetDivider()

                    // Item 5: Download Notification History (.txt)
                    SettingsItemRow(
                        icon = Icons.Default.Download,
                        title = AppStrings.settingsDownloadHistoryTitle,
                        subtitle = "Export stored alerts directly to your Downloads folder",
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = MinimalRed,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            val items = com.zaryif.minimalos.data.repository.NotificationHistoryRepository.getInstance(context).history.value
                            com.zaryif.minimalos.util.NotificationExporter.downloadToDevice(context, items)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // =========================================================
            // CATEGORY 6: ABOUT & SYSTEM INFO
            // =========================================================
            val aboutKeywords = listOf("about", "version", "release", "developer", "author", "zarif", "coffee", "support", "onboarding", "guide", "docs", "license")
            if (q.isEmpty() || aboutKeywords.any { it.contains(q) || q.contains(it) }) {
                SettingsSectionHeader(title = AppStrings.settingsSecAbout)
                Spacer(modifier = Modifier.height(8.dp))

                SettingsGroupCard {
                    // 1. App Info & Version (Click to open full modal)
                    SettingsItemRow(
                        icon = Icons.Default.Info,
                        title = AppStrings.settingsAboutLauncherTitle,
                        subtitle = "v$appVersionName (Build $appVersionCode) · ${AppStrings.settingsAboutLauncherSubtitle}",
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalRed, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "STABLE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MinimalRed)
                            }
                        },
                        onClick = { showAboutModal = true }
                    )

                    SettingsInsetDivider()

                    // 1b. Complete Command Reference & Guide (Click to check it out)
                    SettingsItemRow(
                        icon = Icons.Default.Terminal,
                        title = AppStrings.settingsCommandGuideTitle,
                        subtitle = AppStrings.settingsCommandGuideSubtitle,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalRed)
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(text = "CHECK IT OUT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MinimalOnAccent)
                            }
                        },
                        onClick = { showCommandGuideModal = true }
                    )

                    SettingsInsetDivider()

                    // 2. Developer & Creator
                    SettingsItemRow(
                        icon = Icons.Default.Person,
                        title = AppStrings.settingsDeveloperTitle,
                        subtitle = AppStrings.settingsDeveloperSubtitle,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zaryif"))
                            context.startActivity(browserIntent)
                        }
                    )

                    SettingsInsetDivider()

                    // 3. Support & Fund Project (Buy Me a Coffee)
                    SettingsItemRow(
                        icon = Icons.Default.Favorite,
                        title = AppStrings.settingsSupportTitle,
                        subtitle = AppStrings.settingsSupportSubtitle,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalRed)
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(text = "SUPPORT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MinimalOnAccent)
                            }
                        },
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/zaryif"))
                            context.startActivity(browserIntent)
                        }
                    )

                    SettingsInsetDivider()

                    // 4. App Walkthrough & Guide (Onboarding)
                    SettingsItemRow(
                        icon = Icons.Default.AutoAwesome,
                        title = AppStrings.settingsWalkthroughTitle,
                        subtitle = AppStrings.settingsWalkthroughSubtitle,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalRed, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(text = "REPLAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MinimalRed)
                            }
                        },
                        onClick = { showOnboardingModal = true }
                    )

                    SettingsInsetDivider()

                    // 5. Reset Onboarding Experience
                    SettingsItemRow(
                        icon = Icons.Default.Clear,
                        title = AppStrings.settingsResetOnboardingTitle,
                        subtitle = AppStrings.settingsResetOnboardingSubtitle,
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(text = "RESET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MinimalRed)
                            }
                        },
                        onClick = {
                            scope.launch {
                                userPreferences.setOnboardingCompleted(false)
                            }
                            onNavigateToOnboarding()
                        }
                    )

                    SettingsInsetDivider()

                    // 5. GitHub Releases & Source Code
                    SettingsItemRow(
                        icon = Icons.Default.Code,
                        title = AppStrings.settingsGithubTitle,
                        subtitle = AppStrings.settingsGithubSubtitle,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zaryif/Minimal_os/releases"))
                            context.startActivity(browserIntent)
                        }
                    )

                    SettingsInsetDivider()

                    // 6. Architecture & Documentation
                    SettingsItemRow(
                        icon = Icons.Default.Description,
                        title = "Architecture & Documentation",
                        subtitle = "BRD, SRS, SAD, MIS & Master Plan on GitHub",
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zaryif/Minimal_os#readme"))
                            context.startActivity(browserIntent)
                        }
                    )

                    SettingsInsetDivider()

                    // 7. Open Source License
                    SettingsItemRow(
                        icon = Icons.Default.Check,
                        title = "Open Source License",
                        subtitle = "MIT License · 100% Free, Private & Open Forever",
                        trailingContent = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "MIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MinimalGray)
                            }
                        },
                        onClick = {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zaryif/Minimal_os/blob/main/LICENSE"))
                            context.startActivity(browserIntent)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // =========================================================
        // SEARCHABLE APP PICKER DIALOG (FOR SHORTCUTS)
        // =========================================================
        if (showAppPickerModal) {
            var appPickerSearchQuery by remember { mutableStateOf("") }
            val filteredInstalledApps = remember(appPickerSearchQuery, installedApps) {
                if (appPickerSearchQuery.isBlank()) {
                    installedApps
                } else {
                    val searchFilter = appPickerSearchQuery.lowercase()
                    installedApps.filter { it.label.lowercase().contains(searchFilter) }
                }
            }

            Dialog(onDismissRequest = { showAppPickerModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(520.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MinimalCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "HOME SHORTCUTS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalWhite
                                )
                                Text(
                                    text = "${favoriteAppPackages.size} apps selected",
                                    fontSize = 11.sp,
                                    color = MinimalRed
                                )
                            }
                            IconButton(onClick = { showAppPickerModal = false }, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MinimalGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // App Picker Search Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MinimalGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (appPickerSearchQuery.isEmpty()) {
                                        Text(text = "Search apps to pin…", fontSize = 13.sp, color = MinimalGray)
                                    }
                                    BasicTextField(
                                        value = appPickerSearchQuery,
                                        onValueChange = { appPickerSearchQuery = it },
                                        singleLine = true,
                                        cursorBrush = SolidColor(MinimalRed),
                                        textStyle = TextStyle(fontSize = 13.sp, color = MinimalWhite),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (appPickerSearchQuery.isNotEmpty()) {
                                    IconButton(onClick = { appPickerSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = MinimalGray, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredInstalledApps, key = { it.packageName }) { app ->
                                val isFav = app.packageName in favoriteAppPackages
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isFav) MinimalDarkCardBg else MinimalCardBg)
                                        .clickable {
                                            val updated = if (isFav) {
                                                favoriteAppPackages - app.packageName
                                            } else {
                                                favoriteAppPackages + app.packageName
                                            }
                                            scope.launch { userPreferences.setFavoriteApps(updated) }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isFav) MinimalRed else MinimalDarkCardBg)
                                            .border(1.dp, if (isFav) MinimalRed else MinimalDarkGray, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isFav) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MinimalOnAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = app.label,
                                        fontSize = 14.sp,
                                        color = MinimalWhite,
                                        fontWeight = if (isFav) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MinimalRed)
                                .clickable { showAppPickerModal = false }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DONE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalOnAccent
                            )
                        }
                    }
                }
            }
        }

        // =========================================================
        // ADD CUSTOM COMMAND ALIAS DIALOG
        // =========================================================
        if (showAddCommandModal) {
            var newKeyword by remember { mutableStateOf("") }
            var newTarget by remember { mutableStateOf("") }

            Dialog(onDismissRequest = { showAddCommandModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MinimalBlack)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(18.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEW COMMAND ALIAS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                            IconButton(onClick = { showAddCommandModal = false }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MinimalGray, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "KEYWORD / TRIGGER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MinimalRed)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            if (newKeyword.isEmpty()) {
                                Text(text = "e.g. yt, gh, wifi, rd", fontSize = 13.sp, color = MinimalGray)
                            }
                            BasicTextField(
                                value = newKeyword,
                                onValueChange = { newKeyword = it },
                                singleLine = true,
                                cursorBrush = SolidColor(MinimalRed),
                                textStyle = TextStyle(fontSize = 13.sp, color = MinimalWhite, fontFamily = FontFamily.Monospace),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "ACTION / URL / SETTING / APP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MinimalRed)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            if (newTarget.isEmpty()) {
                                Text(text = "e.g. https://youtube.com", fontSize = 13.sp, color = MinimalGray)
                            }
                            BasicTextField(
                                value = newTarget,
                                onValueChange = { newTarget = it },
                                singleLine = true,
                                cursorBrush = SolidColor(MinimalRed),
                                textStyle = TextStyle(fontSize = 13.sp, color = MinimalWhite, fontFamily = FontFamily.Monospace),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = "QUICK TEMPLATES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MinimalGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val templates = listOf(
                                Pair("yt", "https://youtube.com"),
                                Pair("gh", "https://github.com"),
                                Pair("wifi", "android.settings.WIFI_SETTINGS"),
                                Pair("rd", "https://reddit.com")
                            )
                            templates.forEach { (kw, target) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MinimalDarkCardBg)
                                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                        .clickable {
                                            newKeyword = kw
                                            newTarget = target
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = kw, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MinimalRed)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (newKeyword.isNotBlank() && newTarget.isNotBlank()) MinimalRed else MinimalDarkGray)
                                .clickable(enabled = newKeyword.isNotBlank() && newTarget.isNotBlank()) {
                                    scope.launch {
                                        userPreferences.setCustomCommand(newKeyword.trim(), newTarget.trim())
                                        showAddCommandModal = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "SAVE COMMAND", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (newKeyword.isNotBlank() && newTarget.isNotBlank()) MinimalOnAccent else MinimalWhite)
                        }
                    }
                }
            }
        }

        // =========================================================
        // ABOUT MINIMAL OS & SYSTEM ARCHITECTURE MODAL
        // =========================================================
        if (showAboutModal) {
            Dialog(onDismissRequest = { showAboutModal = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MinimalCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header: Monogram + Title + Version + Dismiss
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "Minimal OS Logo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MINIMAL OS",
                                    fontFamily = AppleSanFranciscoFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalWhite
                                )
                                Text(
                                    text = "v$appVersionName (Build $appVersionCode) · OFFICIAL",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MinimalRed
                                )
                            }

                            IconButton(
                                onClick = { showAboutModal = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MinimalGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Manifesto / Tagline
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MinimalDarkCardBg)
                                .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "An intentional, text-first, terminal-inspired Android launcher engineered to reclaim your attention and eliminate digital dopamine loops.",
                                fontSize = 12.sp,
                                color = MinimalWhite,
                                lineHeight = 17.sp,
                                fontFamily = AppleSanFranciscoFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Developer Attribution Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MinimalDarkCardBg)
                                .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zaryif"))
                                    context.startActivity(intent)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MinimalRed.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MinimalRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MD. Zarif Azfar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalWhite
                                )
                                Text(
                                    text = "Creator & Maintainer · @zaryif · Dhaka, BD",
                                    fontSize = 10.sp,
                                    color = MinimalGray
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MinimalGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Key Architecture & System Pillars
                        Text(
                            text = "CORE SYSTEM PILLARS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val pillars = listOf(
                            Pair("Unified Command Bar", "8 instant symbol triggers (@, #, -, !, *, +, :, ?) & custom alias routing."),
                            Pair("Minimal Memo & Tasks", "Real-time search, quick-add memo bar, and zero-distraction home list."),
                            Pair("Live Widgets Deck", "Live Alarm Countdown, Focus Countdown, Pomodoro & Quick Aliases."),
                            Pair("Adaptive Luminance Engine", "Dynamic MinimalOnAccent WCAG AAA contrast across all light & dark themes."),
                            Pair("100% Private & Local-First", "Zero telemetry, offline-first, no tracking, ads, or data harvesting.")
                        )

                        pillars.forEach { (title, desc) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MinimalWhite
                                )
                                Text(
                                    text = desc,
                                    fontSize = 10.sp,
                                    color = MinimalGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Technical Specifications
                        Text(
                            text = "TECHNICAL SPECIFICATIONS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MinimalDarkCardBg)
                                .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "• Language: Kotlin 2.0+ (Coroutines & Flows)\n• UI Framework: Jetpack Compose Material3\n• Persistence: Jetpack DataStore Preferences\n• Compatibility: Android 8.0 (API 26) to 15 (API 35)\n• License: MIT Open Source",
                                fontSize = 10.sp,
                                color = MinimalGray,
                                lineHeight = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fund Project (Buy Me a Coffee)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MinimalRed)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/zaryif"))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FUND THE PROJECT (BUY ME A COFFEE)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalOnAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // GitHub Link
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/zaryif/Minimal_os"))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "VIEW SOURCE ON GITHUB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dismiss Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MinimalBlack)
                                .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                                .clickable { showAboutModal = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DISMISS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalGray
                            )
                        }
                    }
                }
            }
        }

        // =========================================================
        // APP WALKTHROUGH & ONBOARDING FULLSCREEN MODAL
        // =========================================================
        if (showOnboardingModal) {
            Dialog(
                onDismissRequest = { showOnboardingModal = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MinimalBlack)
                ) {
                    OnboardingScreen(
                        userPreferences = userPreferences,
                        onFinish = {
                            scope.launch {
                                userPreferences.setOnboardingCompleted(true)
                            }
                            showOnboardingModal = false
                        }
                    )

                    // Top Close Button
                    IconButton(
                        onClick = { showOnboardingModal = false },
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MinimalDarkCardBg.copy(alpha = 0.85f))
                            .border(1.dp, MinimalDarkGray, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Walkthrough",
                            tint = MinimalWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (showCommandGuideModal) {
            CommandHelpDialog(
                onDismiss = { showCommandGuideModal = false },
                onSelectExample = { showCommandGuideModal = false }
            )
        }
    }
}

// =========================================================================
// TOP-BRAND DESIGN SYSTEM COMPONENTS
// =========================================================================

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = AppleSanFranciscoFontFamily,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MinimalRed,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lead Icon Container (Top brand standard)
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MinimalDarkCardBg)
                .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MinimalRed,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title & Dynamic Subtitle / State
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = AppleSanFranciscoFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MinimalWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = AppleSanFranciscoFontFamily,
                fontSize = 11.sp,
                color = MinimalGray,
                lineHeight = 15.sp
            )
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
private fun SettingsInsetDivider() {
    HorizontalDivider(
        color = MinimalDarkGray.copy(alpha = 0.4f),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 58.dp)
    )
}
