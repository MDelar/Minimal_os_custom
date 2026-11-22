package com.zaryif.minimalos.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.domain.prayer.AsrSchool
import com.zaryif.minimalos.domain.prayer.PrayerMethod
import com.zaryif.minimalos.domain.prayer.PrayerTimesCalc
import com.zaryif.minimalos.domain.weather.WeatherRepository
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import java.time.ZoneId

/**
 * "asr 14:05 · in 1h 20m" under the home clock, for the weather widget's town.
 * Recalculated when [minuteKey] changes (the home clock's minute), so it costs
 * nothing between minutes and nothing while home isn't showing.
 */
@Composable
fun NextPrayerLine(minuteKey: String, method: String?, asrSchool: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val text = remember(minuteKey, method, asrSchool) {
        val place = WeatherRepository.location(context)
        if (place == null) null
        else try {
            val now = System.currentTimeMillis()
            val zone = ZoneId.systemDefault()
            val next = PrayerTimesCalc.next(place.first, place.second, now, zone, PrayerMethod.from(method), AsrSchool.from(asrSchool))
            PrayerTimesCalc.label(next, now, zone)
        } catch (_: Exception) {
            null
        }
    }
    Text(
        text = text ?: "set a town on the weather card for prayer times",
        fontSize = if (text != null) 13.sp else 11.sp,
        fontFamily = FontFamily.Monospace,
        color = if (text != null && text.endsWith("now")) MinimalRed else MinimalGray,
        modifier = modifier
    )
}
