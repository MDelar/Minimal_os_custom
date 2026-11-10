package com.zaryif.minimalos.ui.widgets

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.provider.CalendarContract
import android.provider.AlarmClock
import java.util.Calendar
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Launch
import com.zaryif.minimalos.domain.quicknote.QuickNoteManager
import com.zaryif.minimalos.ui.components.MinimalAlarmTimePickerDialog
import com.zaryif.minimalos.ui.components.MinimalHomeQuickNoteWidget
import com.zaryif.minimalos.ui.components.MinimalQuickNoteBlackSpaceSheet
import com.zaryif.minimalos.util.AlarmClockPermissionHelper
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.zaryif.minimalos.data.repository.StorageRepository
import com.zaryif.minimalos.domain.model.Note
import com.zaryif.minimalos.domain.model.Todo
import com.zaryif.minimalos.domain.usecase.ExecuteCommandUseCase
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.domain.timer.PomodoroTimerManager
import com.zaryif.minimalos.ui.components.CustomPomodoroDurationDialog
import com.zaryif.minimalos.ui.components.CustomTimerDurationDialog
import android.app.DatePickerDialog
import androidx.compose.material.icons.filled.CalendarMonth
import com.zaryif.minimalos.ui.theme.AppleSanFranciscoFontFamily
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.ui.theme.MinimalOnAccent
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WidgetsDeckScreen(
    storageRepository: StorageRepository,
    userPreferences: UserPreferences,
    executeCommandUseCase: ExecuteCommandUseCase,
    systemWidgetIds: Set<Int>,
    appWidgetManager: AppWidgetManager,
    appWidgetHost: AppWidgetHost,
    onAddSystemWidget: () -> Unit,
    onDeleteSystemWidget: (Int) -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val todos by storageRepository.todos.collectAsState()
    val notes by storageRepository.notes.collectAsState()
    val deckWidgetsOrder by userPreferences.deckWidgetsOrder.collectAsState(initial = UserPreferences.DEFAULT_DECK_WIDGETS)
    val customCommands by userPreferences.customCommands.collectAsState(initial = emptyMap())
    val quickNote by userPreferences.quickNote.collectAsState(initial = "")
    val scope = rememberCoroutineScope()

    var showQuickNoteSheet by remember { mutableStateOf(false) }

    var newTodoText by remember { mutableStateOf("") }
    var newNoteText by remember { mutableStateOf("") }
    var newEventText by remember { mutableStateOf("") }

    // Global Pomodoro Timer State
    val pomodoroSeconds by PomodoroTimerManager.remainingSeconds.collectAsState()
    val isPomodoroRunning by PomodoroTimerManager.isRunning.collectAsState()
    val isBreakMode by PomodoroTimerManager.isBreakMode.collectAsState()
    val pomodoroWorkDuration by PomodoroTimerManager.workDurationSeconds.collectAsState()
    var showCustomPomodoroDialog by remember { mutableStateOf(false) }
    var customMinutesInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MinimalBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    val (x, y) = dragAmount
                    // Swipe right -> Return to Home
                    if (x > 30 && kotlin.math.abs(x) > kotlin.math.abs(y)) {
                        onBackToHome()
                    }
                }
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "widgets",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite
                    )
                    Text(
                        text = "interactive cards · notes · widgets · focus",
                        fontSize = 12.sp,
                        color = MinimalGray
                    )
                }

                Text(
                    text = "HOME ->",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalRed,
                    modifier = Modifier.clickable { onBackToHome() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Deck of Interactive Cards (Reorderable via Settings)
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                deckWidgetsOrder.forEach { cardType ->
                    when (cardType) {
                        "QUICK_NOTE" -> {
                            item(key = "QUICK_NOTE") {
                                MinimalHomeQuickNoteWidget(
                                    quickNoteText = quickNote,
                                    onNoteChange = { text ->
                                        scope.launch { userPreferences.setQuickNote(text) }
                                    },
                                    onClear = {
                                        scope.launch {
                                            userPreferences.setQuickNote("")
                                            QuickNoteManager.cancelNotification(context)
                                        }
                                    },
                                    onOpenFullScreen = { showQuickNoteSheet = true }
                                )
                            }
                        }

                        "NOTES" -> {
                            item(key = "NOTES") {
                                RabbitNotesCard(
                                    notes = notes.take(4),
                                    newNoteText = newNoteText,
                                    onNewNoteChange = { newNoteText = it },
                                    onAddNote = {
                                        if (newNoteText.isNotBlank()) {
                                            storageRepository.addNote(newNoteText.trim())
                                            newNoteText = ""
                                        }
                                    },
                                    onDeleteNote = { id -> storageRepository.deleteNote(id) }
                                )
                            }
                        }

                        "TODOS" -> {
                            item(key = "TODOS") {
                                RabbitTodoCard(
                                    todos = todos.take(4),
                                    newTodoText = newTodoText,
                                    onNewTodoChange = { newTodoText = it },
                                    onAddTodo = {
                                        if (newTodoText.isNotBlank()) {
                                            storageRepository.addTodo(newTodoText.trim())
                                            newTodoText = ""
                                        }
                                    },
                                    onToggleTodo = { id -> storageRepository.toggleTodo(id) },
                                    onDeleteTodo = { id -> storageRepository.deleteTodo(id) }
                                )
                            }
                        }

                        "CALENDAR" -> {
                            item(key = "CALENDAR") {
                                RabbitCalendarCard(
                                    newEventText = newEventText,
                                    onNewEventChange = { newEventText = it },
                                    onAddEvent = {
                                        if (newEventText.isNotBlank()) {
                                            executeCommandUseCase.execute("*${newEventText.trim()}")
                                            newEventText = ""
                                        }
                                    },
                                    onOpenCalendar = {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    }
                                )
                            }
                        }

                        "POMODORO" -> {
                            item(key = "POMODORO") {
                                RabbitPomodoroCard(
                                    seconds = pomodoroSeconds,
                                    isRunning = isPomodoroRunning,
                                    isBreak = isBreakMode,
                                    workDuration = pomodoroWorkDuration,
                                    onToggle = { PomodoroTimerManager.toggle() },
                                    onReset = { PomodoroTimerManager.reset() },
                                    onSwitchMode = { breakMode -> PomodoroTimerManager.setBreakMode(breakMode) },
                                    onSetDuration = { mins -> PomodoroTimerManager.setWorkMinutes(mins) },
                                    onOpenCustomDialog = {
                                        customMinutesInput = (pomodoroWorkDuration / 60).toString()
                                        showCustomPomodoroDialog = true
                                    }
                                )
                            }
                        }

                        "ALARMS" -> {
                            item(key = "ALARMS") {
                                RabbitAlarmWidgetCard(
                                    onSetAlarm = { query ->
                                        executeCommandUseCase.execute(":$query")
                                    },
                                    onOpenClock = {
                                        AlarmClockPermissionHelper.openClockApp(context)
                                    }
                                )
                            }
                        }

                        "ALIASES" -> {
                            item(key = "ALIASES") {
                                RabbitAliasesWidgetCard(
                                    customCommands = customCommands,
                                    onRunAlias = { keyword ->
                                        executeCommandUseCase.execute(keyword)
                                    },
                                    onAddAlias = { keyword, target ->
                                        scope.launch {
                                            userPreferences.setCustomCommand(keyword, target)
                                        }
                                    },
                                    onDeleteAlias = { keyword ->
                                        scope.launch {
                                            userPreferences.removeCustomCommand(keyword)
                                        }
                                    }
                                )
                            }
                        }

                        "TIMERS" -> {
                            item(key = "TIMERS") {
                                RabbitQuickTimersCard(
                                    onSetTimer = { seconds, _ ->
                                        executeCommandUseCase.execute("+${seconds}s")
                                    }
                                )
                            }
                        }

                        "SYSTEM_WIDGETS" -> {
                            item(key = "SYSTEM_WIDGETS_HEADER") {
                                RabbitSystemWidgetsHeader(onAddWidget = onAddSystemWidget)
                            }

                            if (systemWidgetIds.isEmpty()) {
                                item(key = "SYSTEM_WIDGETS_EMPTY") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(MinimalCardBg)
                                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No external app widgets added yet.\nTap '+ ADD WIDGET' to host Calendar, Spotify, Weather, etc.",
                                            fontSize = 12.sp,
                                            color = MinimalGray,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            } else {
                                items(systemWidgetIds.toList(), key = { "widget_$it" }) { widgetId ->
                                    RabbitSystemWidgetHostItem(
                                        widgetId = widgetId,
                                        appWidgetManager = appWidgetManager,
                                        appWidgetHost = appWidgetHost,
                                        onDelete = { onDeleteSystemWidget(widgetId) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Bottom hint
            Text(
                text = "SWIPE RIGHT FOR HOME ->",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MinimalRed,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        if (showCustomPomodoroDialog) {
            CustomPomodoroDurationDialog(
                currentMinutes = pomodoroWorkDuration / 60,
                onDismiss = { showCustomPomodoroDialog = false },
                onSave = { mins ->
                    PomodoroTimerManager.setWorkMinutes(mins)
                    showCustomPomodoroDialog = false
                }
            )
        }
        // KEEP_MAIN_QUICK_NOTE_SHEET
        if (showQuickNoteSheet) {
            MinimalQuickNoteBlackSpaceSheet(
                quickNoteText = quickNote,
                onNoteChange = { text ->
                    scope.launch { userPreferences.setQuickNote(text) }
                },
                onClear = {
                    scope.launch {
                        userPreferences.setQuickNote("")
                        QuickNoteManager.cancelNotification(context)
                    }
                },
                onDismiss = { showQuickNoteSheet = false }
            )
        }
    }
}

// -------------------------------------------------------------
// RABBIT NOTES CARD
// -------------------------------------------------------------
@Composable
private fun RabbitNotesCard(
    notes: List<Note>,
    newNoteText: String,
    onNewNoteChange: (String) -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "notes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "${notes.size} saved",
                    fontSize = 11.sp,
                    color = MinimalGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inline Note Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (newNoteText.isEmpty()) {
                        Text(
                            text = "quick note…",
                            style = TextStyle(fontSize = 13.sp, color = MinimalGray)
                        )
                    }
                    BasicTextField(
                        value = newNoteText,
                        onValueChange = onNewNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        cursorBrush = SolidColor(MinimalRed),
                        textStyle = TextStyle(fontSize = 13.sp, color = MinimalWhite),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onAddNote() })
                    )
                }
                if (newNoteText.isNotEmpty()) {
                    IconButton(onClick = onAddNote, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save",
                            tint = MinimalRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (notes.isEmpty()) {
                Text(
                    text = "No notes saved. Type above to write a note.",
                    fontSize = 12.sp,
                    color = MinimalGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                notes.forEach { note ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = MinimalRed,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = note.content,
                            fontSize = 13.sp,
                            color = MinimalWhite,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onDeleteNote(note.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete",
                                tint = MinimalGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}

// -------------------------------------------------------------
// RABBIT TO-DO CARD
// -------------------------------------------------------------
@Composable
private fun RabbitTodoCard(
    todos: List<Todo>,
    newTodoText: String,
    onNewTodoChange: (String) -> Unit,
    onAddTodo: () -> Unit,
    onToggleTodo: (String) -> Unit,
    onDeleteTodo: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
            .padding(16.dp)
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "tasks",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "${todos.count { !it.isCompleted }} pending",
                    fontSize = 11.sp,
                    color = MinimalGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inline input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (newTodoText.isEmpty()) {
                        Text(
                            text = "add task directly…",
                            style = TextStyle(fontSize = 13.sp, color = MinimalGray)
                        )
                    }
                    BasicTextField(
                        value = newTodoText,
                        onValueChange = onNewTodoChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        cursorBrush = SolidColor(MinimalRed),
                        textStyle = TextStyle(fontSize = 13.sp, color = MinimalWhite),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onAddTodo() })
                    )
                }
                if (newTodoText.isNotEmpty()) {
                    IconButton(onClick = onAddTodo, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = MinimalRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (todos.isEmpty()) {
                Text(
                    text = "No tasks in stack. Type above to add.",
                    fontSize = 12.sp,
                    color = MinimalGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                todos.forEach { todo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (todo.isCompleted) MinimalRed else MinimalDarkCardBg)
                                .border(1.dp, if (todo.isCompleted) MinimalRed else MinimalDarkGray, RoundedCornerShape(4.dp))
                                .clickable { onToggleTodo(todo.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (todo.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MinimalOnAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = todo.title,
                            fontSize = 13.sp,
                            color = if (todo.isCompleted) MinimalGray else MinimalWhite,
                            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onToggleTodo(todo.id) }
                        )
                        IconButton(
                            onClick = { onDeleteTodo(todo.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = MinimalGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

    }
}

// -------------------------------------------------------------
// RABBIT CALENDAR EVENT CARD (Auto silent create)
// -------------------------------------------------------------
@Composable
private fun RabbitCalendarCard(
    newEventText: String,
    onNewEventChange: (String) -> Unit,
    onAddEvent: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val todayFormatted = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "schedule",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "open calendar",
                    fontSize = 11.sp,
                    color = MinimalGray,
                    modifier = Modifier.clickable { onOpenCalendar() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = todayFormatted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MinimalWhite
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Inline Event Add (auto inserts into Calendar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (newEventText.isEmpty()) {
                        Text(
                            text = "quick add event directly…",
                            style = TextStyle(fontSize = 13.sp, color = MinimalGray)
                        )
                    }
                    BasicTextField(
                        value = newEventText,
                        onValueChange = onNewEventChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        cursorBrush = SolidColor(MinimalRed),
                        textStyle = TextStyle(fontSize = 13.sp, color = MinimalWhite),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onAddEvent() })
                    )
                }
                if (newEventText.isNotEmpty()) {
                    IconButton(onClick = onAddEvent, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Create",
                            tint = MinimalRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

    }
}

// -------------------------------------------------------------
// RABBIT POMODORO CARD
// -------------------------------------------------------------
@Composable
private fun RabbitPomodoroCard(
    seconds: Int,
    isRunning: Boolean,
    isBreak: Boolean,
    workDuration: Int,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    onSwitchMode: (Boolean) -> Unit,
    onSetDuration: (Int) -> Unit,
    onOpenCustomDialog: () -> Unit
) {
    val mins = seconds / 60
    val secs = seconds % 60
    val timeFormatted = "%02d:%02d".format(mins, secs)
    val workMins = workDuration / 60

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MinimalCardBg)
            .border(1.5.dp, MinimalRed, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBreak) "break" else "pomodoro",
                        fontFamily = AppleSanFranciscoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${workMins}m focus",
                        fontFamily = AppleSanFranciscoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (!isBreak) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isBreak) MinimalWhite else MinimalGray,
                        modifier = Modifier.clickable { onSwitchMode(false) }
                    )
                    Text(
                        text = "5m rest",
                        fontFamily = AppleSanFranciscoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isBreak) FontWeight.Bold else FontWeight.Normal,
                        color = if (isBreak) MinimalWhite else MinimalGray,
                        modifier = Modifier.clickable { onSwitchMode(true) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Duration selector chips: 15m, 25m, 45m, 60m, custom
            if (!isBreak) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(15, 25, 45, 60).forEach { presetMins ->
                        val isCurrent = workMins == presetMins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) MinimalRed else MinimalDarkCardBg)
                                .border(1.dp, if (isCurrent) MinimalRed else MinimalDarkGray, RoundedCornerShape(6.dp))
                                .clickable { onSetDuration(presetMins) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${presetMins}m",
                                fontFamily = AppleSanFranciscoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) MinimalOnAccent else MinimalWhite
                            )
                        }
                    }

                    val isCustom = workMins !in listOf(15, 25, 45, 60)
                    if (isCustom) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MinimalRed)
                                .border(1.dp, MinimalRed, RoundedCornerShape(6.dp))
                                .clickable { onOpenCustomDialog() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${workMins}m",
                                fontFamily = AppleSanFranciscoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalOnAccent
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                            .clickable { onOpenCustomDialog() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Custom duration",
                            tint = MinimalRed,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormatted,
                    fontFamily = AppleSanFranciscoFontFamily,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Light,
                    color = MinimalWhite
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Reset",
                            tint = MinimalGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalRed)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = MinimalOnAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

    }
}

// -------------------------------------------------------------
// RABBIT ALARM WIDGET CARD
// -------------------------------------------------------------
@Composable
private fun RabbitAlarmWidgetCard(
    onSetAlarm: (String) -> Unit,
    onOpenClock: () -> Unit
) {
    val context = LocalContext.current
    var statusFeedback by remember { mutableStateOf<String?>(null) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var selectedTimeDisplay by remember { mutableStateOf("7:00 AM") }
    var selectedDayDisplay by remember { mutableStateOf("Tomorrow") }
    var hasUserSelectedPreset by remember { mutableStateOf(false) }

    // Live query for scheduled system alarm
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
    val hasActiveAlarm = triggerTime > 0L && diffMs > 0L

    val activeAlarmTimeFormatted = remember(triggerTime, hasActiveAlarm) {
        if (!hasActiveAlarm) ""
        else {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(Date(triggerTime))
        }
    }

    val activeAlarmDayFormatted = remember(triggerTime, hasActiveAlarm) {
        if (!hasActiveAlarm) ""
        else {
            val sdf = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            sdf.format(Date(triggerTime))
        }
    }

    val presetTimes = listOf("6:00 AM", "7:00 AM", "7:30 AM", "8:00 AM")

    val openDatePicker = {
        val cal = Calendar.getInstance()
        val dpd = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosen = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                val sdf = SimpleDateFormat("MMM d", Locale.US)
                selectedDayDisplay = sdf.format(chosen.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        dpd.datePicker.minDate = System.currentTimeMillis() - 1000
        dpd.show()
    }

    if (showTimePickerDialog) {
        MinimalAlarmTimePickerDialog(
            initialHour = 7,
            initialMinute = 0,
            initialAmPm = "AM",
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { hour, minute, amPm, dayLabel ->
                showTimePickerDialog = false
                val formattedTime = "%d:%02d %s".format(hour, minute, amPm)
                selectedTimeDisplay = formattedTime
                selectedDayDisplay = dayLabel
                val commandQuery = "$formattedTime $dayLabel"
                onSetAlarm(commandQuery)
                statusFeedback = "Alarm set for $formattedTime ($dayLabel)"
                hasUserSelectedPreset = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MinimalCardBg)
            .border(1.5.dp, if (hasActiveAlarm) MinimalRed else MinimalDarkGray, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header matching RabbitPomodoroCard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable { onOpenClock() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Open Clock",
                        tint = if (hasActiveAlarm) MinimalRed else MinimalGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "alarm",
                        fontFamily = AppleSanFranciscoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                // Active indicator or Date selector pill
                if (hasActiveAlarm) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalRed.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .clickable { onOpenClock() }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "● ACTIVE",
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                            .clickable { openDatePicker() }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedDayDisplay,
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preset time selector chips: 6:00 AM, 7:00 AM, 7:30 AM, 8:00 AM, + button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetTimes.forEach { preset ->
                    val isSelected = hasUserSelectedPreset && selectedTimeDisplay == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MinimalRed else MinimalDarkCardBg)
                            .border(1.dp, if (isSelected) MinimalRed else MinimalDarkGray, RoundedCornerShape(6.dp))
                            .clickable {
                                selectedTimeDisplay = preset
                                hasUserSelectedPreset = true
                                statusFeedback = null
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = preset,
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MinimalOnAccent else MinimalWhite
                        )
                    }
                }

                val isCustomTime = hasUserSelectedPreset && selectedTimeDisplay !in presetTimes
                if (isCustomTime) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MinimalRed)
                            .border(1.dp, MinimalRed, RoundedCornerShape(6.dp))
                            .clickable { showTimePickerDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedTimeDisplay,
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalOnAccent
                        )
                    }
                }

                // Theme matched + button to open time picker dialog
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                        .clickable { showTimePickerDialog = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Custom Alarm Time",
                        tint = MinimalRed,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Display Row: Time + Subtitle on left, action buttons on right!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (hasActiveAlarm) {
                        Text(
                            text = activeAlarmTimeFormatted,
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Light,
                            color = MinimalWhite
                        )
                        Text(
                            text = "Rings $activeAlarmDayFormatted",
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 11.sp,
                            color = MinimalRed
                        )
                    } else {
                        Text(
                            text = if (hasUserSelectedPreset) selectedTimeDisplay else "--:--",
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Light,
                            color = if (hasUserSelectedPreset) MinimalWhite else MinimalGray
                        )
                        Text(
                            text = if (hasUserSelectedPreset) {
                                "Tap ⏰ to set for $selectedDayDisplay"
                            } else {
                                "No alarm set • Choose a preset or tap +"
                            },
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontSize = 11.sp,
                            color = MinimalGray
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Button 1: Clock / Calendar button
                    IconButton(
                        onClick = {
                            if (hasActiveAlarm) {
                                onOpenClock()
                            } else {
                                openDatePicker()
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = if (hasActiveAlarm) Icons.Default.Schedule else Icons.Default.CalendarMonth,
                            contentDescription = if (hasActiveAlarm) "Open Clock" else "Pick Date",
                            tint = MinimalGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Button 2: Set Alarm
                    IconButton(
                        onClick = {
                            val timeToSet = if (hasUserSelectedPreset) selectedTimeDisplay else "7:00 AM"
                            val query = "$timeToSet $selectedDayDisplay"
                            onSetAlarm(query)
                            statusFeedback = "Alarm set for $timeToSet ($selectedDayDisplay)"
                            hasUserSelectedPreset = false
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Set Alarm",
                            tint = MinimalOnAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // Feedback badge if alarm was set
            statusFeedback?.let { feedback ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = feedback,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalWhite
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MinimalGray,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { statusFeedback = null }
                    )
                }
            }
        }

    }
}

// -------------------------------------------------------------
// RABBIT ALIASES & SHORTCUTS WIDGET CARD
// -------------------------------------------------------------
@Composable
private fun RabbitAliasesWidgetCard(
    customCommands: Map<String, String>,
    onRunAlias: (String) -> Unit,
    onAddAlias: (String, String) -> Unit,
    onDeleteAlias: (String) -> Unit
) {
    var showAddRow by remember { mutableStateOf(false) }
    var aliasKeyword by remember { mutableStateOf("") }
    var aliasTarget by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "aliases & shortcuts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (showAddRow) MinimalDarkCardBg else MinimalRed)
                            .clickable { showAddRow = !showAddRow }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (showAddRow) "CANCEL" else "+ ADD ALIAS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showAddRow) MinimalWhite else MinimalOnAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inline add form if expanded
            if (showAddRow) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "NEW ALIAS SHORTCUT",
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
                        Box(
                            modifier = Modifier
                                .weight(0.35f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MinimalCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (aliasKeyword.isEmpty()) {
                                Text("alias (yt)", fontSize = 11.sp, color = MinimalGray)
                            }
                            BasicTextField(
                                value = aliasKeyword,
                                onValueChange = { aliasKeyword = it },
                                singleLine = true,
                                cursorBrush = SolidColor(MinimalRed),
                                textStyle = TextStyle(fontSize = 12.sp, color = MinimalWhite, fontFamily = FontFamily.Monospace)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.65f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MinimalCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (aliasTarget.isEmpty()) {
                                Text("url or action", fontSize = 11.sp, color = MinimalGray)
                            }
                            BasicTextField(
                                value = aliasTarget,
                                onValueChange = { aliasTarget = it },
                                singleLine = true,
                                cursorBrush = SolidColor(MinimalRed),
                                textStyle = TextStyle(fontSize = 12.sp, color = MinimalWhite, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (aliasKeyword.isNotBlank() && aliasTarget.isNotBlank()) MinimalRed else MinimalDarkGray)
                                .clickable(enabled = aliasKeyword.isNotBlank() && aliasTarget.isNotBlank()) {
                                    onAddAlias(aliasKeyword.trim(), aliasTarget.trim())
                                    aliasKeyword = ""
                                    aliasTarget = ""
                                    showAddRow = false
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "SAVE ALIAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (aliasKeyword.isNotBlank() && aliasTarget.isNotBlank()) MinimalOnAccent else MinimalWhite
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (customCommands.isEmpty()) {
                Column {
                    Text(
                        text = "No custom aliases created yet. Tap '+ ADD ALIAS' or choose a quick template:",
                        fontSize = 12.sp,
                        color = MinimalGray,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val templates = listOf(
                            Triple("yt", "https://youtube.com", "YouTube"),
                            Triple("gh", "https://github.com", "GitHub"),
                            Triple("wifi", "android.settings.WIFI_SETTINGS", "Wi-Fi")
                        )
                        templates.forEach { (kw, target, label) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                    .clickable { onAddAlias(kw, target) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+ $label",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalRed
                                )
                            }
                        }
                    }
                }
            } else {
                // List active aliases
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    customCommands.entries.take(6).forEach { (keyword, target) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                            .clickable { onRunAlias(keyword) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = keyword,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MinimalRed,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RUN ->",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MinimalGray
                                    )
                                }
                                Text(
                                    text = target,
                                    fontSize = 11.sp,
                                    color = MinimalGray,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }

                            IconButton(
                                onClick = { onDeleteAlias(keyword) },
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

    }
}

// -------------------------------------------------------------
// RABBIT QUICK TIMERS CARD (One-Tap Silent Timers)
// -------------------------------------------------------------
@Composable
private fun RabbitQuickTimersCard(
    onSetTimer: (Int, String) -> Unit
) {
    var showCustomTimerDialog by remember { mutableStateOf(false) }

    val presets = listOf(
        Pair(300, "5m"),
        Pair(900, "15m"),
        Pair(1800, "30m"),
        Pair(2700, "45m")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MinimalRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "timers",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "tap preset or +",
                    fontSize = 11.sp,
                    color = MinimalGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presets.forEach { (sec, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                            .clickable { onSetTimer(sec, label) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalWhite
                        )
                    }
                }

                // Theme matched + button for custom timer duration!
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                        .clickable { showCustomTimerDialog = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Custom Timer",
                        tint = MinimalRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showCustomTimerDialog) {
        CustomTimerDurationDialog(
            initialMinutes = 10,
            onDismiss = { showCustomTimerDialog = false },
            onSetTimer = { seconds, label ->
                onSetTimer(seconds, label)
                showCustomTimerDialog = false
            }
        )

    }
}

// -------------------------------------------------------------
// SYSTEM WIDGETS HEADER
// -------------------------------------------------------------
@Composable
private fun RabbitSystemWidgetsHeader(onAddWidget: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = null,
                tint = MinimalRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "widgets",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalWhite,
                letterSpacing = 0.5.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MinimalRed)
                .clickable { onAddWidget() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "+ ADD WIDGET",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalOnAccent
            )
        }

    }
}

// -------------------------------------------------------------
// SYSTEM WIDGET HOST ITEM
// -------------------------------------------------------------
@Composable
private fun RabbitSystemWidgetHostItem(
    widgetId: Int,
    appWidgetManager: AppWidgetManager,
    appWidgetHost: AppWidgetHost,
    onDelete: () -> Unit
) {
    com.zaryif.minimalos.ui.components.MinimalSystemWidgetHostItem(
        widgetId = widgetId,
        appWidgetManager = appWidgetManager,
        appWidgetHost = appWidgetHost,
        onDelete = onDelete
    )
}
