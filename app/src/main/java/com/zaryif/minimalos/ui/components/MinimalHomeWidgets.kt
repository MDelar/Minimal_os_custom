package com.zaryif.minimalos.ui.components

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.domain.model.AppInfo
import com.zaryif.minimalos.domain.model.Note
import com.zaryif.minimalos.domain.model.Todo
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.ui.theme.MinimalOnAccent
import com.zaryif.minimalos.ui.theme.AppleSanFranciscoFontFamily
import com.zaryif.minimalos.domain.timer.PomodoroTimerManager
import android.app.AlarmManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.zaryif.minimalos.util.AlarmClockPermissionHelper
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MinimalDynamicHomeWidgets(
    widgetsOrder: List<String>,
    todos: List<Todo>,
    notes: List<Note>,
    favoriteApps: List<AppInfo>,
    homeSystemWidgetIds: Set<Int>,
    appWidgetManager: AppWidgetManager?,
    appWidgetHost: AppWidgetHost?,
    showAllTodosPreference: Boolean = false,
    showAllNotesPreference: Boolean = false,
    onToggleTodo: (String) -> Unit,
    onOpenMemo: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenClock: () -> Unit = {},
    onLaunchApp: (AppInfo) -> Unit,
    quickNoteText: String = "",
    onQuickNoteChange: (String) -> Unit = {},
    onClearQuickNote: () -> Unit = {},
    onOpenQuickNote: () -> Unit = {},
    onAddHomeWidget: (() -> Unit)?,
    onDeleteHomeWidget: ((Int) -> Unit)?,
    modifier: Modifier = Modifier
) {
    var pomodoroWidgetRendered = false

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        widgetsOrder.distinct().forEach { widgetKey ->
            when (widgetKey) {
                "TODOS" -> {
                    MinimalHomeTodosWidget(
                        todos = todos,
                        initialShowAll = showAllTodosPreference,
                        onToggleTodo = onToggleTodo,
                        onOpenMemo = onOpenMemo
                    )
                }

                "NOTES" -> {
                    MinimalHomeNotesWidget(
                        notes = notes,
                        initialShowAll = showAllNotesPreference,
                        onOpenMemo = onOpenMemo
                    )
                }

                "QUICK_NOTE" -> {
                    MinimalHomeQuickNoteWidget(
                        quickNoteText = quickNoteText,
                        onNoteChange = onQuickNoteChange,
                        onClear = onClearQuickNote,
                        onOpenFullScreen = onOpenQuickNote
                    )
                }

                "CALENDAR" -> {
                    MinimalHomeCalendarWidget(
                        onOpenCalendar = onOpenCalendar
                    )
                }

                "ALARM_COUNTDOWN" -> {
                    MinimalHomeAlarmCountdownWidget(
                        onOpenClock = onOpenClock
                    )
                }

                "POMODORO", "TIMERS", "LIVE_TIMER" -> {
                    if (!pomodoroWidgetRendered) {
                        pomodoroWidgetRendered = true
                        MinimalHomePomodoroWidget()
                    }
                }

                "SHORTCUTS" -> {
                    if (favoriteApps.isNotEmpty()) {
                        MinimalHomeShortcutsWidget(
                            favoriteApps = favoriteApps,
                            onLaunchApp = onLaunchApp
                        )
                    }
                }

                "SYSTEM_WIDGETS" -> {
                    if (appWidgetManager != null && appWidgetHost != null) {
                        MinimalHomeSystemWidgetsSection(
                            systemWidgetIds = homeSystemWidgetIds,
                            appWidgetManager = appWidgetManager,
                            appWidgetHost = appWidgetHost,
                            onAddWidget = onAddHomeWidget ?: {},
                            onDeleteWidget = onDeleteHomeWidget ?: {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalHomeTodosWidget(
    todos: List<Todo>,
    initialShowAll: Boolean = false,
    onToggleTodo: (String) -> Unit,
    onOpenMemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTodos = todos.filter { !it.isCompleted }
    if (activeTodos.isNotEmpty()) {
        var isExpanded by remember(initialShowAll) { mutableStateOf(initialShowAll) }
        val displayTodos = if (isExpanded) activeTodos else activeTodos.take(2)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MinimalCardBg)
                .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "tasks",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalWhite,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (activeTodos.size > 2) {
                            Text(
                                text = if (isExpanded) "collapse" else "all (${activeTodos.size})",
                                fontSize = 11.sp,
                                color = MinimalRed,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { isExpanded = !isExpanded }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = "open memo",
                            fontSize = 11.sp,
                            color = MinimalGray,
                            modifier = Modifier.clickable { onOpenMemo() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                displayTodos.forEach { todo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleTodo(todo.id) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {}
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = todo.title,
                            fontSize = 13.sp,
                            color = MinimalWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalHomeNotesWidget(
    notes: List<Note>,
    initialShowAll: Boolean = false,
    onOpenMemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (notes.isNotEmpty()) {
        var isExpanded by remember(initialShowAll) { mutableStateOf(initialShowAll) }
        val displayNotes = if (isExpanded) notes else notes.take(2)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(dampingRatio = 0.82f, stiffness = 400f))
                .clip(RoundedCornerShape(12.dp))
                .background(MinimalCardBg)
                .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
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
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "notes",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalWhite,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (notes.size > 2) {
                            Text(
                                text = if (isExpanded) "collapse" else "all (${notes.size})",
                                fontSize = 11.sp,
                                color = MinimalRed,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { isExpanded = !isExpanded }
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        Text(
                            text = "open memo",
                            fontSize = 11.sp,
                            color = MinimalGray,
                            modifier = Modifier.clickable { onOpenMemo() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                displayNotes.forEach { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenMemo() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = MinimalRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = note.content,
                            fontSize = 13.sp,
                            color = MinimalWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MinimalHomeCalendarWidget(
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
            .clickable { onOpenCalendar() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MinimalRed,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "calendar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "view schedule & events",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = MinimalGray
                    )
                }
            }
            Text(
                text = "OPEN ->",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalRed
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MinimalHomeShortcutsWidget(
    favoriteApps: List<AppInfo>,
    onLaunchApp: (AppInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        favoriteApps.forEach { app ->
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.93f else 1.0f,
                animationSpec = spring(dampingRatio = 0.75f, stiffness = 600f),
                label = "shortcutChipScale"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onLaunchApp(app) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = app.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MinimalWhite
                )
            }
        }
    }
}

@Composable
fun MinimalHomeSystemWidgetsSection(
    systemWidgetIds: Set<Int>,
    appWidgetManager: AppWidgetManager,
    appWidgetHost: AppWidgetHost,
    onAddWidget: () -> Unit,
    onDeleteWidget: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "widgets",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalWhite,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MinimalDarkCardBg)
                    .clickable { onAddWidget() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MinimalRed,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ADD WIDGET",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalWhite
                )
            }
        }

        if (systemWidgetIds.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
                    .clickable { onAddWidget() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Add system app widget (Spotify, Weather, Calendar)",
                    fontSize = 12.sp,
                    color = MinimalGray
                )
            }
        } else {
            systemWidgetIds.forEach { widgetId ->
                MinimalSystemWidgetHostItem(
                    widgetId = widgetId,
                    appWidgetManager = appWidgetManager,
                    appWidgetHost = appWidgetHost,
                    onDelete = { onDeleteWidget(widgetId) }
                )
            }
        }
    }
}

@Composable
fun MinimalHomeAlarmCountdownWidget(
    onOpenClock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var triggerTime by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var hasExactPermission by remember {
        mutableStateOf(AlarmClockPermissionHelper.canScheduleExactAlarms(context))
    }

    LaunchedEffect(Unit) {
        hasExactPermission = AlarmClockPermissionHelper.canScheduleExactAlarms(context)
        triggerTime = AlarmClockPermissionHelper.getNextAlarmTriggerTime(context)
        var ticks = 0
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
            ticks++
            if (ticks >= 30) {
                ticks = 0
                hasExactPermission = AlarmClockPermissionHelper.canScheduleExactAlarms(context)
                triggerTime = AlarmClockPermissionHelper.getNextAlarmTriggerTime(context)
            }
        }
    }

    val diffMs = triggerTime - currentTime
    val hasAlarm = triggerTime > 0L && diffMs > 0L

    val countdownText = remember(diffMs, hasAlarm) {
        if (!hasAlarm) "no upcoming alarm"
        else {
            val totalSeconds = diffMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            when {
                hours > 0 -> "${hours}h ${minutes}m left"
                minutes > 0 -> "${minutes}m ${seconds}s left"
                else -> "${seconds}s left"
            }
        }
    }

    val alarmTimeFormatted = remember(triggerTime, hasAlarm) {
        if (!hasAlarm) ""
        else {
            val sdf = SimpleDateFormat("h:mm a, EEE", Locale.getDefault())
            sdf.format(Date(triggerTime))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalCardBg)
            .border(
                1.dp,
                if (hasAlarm) MinimalRed.copy(alpha = 0.5f)
                else if (!hasExactPermission) MinimalRed.copy(alpha = 0.4f)
                else MinimalDarkGray.copy(alpha = 0.7f),
                RoundedCornerShape(14.dp)
            )
            .clickable {
                if (!hasExactPermission) {
                    AlarmClockPermissionHelper.requestExactAlarmPermission(context)
                } else {
                    onOpenClock()
                }
            }
            .padding(16.dp)
    ) {
        if (hasAlarm) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NEXT ALARM",
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed,
                            letterSpacing = 1.sp
                        )
                    }

                    // Sleek minimal live status pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MinimalDarkCardBg)
                            .border(0.5.dp, MinimalRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "● ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // High-end typographic countdown (Apple / Google standard)
                Text(
                    text = countdownText,
                    fontFamily = AppleSanFranciscoFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    color = MinimalWhite,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                // Detailed ring time & day
                Text(
                    text = "Rings $alarmTimeFormatted",
                    fontFamily = AppleSanFranciscoFontFamily,
                    fontSize = 12.sp,
                    color = MinimalGray
                )
            }
        } else if (!hasExactPermission) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CLOCK PERMISSION NEEDED",
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to grant exact alarm permission",
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 12.sp,
                            color = MinimalGray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MinimalDarkCardBg)
                        .border(0.5.dp, MinimalRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "GRANT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalRed
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MinimalGray,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "NO UPCOMING ALARM",
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalGray,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to configure an alarm in deck",
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 12.sp,
                            color = MinimalDarkGray
                        )
                    }
                }

                Text(
                    text = "● STANDBY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalDarkGray
                )
            }
        }
    }
}

@Composable
fun MinimalHomePomodoroWidget(
    modifier: Modifier = Modifier
) {
    val isRunning by PomodoroTimerManager.isRunning.collectAsState()
    val remainingSeconds by PomodoroTimerManager.remainingSeconds.collectAsState()
    val isBreak by PomodoroTimerManager.isBreakMode.collectAsState()
    val timerLabel by PomodoroTimerManager.timerLabel.collectAsState()
    val workDuration by PomodoroTimerManager.workDurationSeconds.collectAsState()
    var showCustomDialog by remember { mutableStateOf(false) }

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val timeFormatted = "%02d:%02d".format(mins, secs)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MinimalCardBg)
            .border(1.dp, if (isRunning) MinimalRed.copy(alpha = 0.6f) else MinimalDarkGray, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassBottom,
                    contentDescription = null,
                    tint = MinimalRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isBreak) "BREAK FOCUS" else "${timerLabel.uppercase()} FOCUS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MinimalRed,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppleSanFranciscoFontFamily,
                        color = MinimalWhite
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showCustomDialog = true },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Set Custom Duration",
                        tint = MinimalGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { PomodoroTimerManager.toggle() },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        tint = MinimalRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { PomodoroTimerManager.reset() },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reset",
                        tint = MinimalGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomPomodoroDurationDialog(
            currentMinutes = workDuration / 60,
            onDismiss = { showCustomDialog = false },
            onSave = { mins ->
                PomodoroTimerManager.setWorkMinutes(mins)
                showCustomDialog = false
            }
        )
    }
}

