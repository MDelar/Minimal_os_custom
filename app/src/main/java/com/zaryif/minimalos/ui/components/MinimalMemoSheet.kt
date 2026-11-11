package com.zaryif.minimalos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.data.repository.StorageRepository
import com.zaryif.minimalos.domain.quicknote.QuickNoteManager
import com.zaryif.minimalos.domain.timer.PomodoroTimerManager
import com.zaryif.minimalos.domain.usecase.ExecuteCommandUseCase
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalOnAccent
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.util.AppStrings
import com.zaryif.minimalos.util.NotificationExporter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalMemoSheet(
    storageRepository: StorageRepository,
    executeCommandUseCase: ExecuteCommandUseCase? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notes by storageRepository.notes.collectAsState()
    val todos by storageRepository.todos.collectAsState()

    var selectedTab by remember { mutableStateOf("TASKS") }
    var searchQuery by remember { mutableStateOf("") }
    var newItemText by remember { mutableStateOf("") }
    var showCustomTimerDialog by remember { mutableStateOf(false) }

    val filteredTodos = remember(todos, searchQuery) {
        if (searchQuery.isBlank()) todos
        else todos.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
    }

    val filteredNotes = remember(notes, searchQuery) {
        if (searchQuery.isBlank()) notes
        else notes.filter { it.content.contains(searchQuery.trim(), ignoreCase = true) }
    }

    fun submitNewItem() {
        val trimmed = newItemText.trim()
        if (trimmed.isNotEmpty()) {
            if (trimmed.startsWith("+")) {
                executeCommandUseCase?.execute(trimmed)
            } else if (trimmed.startsWith("timer ", ignoreCase = true)) {
                executeCommandUseCase?.execute("+" + trimmed.removePrefix("timer ").trim())
            } else if (trimmed.startsWith("pomo", ignoreCase = true)) {
                executeCommandUseCase?.execute("+pomo")
            } else if (selectedTab == "TASKS") {
                storageRepository.addTodo(trimmed)
            } else {
                storageRepository.addNote(trimmed)
            }
            newItemText = ""
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MinimalBlack,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Header with title and + TIMER shortcut
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.memoTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalWhite,
                    letterSpacing = (-0.5).sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                            .clickable { showCustomTimerDialog = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Set Timer",
                                tint = MinimalRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = AppStrings.memoTimerBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MinimalGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs Segmented Switcher: TASKS vs NOTES
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MinimalDarkCardBg)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tasks Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "TASKS") MinimalCardBg else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { selectedTab = "TASKS" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${AppStrings.memoTabTasks} (${todos.size})",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == "TASKS") FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTab == "TASKS") MinimalWhite else MinimalGray
                    )
                }

                // Notes Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == "NOTES") MinimalCardBg else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { selectedTab = "NOTES" }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${AppStrings.memoTabNotes} (${notes.size})",
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == "NOTES") FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTab == "NOTES") MinimalWhite else MinimalGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Search Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MinimalGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = if (selectedTab == "TASKS") AppStrings.memoSearchTasksHint else AppStrings.memoSearchNotesHint,
                                style = TextStyle(fontSize = 13.sp, color = MinimalGray)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            cursorBrush = SolidColor(MinimalRed),
                            textStyle = TextStyle(fontSize = 13.sp, color = MinimalWhite),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MinimalGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Add New Item Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MinimalCardBg)
                        .border(
                            1.dp,
                            if (newItemText.isNotBlank()) MinimalRed.copy(alpha = 0.6f) else MinimalDarkGray,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (newItemText.isEmpty()) {
                            Text(
                                text = if (selectedTab == "TASKS") AppStrings.memoAddTaskHint else AppStrings.memoAddNoteHint,
                                style = TextStyle(fontSize = 13.sp, color = MinimalGray)
                            )
                        }
                        BasicTextField(
                            value = newItemText,
                            onValueChange = { newItemText = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            cursorBrush = SolidColor(MinimalRed),
                            textStyle = TextStyle(fontSize = 13.sp, color = MinimalWhite),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submitNewItem() })
                        )
                    }
                    IconButton(
                        onClick = { submitNewItem() },
                        modifier = Modifier.size(30.dp),
                        enabled = newItemText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = if (newItemText.isNotBlank()) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Add,
                            contentDescription = "Add",
                            tint = if (newItemText.isNotBlank()) MinimalRed else MinimalGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Tab Contents
                if (selectedTab == "TASKS") {
                    if (filteredTodos.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No tasks matching \"$searchQuery\"" else AppStrings.memoEmptyTasks,
                                fontSize = 13.sp,
                                color = MinimalGray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTodos, key = { it.id }) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MinimalCardBg)
                                        .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                                        .clickable { storageRepository.toggleTodo(item.id) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (item.isCompleted) MinimalRed else MinimalDarkCardBg)
                                            .border(1.dp, if (item.isCompleted) MinimalRed else MinimalDarkGray, RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (item.isCompleted) {
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
                                        text = item.title,
                                        fontSize = 14.sp,
                                        color = if (item.isCompleted) MinimalGray else MinimalWhite,
                                        textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            executeCommandUseCase?.execute("+25m")
                                            PomodoroTimerManager.start(25 * 60, item.title)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = "Start Focus Timer",
                                            tint = MinimalRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { storageRepository.deleteTodo(item.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MinimalDarkGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // NOTES tab
                    if (filteredNotes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No notes matching \"$searchQuery\"" else AppStrings.memoEmptyNotes,
                                fontSize = 13.sp,
                                color = MinimalGray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredNotes, key = { it.id }) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MinimalCardBg)
                                        .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.content,
                                            fontSize = 14.sp,
                                            color = MinimalWhite,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { storageRepository.deleteNote(item.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MinimalDarkGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        } // End Column
    } // End ModalBottomSheet

    if (showCustomTimerDialog) {
        CustomTimerDurationDialog(
            initialMinutes = 10,
            onDismiss = { showCustomTimerDialog = false },
            onSetTimer = { seconds, label ->
                executeCommandUseCase?.execute("+${seconds}s")
                PomodoroTimerManager.start(seconds, label)
                showCustomTimerDialog = false
            }
        )
    }
}
