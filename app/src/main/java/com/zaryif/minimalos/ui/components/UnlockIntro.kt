package com.zaryif.minimalos.ui.components

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

private enum class IntroPhase { Idle, Armed, Playing }

/**
 * Terminal boot-up shown when you unlock the phone onto the home screen: your
 * header types itself out with an "unlock" command, a greeting prints, then
 * a scan line in the theme's accent wipes down to reveal home. About 0.45s;
 * tap to skip.
 *
 * Android doesn't let launchers change the lock screen, so this covers home as
 * the screen turns off (only if home was what you were looking at) and plays
 * once the phone reports it's been unlocked. Nothing runs while idle.
 */
@Composable
fun UnlockIntroOverlay(headerText: String, enabled: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var phase by remember { mutableStateOf(IntroPhase.Idle) }
    var playId by remember { mutableIntStateOf(0) }
    val currentEnabled by rememberUpdatedState(enabled)

    DisposableEffect(context, lifecycleOwner) {
        val keyguard = context.getSystemService(KeyguardManager::class.java)
        val lifecycle = lifecycleOwner.lifecycle
        var lastStoppedAt = 0L
        var waitForUnlock: Job? = null

        fun play() {
            waitForUnlock?.cancel()
            if (phase != IntroPhase.Armed) return
            phase = IntroPhase.Playing
            playId++
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    lastStoppedAt = SystemClock.elapsedRealtime()
                    waitForUnlock?.cancel()
                }
                // Home is back on screen. Play as soon as the lock screen has gone: check
                // briefly rather than waiting on the unlock broadcast, whose timing varies
                // by phone. Gives up after a few seconds so home is never left covered.
                Lifecycle.Event.ON_RESUME -> if (phase == IntroPhase.Armed) {
                    waitForUnlock?.cancel()
                    waitForUnlock = scope.launch {
                        val giveUpAt = SystemClock.elapsedRealtime() + 4000
                        while (phase == IntroPhase.Armed && SystemClock.elapsedRealtime() < giveUpAt) {
                            if (keyguard?.isKeyguardLocked != true) { play(); return@launch }
                            delay(30)
                        }
                        if (phase == IntroPhase.Armed) phase = IntroPhase.Idle
                    }
                }
                else -> {}
            }
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        val homeWasShowing = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) ||
                            SystemClock.elapsedRealtime() - lastStoppedAt < 1500
                        if (currentEnabled && homeWasShowing && animationsOn(c)) {
                            waitForUnlock?.cancel()
                            phase = IntroPhase.Armed
                        }
                    }
                    Intent.ACTION_USER_PRESENT ->
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) play()
                }
            }
        }
        lifecycle.addObserver(observer)
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose {
            waitForUnlock?.cancel()
            lifecycle.removeObserver(observer)
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        }
    }

    if (phase == IntroPhase.Idle) return

    val prompt = headerText.trim().ifEmpty { "~$" }
    val command = "unlock"
    val fullLength = prompt.length + 1 + command.length
    val typing = remember(playId) { Animatable(0f) }
    val typed = (typing.value * fullLength).toInt()
    var logLines by remember(playId) { mutableIntStateOf(0) }
    val reveal = remember(playId) { Animatable(0f) }

    if (phase == IntroPhase.Playing) {
        // About 0.45s in all: type 160ms, greet, wipe 220ms.
        LaunchedEffect(playId) {
            typing.animateTo(1f, tween(160, easing = LinearEasing))
            logLines = 1
            delay(70)
            reveal.animateTo(1f, tween(220, easing = FastOutSlowInEasing))
            phase = IntroPhase.Idle
        }
    }

    val blink by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(530, easing = LinearEasing), RepeatMode.Reverse),
        label = "cursorBlink"
    )
    val accent = MinimalRed
    val background = MinimalBlack

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { phase = IntroPhase.Idle } }
            .drawWithContent {
                val edge = size.height * reveal.value
                clipRect(top = edge) {
                    drawRect(background)
                    this@drawWithContent.drawContent()
                }
                if (reveal.value > 0f && reveal.value < 1f) {
                    val glow = 28.dp.toPx()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, accent.copy(alpha = 0.35f)),
                            startY = edge - glow,
                            endY = edge
                        ),
                        topLeft = Offset(0f, edge - glow),
                        size = Size(size.width, glow)
                    )
                    drawRect(accent, topLeft = Offset(0f, edge - 1.dp.toPx()), size = Size(size.width, 2.dp.toPx()))
                }
            }
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp)
                .padding(top = 9.dp) // lines up with the real header, which sits in a 36dp row
        ) {
            val typing = phase == IntroPhase.Playing && typed < fullLength
            val cursorAlpha = if (typing) 1f else blink
            Text(
                text = buildAnnotatedString {
                    val shownPrompt = prompt.take(typed)
                    withStyle(SpanStyle(color = MinimalWhite, fontWeight = FontWeight.Bold)) { append(shownPrompt) }
                    if (typed > prompt.length) {
                        withStyle(SpanStyle(color = accent)) { append(" " + command.take(typed - prompt.length - 1)) }
                    }
                    if (logLines == 0) {
                        withStyle(SpanStyle(color = accent.copy(alpha = cursorAlpha))) { append("█") }
                    }
                },
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (logLines >= 1) LogLine(greeting(prompt), accent)
        }
    }
}

@Composable
private fun LogLine(text: String, accent: Color) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MinimalGray)) { append("[ ") }
            withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold)) { append("ok") }
            withStyle(SpanStyle(color = MinimalGray)) { append(" ] $text") }
        },
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        maxLines = 1,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

/** "good evening, mdelar" from a header like "mdelar@titan:~$". */
internal fun greeting(header: String, now: LocalTime = LocalTime.now()): String {
    val part = when (now.hour) {
        in 5..11 -> "good morning"
        in 12..16 -> "good afternoon"
        in 17..21 -> "good evening"
        else -> "up late"
    }
    val user = header.substringBefore('@', missingDelimiterValue = "")
        .trim().takeIf { it.isNotEmpty() && it.length <= 16 && it.none(Char::isWhitespace) }
    return if (user != null) "$part, $user" else part
}

// Respect "Remove animations" / animator scale 0 in developer or accessibility settings.
private fun animationsOn(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
