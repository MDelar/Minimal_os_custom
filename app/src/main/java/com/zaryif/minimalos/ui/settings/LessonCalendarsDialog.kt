package com.zaryif.minimalos.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zaryif.minimalos.domain.lessons.LessonSilencer
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite

/** Pick which calendars hold your lessons (e.g. the ICSx⁵ timetable). */
@Composable
fun LessonCalendarsDialog(
    options: List<LessonSilencer.CalendarOption>,
    selected: Set<Long>,
    onDismiss: () -> Unit,
    onSave: (Set<Long>) -> Unit
) {
    var chosen by remember { mutableStateOf(selected) }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MinimalCardBg)
                .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text("Lesson calendars", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MinimalWhite)
            Spacer(Modifier.height(4.dp))
            Text(
                "The phone goes on Do Not Disturb while any timed event in these is on. All-day events are ignored.",
                fontSize = 12.sp, color = MinimalGray
            )
            Spacer(Modifier.height(10.dp))
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                if (options.isEmpty()) {
                    Text("No calendars found on this phone.", fontSize = 13.sp, color = MinimalGray)
                }
                options.forEach { cal ->
                    val checked = cal.id in chosen
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { chosen = if (checked) chosen - cal.id else chosen + cal.id }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { chosen = if (it) chosen + cal.id else chosen - cal.id },
                            colors = CheckboxDefaults.colors(checkedColor = MinimalRed, uncheckedColor = MinimalGray)
                        )
                        Column(Modifier.weight(1f)) {
                            Text(cal.name, fontSize = 14.sp, color = MinimalWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (cal.account.isNotBlank()) {
                                Text(cal.account, fontSize = 11.sp, color = MinimalGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    "CANCEL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalGray,
                    modifier = Modifier.clickable(onClick = onDismiss).padding(8.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "SAVE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalRed,
                    modifier = Modifier.clickable { onSave(chosen) }.padding(8.dp)
                )
            }
        }
    }
}
