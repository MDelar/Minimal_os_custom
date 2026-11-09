package com.zaryif.minimalos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.domain.timer.PomodoroTimerManager
import com.zaryif.minimalos.ui.theme.AppleSanFranciscoFontFamily
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite

/**
 * Apple Live Activity style countdown card for the Home Screen.
 * Automatically appears whenever a Pomodoro session or timer is active.
 */
@Composable
fun MinimalLiveTimerCard(
    modifier: Modifier = Modifier
) {
    val hasStarted by PomodoroTimerManager.hasStarted.collectAsState()
    val isRunning by PomodoroTimerManager.isRunning.collectAsState()
    val remainingSeconds by PomodoroTimerManager.remainingSeconds.collectAsState()
    val workDuration by PomodoroTimerManager.workDurationSeconds.collectAsState()
    val breakDuration by PomodoroTimerManager.breakDurationSeconds.collectAsState()
    val timerLabel by PomodoroTimerManager.timerLabel.collectAsState()
    val isBreak by PomodoroTimerManager.isBreakMode.collectAsState()

    // Show whenever timer is actively started or running, with positive remaining seconds
    val hasActiveSession = (hasStarted || isRunning) && remainingSeconds > 0

    AnimatedVisibility(
        visible = hasActiveSession,
        enter = expandVertically(spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeIn(tween(180)),
        exit = shrinkVertically(tween(140)) + fadeOut(tween(140)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MinimalCardBg)
                .border(1.dp, MinimalRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MinimalRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = AppleSanFranciscoFontFamily,
                            letterSpacing = (-0.4).sp,
                            color = MinimalWhite
                        )
                        Text(
                            text = if (isBreak) "break session" else "${timerLabel.lowercase()} focus",
                            fontSize = 11.sp,
                            fontFamily = AppleSanFranciscoFontFamily,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { PomodoroTimerManager.toggle() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Resume",
                            tint = MinimalWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { PomodoroTimerManager.reset() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color(0xFFA1A1AA),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
