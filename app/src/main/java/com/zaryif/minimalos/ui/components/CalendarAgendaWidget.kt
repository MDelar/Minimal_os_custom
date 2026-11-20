package com.zaryif.minimalos.ui.components

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.zaryif.minimalos.domain.calendar.AgendaEvent
import com.zaryif.minimalos.domain.calendar.CalendarAgenda
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.time.ZoneId

/**
 * Home calendar card: the next few events as text rows ("14:00  dentist",
 * "tmrw 09:00  staff meeting"). Tap an event to open it, or "open" for the
 * calendar app. Reads the phone's calendar storage locally; refreshes while
 * the launcher is on screen.
 */
@Composable
fun MinimalHomeCalendarWidget(
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier,
    maxEvents: Int = 3
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(CalendarAgenda.hasPermission(context)) }
    var events by remember { mutableStateOf<List<AgendaEvent>>(emptyList()) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var loaded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, hasPermission) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            hasPermission = CalendarAgenda.hasPermission(context)
            while (isActive && hasPermission) {
                now = System.currentTimeMillis()
                events = withContext(Dispatchers.IO) {
                    CalendarAgenda.upcoming(context, limit = maxEvents, now = now)
                } ?: emptyList()
                loaded = true
                delay(60_000) // keeps "now" and finished events up to date
            }
        }
    }

    fun openEvent(e: AgendaEvent) {
        val intent = Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, e.eventId))
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, e.begin)
            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, e.end)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { context.startActivity(intent) } catch (_: Exception) { onOpenCalendar() }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
            .clickable {
                if (hasPermission) onOpenCalendar()
                else permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
            }
            .padding(horizontal = 12.dp, vertical = 10.dp)
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
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "calendar", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MinimalGray)
                }
                Text(text = "open →", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MinimalRed)
            }

            Spacer(modifier = Modifier.height(6.dp))

            when {
                !hasPermission -> Text(
                    text = "tap to show your next events",
                    fontSize = 13.sp, color = MinimalWhite
                )
                loaded && events.isEmpty() -> Text(
                    text = "nothing in the next 7 days",
                    fontSize = 13.sp, color = MinimalGray
                )
                else -> {
                    val zone = ZoneId.systemDefault()
                    events.forEach { e ->
                        val label = CalendarAgenda.whenLabel(e, now, zone)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openEvent(e) }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (label == "now") MinimalRed else MinimalGray,
                                maxLines = 1,
                                modifier = Modifier.width(80.dp)
                            )
                            Text(
                                text = e.title,
                                fontSize = 13.sp,
                                color = MinimalWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
