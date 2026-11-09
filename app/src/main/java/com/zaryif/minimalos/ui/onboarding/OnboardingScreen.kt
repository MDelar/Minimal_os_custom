package com.zaryif.minimalos.ui.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.zaryif.minimalos.service.MinimalNotificationService
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.R
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.ui.components.ButtonStyle
import com.zaryif.minimalos.ui.components.CommandHelpDialog
import com.zaryif.minimalos.ui.components.MinimalButton
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalOnAccent
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalThemeRegistry
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.ui.theme.currentActiveTheme
import com.zaryif.minimalos.util.AlarmClockPermissionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    userPreferences: UserPreferences? = null,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val prefs = userPreferences ?: remember { UserPreferences(context) }
    var currentStep by remember { mutableIntStateOf(0) }
    var showHelpDialog by remember { mutableStateOf(false) }
    val totalSteps = 5

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Navigation & Step Indicator Header (Zero-scroll, Apple/Google style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { currentStep-- }
                            .padding(vertical = 4.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MinimalGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BACK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalGray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Sleek Step Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalSteps) {
                        Box(
                            modifier = Modifier
                                .size(if (i == currentStep) 8.dp else 5.dp)
                                .clip(CircleShape)
                                .background(if (i == currentStep) MinimalRed else MinimalDarkGray)
                        )
                    }
                }

                // Step Counter Text
                Text(
                    text = "${currentStep + 1}/$totalSteps",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MinimalGray
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                        } else {
                            slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                        }
                    },
                    label = "OnboardingContent"
                ) { step ->
                    when (step) {
                        0 -> StepWelcome(onNext = { currentStep = 1 })
                        1 -> StepCommandBarAnimated(
                            onNext = { currentStep = 2 },
                            onOpenGuide = { showHelpDialog = true }
                        )
                        2 -> StepWidgetsDeck(onNext = { currentStep = 3 })
                        3 -> StepNotificationHub(onNext = { currentStep = 4 })
                        4 -> StepSetupWizard(
                            userPreferences = prefs,
                            onFinish = onFinish,
                            onOpenGuide = { showHelpDialog = true }
                        )
                    }
                }
            }
        }

        if (showHelpDialog) {
            CommandHelpDialog(
                onDismiss = { showHelpDialog = false },
                onSelectExample = { showHelpDialog = false }
            )
        }
    }
}

// -------------------------------------------------------------
// STEP 1: Welcome & Philosophy (100% Viewport, Zero Scrolling)
// -------------------------------------------------------------
@Composable
private fun StepWelcome(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(10.dp))
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Minimal OS Logo",
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "welcome to\nminimal os",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalWhite,
                lineHeight = 38.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "an intentional, text-first home launcher designed to reclaim your attention and time.",
                fontSize = 13.sp,
                color = MinimalGray,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 3 Apple/Google-style clean feature highlights
            CleanFeatureRow(
                icon = Icons.Default.Terminal,
                title = "Integrated Command Line",
                subtitle = "Natural typing for tasks, notes, alarms, calls, and web queries in seconds."
            )
            Spacer(modifier = Modifier.height(12.dp))

            CleanFeatureRow(
                icon = Icons.Default.Widgets,
                title = "Live Cards & Widgets Deck",
                subtitle = "High-contrast countdown timers, scratchpad memos, and modular deck cards."
            )
            Spacer(modifier = Modifier.height(12.dp))

            CleanFeatureRow(
                icon = Icons.Default.Security,
                title = "100% Offline & Zero Telemetry",
                subtitle = "No tracking, no cloud accounts, no ads, and strictly zero notification anxiety."
            )
        }

        Column {
            MinimalButton(
                text = "GET STARTED ->",
                onClick = onNext,
                style = ButtonStyle.PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 2: The Command Bar with Live Typewriter Animation
// -------------------------------------------------------------
private data class DemoCommand(
    val triggerText: String,
    val symbol: String,
    val pillLabel: String,
    val categoryBadge: String,
    val resultTitle: String,
    val resultSubtitle: String,
    val icon: ImageVector
)

@Composable
private fun StepCommandBarAnimated(
    onNext: () -> Unit,
    onOpenGuide: () -> Unit
) {
    val demoList = remember {
        listOf(
            DemoCommand(
                triggerText = "@Zaryif On my way",
                symbol = "@",
                pillLabel = "@Zaryif",
                categoryBadge = "@ MESSAGE / SMS",
                resultTitle = "Message to Zaryif",
                resultSubtitle = "\"On my way\"",
                icon = Icons.AutoMirrored.Filled.Chat
            ),
            DemoCommand(
                triggerText = "- Finish my project",
                symbol = "-",
                pillLabel = "- Project",
                categoryBadge = "- TASK",
                resultTitle = "Task Added to Home",
                resultSubtitle = "Finish my project",
                icon = Icons.Default.CheckCircle
            ),
            DemoCommand(
                triggerText = "! Life is always good when you have Minimal OS as your default",
                symbol = "!",
                pillLabel = "! Note",
                categoryBadge = "! QUICK NOTE",
                resultTitle = "Note Saved to Scratchpad",
                resultSubtitle = "\"Life is always good when you have Minimal OS as your default\"",
                icon = Icons.Default.EditNote
            ),
            DemoCommand(
                triggerText = "* Team meeting at 3pm",
                symbol = "*",
                pillLabel = "* Meeting",
                categoryBadge = "* CALENDAR EVENT",
                resultTitle = "Calendar Event Scheduled",
                resultSubtitle = "Team meeting · Today at 3:00 PM",
                icon = Icons.Default.CalendarToday
            ),
            DemoCommand(
                triggerText = "#Zaryif",
                symbol = "#",
                pillLabel = "# Call",
                categoryBadge = "# DIRECT CALL",
                resultTitle = "Calling Zaryif",
                resultSubtitle = "Connecting via phone dialer...",
                icon = Icons.Default.Phone
            )
        )
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var typedText by remember { mutableStateOf("") }
    var showResultCard by remember { mutableStateOf(false) }
    var cursorVisible by remember { mutableStateOf(true) }

    // Blinking cursor loop
    LaunchedEffect(Unit) {
        while (true) {
            cursorVisible = !cursorVisible
            delay(500)
        }
    }

    // Typewriter animation cycle
    LaunchedEffect(selectedIndex) {
        showResultCard = false
        typedText = ""
        val fullText = demoList[selectedIndex].triggerText

        // Type letter by letter
        for (i in fullText.indices) {
            typedText = fullText.substring(0, i + 1)
            delay(40)
        }
        delay(350)
        showResultCard = true

        // Hold result card for reading, then advance
        delay(3200)
        selectedIndex = (selectedIndex + 1) % demoList.size
    }

    val currentDemo = demoList[selectedIndex]

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "the command bar",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "control your phone with a fast unified command line. type a symbol or natural phrase for instant action.",
                fontSize = 13.sp,
                color = MinimalGray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Tap-to-Try Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                demoList.forEachIndexed { index, demo ->
                    val isSelected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MinimalRed else MinimalDarkCardBg)
                            .border(
                                1.dp,
                                if (isSelected) MinimalRed else MinimalDarkGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedIndex = index }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = demo.pillLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MinimalOnAccent else MinimalWhite,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Simulated Live Terminal Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TERMINAL INPUT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MinimalCardBg)
                                .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LIVE DEMO",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalGray,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "> ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = typedText,
                            fontSize = 14.sp,
                            color = MinimalWhite,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (cursorVisible) {
                            Text(
                                text = "|",
                                fontSize = 15.sp,
                                color = MinimalRed,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated Live Result Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                verticalArrangement = Arrangement.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showResultCard,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { -it / 2 }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MinimalCardBg)
                            .border(1.dp, MinimalRed, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = currentDemo.icon,
                                    contentDescription = null,
                                    tint = MinimalRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentDemo.categoryBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalRed,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = currentDemo.resultTitle,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalWhite
                                )
                                Text(
                                    text = currentDemo.resultSubtitle,
                                    fontSize = 11.sp,
                                    color = MinimalGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Check Details / Command Guide button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenGuide() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MinimalRed,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CHECK COMMAND GUIDE & SHORTCUTS ->",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalRed,
                    letterSpacing = 0.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Column {
            Text(
                text = "Full guide is always accessible under Settings > Command Reference.",
                fontSize = 10.sp,
                color = MinimalGray,
                lineHeight = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            MinimalButton(
                text = "NEXT: WIDGETS DECK ->",
                onClick = onNext,
                style = ButtonStyle.PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 3: Widgets & Deck (100% Viewport, Zero Scrolling)
// -------------------------------------------------------------
@Composable
private fun StepWidgetsDeck(onNext: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "cards & deck",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "swipe right from home for live countdowns, focus tools, memos, and native widgets.",
                fontSize = 13.sp,
                color = MinimalGray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Compact Hero Visuals
            HeroCardPreview(
                icon = Icons.Default.HourglassBottom,
                tag = "LIVE POMODORO COUNTDOWN",
                title = "08:34",
                subtitle = "10m focus · 5m rest session active on home screen",
                accent = MinimalRed
            )
            Spacer(modifier = Modifier.height(10.dp))

            HeroCardPreview(
                icon = Icons.Default.CheckCircle,
                tag = "ACTIVE TASK",
                title = "Finish my project",
                subtitle = "Pinned priority task with instant strike-through",
                accent = MinimalWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            HeroCardPreview(
                icon = Icons.Default.CalendarToday,
                tag = "CALENDAR AGENDA",
                title = "Team sync · 4:00 PM",
                subtitle = "Natural scheduling via '*' command or calendar view",
                accent = MinimalWhite
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Swipe Right from Home or tap 'widgets ->' anytime.",
                        fontSize = 11.sp,
                        color = MinimalGray
                    )
                }
            }
        }

        Column {
            MinimalButton(
                text = "NEXT: NOTIFICATION HUB ->",
                onClick = onNext,
                style = ButtonStyle.PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 4: Notification Hub (100% Viewport, Zero Scrolling)
// -------------------------------------------------------------
@Composable
private fun StepNotificationHub(onNext: () -> Unit) {
    val tabs = listOf("ALL", "MESSAGE", "CALLS", "EMAIL")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "notification hub",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "messages, calls, and emails in one calm stream, eliminating notification anxiety.",
                fontSize = 13.sp,
                color = MinimalGray,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabs.forEachIndexed { index, tab ->
                    Text(
                        text = tab,
                        fontSize = 10.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == 0) MinimalRed else MinimalGray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sample notification cards
            NotificationSampleRow(
                sender = "Zaryif",
                preview = "On my way now",
                time = "2m",
                icon = Icons.AutoMirrored.Filled.Chat
            )
            Spacer(modifier = Modifier.height(8.dp))

            NotificationSampleRow(
                sender = "Engineering Team",
                preview = "Meeting event: Project launch update",
                time = "15m",
                icon = Icons.Default.CalendarToday
            )
            Spacer(modifier = Modifier.height(8.dp))

            NotificationSampleRow(
                sender = "Zaryif",
                preview = "Missed call",
                time = "30m",
                icon = Icons.Default.Phone
            )

            val context = LocalContext.current
            val isNotifGranted = remember { MinimalNotificationService.isPermissionGranted(context) }
            if (!isNotifGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MinimalCardBg)
                        .border(1.dp, MinimalRed, RoundedCornerShape(8.dp))
                        .clickable { launchNotificationAccess(context) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grant Notification Access",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalWhite
                        )
                    }
                    Text(
                        text = "GRANT ->",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Swipe Left from Home or tap '<- hub' anytime.",
                        fontSize = 11.sp,
                        color = MinimalGray
                    )
                }
            }
        }

        Column {
            MinimalButton(
                text = "NEXT: SETUP WIZARD ->",
                onClick = onNext,
                style = ButtonStyle.PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// -------------------------------------------------------------
// STEP 5: Interactive Setup Wizard (100% Viewport, Zero Scrolling)
// -------------------------------------------------------------
@Composable
private fun StepSetupWizard(
    userPreferences: UserPreferences,
    onFinish: () -> Unit,
    onOpenGuide: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedThemeId by remember { mutableStateOf("AMOLED_RED") }
    var enablePomodoroWidget by remember { mutableStateOf(true) }
    var enableTasksWidget by remember { mutableStateOf(true) }
    var enableNotesWidget by remember { mutableStateOf(false) }
    var enableAlarmCountdown by remember { mutableStateOf(false) }
    var enableCommandHistory by remember { mutableStateOf(true) }
    var selectedAssistant by remember { mutableStateOf("SYSTEM_DEFAULT") }

    val assistantPills = listOf(
        Pair("SYSTEM_DEFAULT", "Default"),
        Pair("com.google.android.apps.bard", "Gemini"),
        Pair("com.openai.chatgpt", "ChatGPT"),
        Pair("com.anthropic.claude", "Claude")
    )

    fun applyAndLaunch() {
        scope.launch {
            userPreferences.setThemeId(selectedThemeId)

            val widgets = mutableListOf<String>()
            if (enableAlarmCountdown) widgets.add("ALARM_COUNTDOWN")
            if (enablePomodoroWidget) widgets.add("POMODORO")
            if (enableTasksWidget) widgets.add("TODOS")
            if (enableNotesWidget) widgets.add("NOTES")
            widgets.add("CALENDAR")
            widgets.add("SHORTCUTS")
            userPreferences.setHomeWidgets(widgets.distinct())

            userPreferences.setCommandHistoryEnabled(enableCommandHistory)

            if (selectedAssistant == "SYSTEM_DEFAULT") {
                userPreferences.setAssistantPackage(null)
            } else {
                userPreferences.setAssistantPackage(selectedAssistant)
            }

            userPreferences.setOnboardingCompleted(true)

            if (!isDefaultLauncher(context)) {
                launchSetDefaultHome(context)
            }

            onFinish()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "your setup",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "set as default, grant permissions, and choose your palette.",
                fontSize = 11.sp,
                color = MinimalGray,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 1. System Perfection: Default Launcher & Hub Permissions
            Text(
                text = "1. SYSTEM PERFECTION & DEFAULT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalRed,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isDefault = isDefaultLauncher(context)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDefault) MinimalCardBg else MinimalDarkCardBg)
                        .border(
                            1.dp,
                            if (isDefault) MinimalRed else MinimalDarkGray,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { launchSetDefaultHome(context) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Default Launcher",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                            if (isDefault) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MinimalRed,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isDefault) "active default" else "tap to set default",
                            fontSize = 9.sp,
                            color = if (isDefault) MinimalRed else MinimalGray
                        )
                    }
                }

                val hasNotif = MinimalNotificationService.isPermissionGranted(context)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hasNotif) MinimalCardBg else MinimalDarkCardBg)
                        .border(
                            1.dp,
                            if (hasNotif) MinimalRed else MinimalDarkGray,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { launchNotificationAccess(context) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notification Hub",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                            if (hasNotif) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MinimalRed,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = if (hasNotif) "access granted" else "tap to grant access",
                            fontSize = 9.sp,
                            color = if (hasNotif) MinimalRed else MinimalGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Theme Palette Selector (Compact Horizontal)
            Text(
                text = "2. THEME PALETTE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalRed,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MinimalThemeRegistry.ORIGINAL_THEMES.forEach { theme ->
                    val isSelected = selectedThemeId.equals(theme.id, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MinimalCardBg else MinimalDarkCardBg)
                            .border(
                                1.dp,
                                if (isSelected) MinimalRed else MinimalDarkGray,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                selectedThemeId = theme.id
                                currentActiveTheme = theme
                            }
                            .padding(vertical = 7.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(theme.accent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = theme.name.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MinimalWhite,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Home Widgets (2x2 Compact Grid)
            Text(
                text = "3. HOME SCREEN CARDS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalRed,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactWidgetChip(
                    label = "Tasks",
                    active = enableTasksWidget,
                    modifier = Modifier.weight(1f),
                    onToggle = { enableTasksWidget = !enableTasksWidget }
                )
                CompactWidgetChip(
                    label = "Pomodoro",
                    active = enablePomodoroWidget,
                    modifier = Modifier.weight(1f),
                    onToggle = { enablePomodoroWidget = !enablePomodoroWidget }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactWidgetChip(
                    label = "Recent Notes",
                    active = enableNotesWidget,
                    modifier = Modifier.weight(1f),
                    onToggle = { enableNotesWidget = !enableNotesWidget }
                )
                CompactWidgetChip(
                    label = "Alarm Timer",
                    active = enableAlarmCountdown,
                    modifier = Modifier.weight(1f),
                    onToggle = {
                        enableAlarmCountdown = !enableAlarmCountdown
                        if (enableAlarmCountdown && !AlarmClockPermissionHelper.canScheduleExactAlarms(context)) {
                            AlarmClockPermissionHelper.requestExactAlarmPermission(context)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Command History & Assistant
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                    .clickable { enableCommandHistory = !enableCommandHistory }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Save Terminal Command History",
                    fontSize = 11.sp,
                    color = MinimalWhite
                )
                Switch(
                    checked = enableCommandHistory,
                    onCheckedChange = { enableCommandHistory = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MinimalOnAccent,
                        checkedTrackColor = MinimalRed,
                        uncheckedThumbColor = MinimalGray,
                        uncheckedTrackColor = MinimalDarkCardBg
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Digital Assistant Pills
            Text(
                text = "PREFERRED AI ASSISTANT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalRed,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                assistantPills.forEach { (pkg, label) ->
                    val isSelected = selectedAssistant == pkg
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MinimalCardBg else MinimalDarkCardBg)
                            .border(
                                1.dp,
                                if (isSelected) MinimalRed else MinimalDarkGray,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedAssistant = pkg }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MinimalRed else MinimalWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Check Details / Command Guide Link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onOpenGuide() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MinimalRed,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CHECK IT OUT: COMPLETE COMMAND & FEATURE GUIDE ->",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalRed,
                    letterSpacing = 0.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Column {
            MinimalButton(
                text = "LAUNCH MINIMAL OS ->",
                onClick = { applyAndLaunch() },
                style = ButtonStyle.PRIMARY
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// -------------------------------------------------------------
// HELPER COMPONENTS (Apple / Google Clean Viewport Design)
// -------------------------------------------------------------
@Composable
private fun CleanFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MinimalDarkCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MinimalCardBg)
                .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MinimalRed,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MinimalWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MinimalGray,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun HeroCardPreview(
    icon: ImageVector,
    tag: String,
    title: String,
    subtitle: String,
    accent: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MinimalDarkCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MinimalCardBg)
                .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalRed,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MinimalGray
                )
            }
        }
    }
}

@Composable
private fun NotificationSampleRow(
    sender: String,
    preview: String,
    time: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MinimalDarkCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MinimalRed,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = sender,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalWhite
                )
                Text(
                    text = preview,
                    fontSize = 11.sp,
                    color = MinimalGray
                )
            }
        }
        Text(
            text = time,
            fontSize = 10.sp,
            color = MinimalGray,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun CompactWidgetChip(
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) MinimalCardBg else MinimalDarkCardBg)
            .border(
                1.dp,
                if (active) MinimalRed else MinimalDarkGray,
                RoundedCornerShape(8.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = MinimalWhite
            )
            if (active) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MinimalRed,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun isDefaultLauncher(context: Context): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                intent,
                android.content.pm.PackageManager.ResolveInfoFlags.of(android.content.pm.PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            context.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        }
        resolveInfo?.activityInfo?.packageName == context.packageName
    } catch (_: Exception) {
        false
    }
}

private fun launchSetDefaultHome(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}

private fun launchNotificationAccess(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
