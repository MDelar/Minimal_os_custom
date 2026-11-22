package com.zaryif.minimalos.ui.components

import androidx.lifecycle.repeatOnLifecycle
import android.content.Intent
import android.provider.AlarmClock
import com.zaryif.minimalos.util.AlarmClockPermissionHelper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.domain.timer.PomodoroTimerManager
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalClockSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentTime by remember { mutableStateOf("") }
    var currentSeconds by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showAlarmDialog by remember { mutableStateOf(false) }

    val sheetLifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current
    LaunchedEffect(sheetLifecycle) {
        val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
        val secFormat = SimpleDateFormat("ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        sheetLifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
            while (true) {
                val now = Date()
                currentTime = timeFormat.format(now)
                currentSeconds = secFormat.format(now)
                currentDate = dateFormat.format(now).lowercase()
                delay(1000 - System.currentTimeMillis() % 1000) // on the second
            }
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
                .fillMaxHeight(0.6f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Minimal Clock",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalWhite
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MinimalGray
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentTime,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    color = MinimalWhite
                )
                Text(
                    text = ":$currentSeconds",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MinimalRed,
                    modifier = Modifier.padding(bottom = 14.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = currentDate,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = MinimalGray
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinimalButton(
                    text = "+ set timer",
                    onClick = { showTimerDialog = true },
                    modifier = Modifier.weight(1f),
                    style = ButtonStyle.SECONDARY
                )
                MinimalButton(
                    text = ": set alarm",
                    onClick = { showAlarmDialog = true },
                    modifier = Modifier.weight(1f),
                    style = ButtonStyle.PRIMARY
                )
            }
        }

        if (showTimerDialog) {
            CustomTimerDurationDialog(
                initialMinutes = 10,
                onDismiss = { showTimerDialog = false },
                onSetTimer = { seconds, label ->
                    PomodoroTimerManager.start(seconds, label)
                    AlarmClockPermissionHelper.setTimer(context, seconds, "Minimal Timer")
                    showTimerDialog = false
                    onDismiss()
                }
            )
        }

        if (showAlarmDialog) {
            MinimalAlarmTimePickerDialog(
                initialHour = 7,
                initialMinute = 0,
                initialAmPm = "AM",
                onDismiss = { showAlarmDialog = false },
                onConfirm = { hour, minute, amPm, dayLabel ->
                    val finalHour = if (amPm == "PM" && hour < 12) hour + 12 else if (amPm == "AM" && hour == 12) 0 else hour
                    AlarmClockPermissionHelper.setAlarm(context, finalHour, minute, "Minimal Alarm ($dayLabel)")
                    showAlarmDialog = false
                    onDismiss()
                }
            )
        }
    }
}
