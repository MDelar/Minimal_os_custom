package com.zaryif.minimalos.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.zaryif.minimalos.domain.weather.WeatherParser
import com.zaryif.minimalos.domain.weather.WeatherRepository
import com.zaryif.minimalos.domain.weather.WeatherSnapshot
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle as DateTextStyle
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val Mono = FontFamily.Monospace

private fun deg(value: Double) = "${value.roundToInt()}°"

/**
 * Home-screen weather card, styled after Omalauncher's "today" widget.
 * Tap to expand (next 24 hours, 3 days, sunrise and sunset); long-press to change town.
 * Only fetches while the launcher is on screen, at most every 30 minutes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinimalHomeWeatherWidget(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var weather by remember { mutableStateOf(WeatherRepository.cached(context)) }
    var hasTown by remember { mutableStateOf(WeatherRepository.placeName(context) != null) }
    var lastFetchFailed by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showTownDialog by remember { mutableStateOf(false) }

    suspend fun load(force: Boolean) {
        val result = WeatherRepository.refresh(context, force)
        weather = result
        // Offline if nothing could be fetched, or what we have is well past its refresh time.
        lastFetchFailed = result == null ||
            System.currentTimeMillis() - result.fetchedAt > WeatherRepository.REFRESH_AFTER_MS * 2
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, hasTown) {
        if (!hasTown) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                load(force = false)
                delay(WeatherRepository.REFRESH_AFTER_MS)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = { if (hasTown && weather != null) expanded = !expanded else showTownDialog = true },
                onLongClick = { showTownDialog = true }
            )
            .animateContentSize()
            .padding(12.dp)
    ) {
        val w = weather
        when {
            !hasTown -> WeatherMessage("weather", "tap to set your town")
            w == null -> WeatherMessage(
                "weather · ${WeatherRepository.placeName(context)?.lowercase().orEmpty()}",
                if (lastFetchFailed) "offline · tap and hold to change town" else "loading…"
            )
            else -> Column {
                WeatherSummary(w, lastFetchFailed)
                if (expanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    WeatherDetails(w, onChangeTown = { showTownDialog = true }, onRefresh = {
                        scope.launch { load(force = true) }
                    })
                }
            }
        }
    }

    if (showTownDialog) {
        TownDialog(
            initial = WeatherRepository.placeName(context).orEmpty(),
            onDismiss = { showTownDialog = false },
            onSave = { query, onResult ->
                scope.launch {
                    val place = WeatherRepository.setTown(context, query)
                    if (place != null) {
                        hasTown = true
                        weather = null
                        showTownDialog = false
                        load(force = true)
                    }
                    onResult(place != null)
                }
            }
        )
    }
}

@Composable
private fun WeatherMessage(title: String, message: String) {
    Column {
        Text(text = title, fontSize = 11.sp, fontFamily = Mono, color = MinimalGray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = message, fontSize = 13.sp, color = MinimalWhite)
    }
}

@Composable
private fun WeatherSummary(w: WeatherSnapshot, offline: Boolean) {
    val today = w.daily.firstOrNull()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "weather · ${w.placeName.lowercase()}" + if (offline) " · offline" else "",
                fontSize = 11.sp,
                fontFamily = Mono,
                color = MinimalGray,
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = deg(w.temperature),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = MinimalWhite
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = WeatherParser.describe(w.code, w.isDay),
                    fontSize = 13.sp,
                    color = MinimalRed,
                    modifier = Modifier.padding(bottom = 7.dp)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (today != null) {
                Text(
                    text = "↑${deg(today.max)} ↓${deg(today.min)}",
                    fontSize = 12.sp, fontFamily = Mono, color = MinimalWhite
                )
            }
            Text(text = "feels ${deg(w.feelsLike)}", fontSize = 11.sp, fontFamily = Mono, color = MinimalGray)
            today?.precipitationChance?.let {
                Text(text = "rain $it%", fontSize = 11.sp, fontFamily = Mono, color = MinimalGray)
            }
        }
    }
}

@Composable
private fun WeatherDetails(w: WeatherSnapshot, onChangeTown: () -> Unit, onRefresh: () -> Unit) {
    Column {
        Text(
            text = "wind ${w.windMph.roundToInt()} mph · humidity ${w.humidity}%",
            fontSize = 11.sp, fontFamily = Mono, color = MinimalGray
        )

        Spacer(modifier = Modifier.height(10.dp))
        SectionLabel("next 24 hours")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            w.hourly.filterIndexed { i, _ -> i % 3 == 0 }.take(8).forEachIndexed { i, h ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (i == 0) "now" else h.time.take(2),
                        fontSize = 10.sp, fontFamily = Mono, color = MinimalGray
                    )
                    Text(text = deg(h.temperature), fontSize = 13.sp, fontFamily = Mono, color = MinimalWhite)
                    Text(
                        text = h.precipitationChance?.let { "$it%" } ?: "",
                        fontSize = 9.sp, fontFamily = Mono,
                        color = if ((h.precipitationChance ?: 0) >= 50) MinimalRed else MinimalGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        SectionLabel("3 days")
        w.daily.take(3).forEachIndexed { i, d ->
            val dayName = if (i == 0) "today" else try {
                LocalDate.parse(d.date).dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.UK).lowercase()
            } catch (_: Exception) {
                d.date
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dayName, fontSize = 12.sp, fontFamily = Mono, color = MinimalWhite, modifier = Modifier.width(52.dp))
                Text(
                    text = WeatherParser.describe(d.code),
                    fontSize = 12.sp, color = MinimalGray, maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = d.precipitationChance?.let { "$it%" } ?: "",
                    fontSize = 11.sp, fontFamily = Mono, color = MinimalGray,
                    textAlign = TextAlign.End, modifier = Modifier.width(40.dp)
                )
                Text(
                    text = "${deg(d.min)} – ${deg(d.max)}",
                    fontSize = 12.sp, fontFamily = Mono, color = MinimalWhite,
                    textAlign = TextAlign.End, modifier = Modifier.width(78.dp)
                )
            }
        }

        w.daily.firstOrNull()?.let { today ->
            if (today.sunrise != null && today.sunset != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "sunrise ${today.sunrise} · sunset ${today.sunset}",
                    fontSize = 11.sp, fontFamily = Mono, color = MinimalGray
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = "updated ${SimpleDateFormat("HH:mm", Locale.UK).format(Date(w.fetchedAt))} · open-meteo",
                fontSize = 10.sp, fontFamily = Mono, color = MinimalGray
            )
            Row {
                Text(
                    text = "refresh",
                    fontSize = 10.sp, fontFamily = Mono, fontWeight = FontWeight.Bold, color = MinimalRed,
                    modifier = Modifier.clickable { onRefresh() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "change town",
                    fontSize = 10.sp, fontFamily = Mono, fontWeight = FontWeight.Bold, color = MinimalRed,
                    modifier = Modifier.clickable { onChangeTown() }
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 9.sp, fontFamily = Mono, letterSpacing = 1.sp, color = MinimalGray,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun TownDialog(
    initial: String,
    onDismiss: () -> Unit,
    onSave: (query: String, onResult: (Boolean) -> Unit) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    var busy by remember { mutableStateOf(false) }
    var notFound by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { try { focusRequester.requestFocus() } catch (_: Exception) {} }

    fun submit() {
        if (busy || text.isBlank()) return
        busy = true
        notFound = false
        onSave(text.trim()) { ok -> busy = false; notFound = !ok }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MinimalCardBg)
                .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Text(text = "your town", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MinimalWhite)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "e.g. Batley, or Batley, UK if there are others",
                fontSize = 11.sp, color = MinimalGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            BasicTextField(
                value = text,
                onValueChange = { text = it; notFound = false },
                singleLine = true,
                textStyle = TextStyle(color = MinimalWhite, fontSize = 15.sp, fontFamily = Mono),
                cursorBrush = SolidColor(MinimalRed),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            )
            if (notFound) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "couldn't find that town, or you're offline",
                    fontSize = 11.sp, color = MinimalRed
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Weather comes from open-meteo.com. It's the only thing this launcher goes online for.",
                fontSize = 10.sp, color = MinimalGray
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "CANCEL",
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalGray,
                    modifier = Modifier.clickable { onDismiss() }.padding(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (busy) "FINDING…" else "SAVE",
                    fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MinimalRed,
                    modifier = Modifier.clickable { submit() }.padding(8.dp)
                )
            }
        }
    }
}
