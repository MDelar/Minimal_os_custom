package com.zaryif.minimalos.ui.components

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.domain.quicknote.QuickNoteManager
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import kotlinx.coroutines.launch

@Composable
fun MinimalHomeQuickNoteWidget(
    quickNoteText: String,
    onNoteChange: (String) -> Unit,
    onClear: () -> Unit,
    onOpenFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned Note",
                        tint = MinimalRed,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "quick note",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• pinned to bar",
                        fontSize = 10.sp,
                        color = MinimalGray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (quickNoteText.isNotBlank()) {
                        Text(
                            text = "clear",
                            fontSize = 11.sp,
                            color = MinimalGray,
                            modifier = Modifier
                                .clickable { onClear() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Expand Fullscreen",
                        tint = MinimalGray,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onOpenFullScreen() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Black space scratchpad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp, max = 130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinimalDarkCardBg.copy(alpha = 0.8f))
                    .border(1.dp, MinimalDarkGray.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                if (quickNoteText.isEmpty()) {
                    Text(
                        text = "What's on your mind? Type anything here...\nPinned to your notification bar automatically.",
                        color = MinimalDarkGray,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                BasicTextField(
                    value = quickNoteText,
                    onValueChange = { newText ->
                        onNoteChange(newText)
                        QuickNoteManager.postNotification(context, newText)
                    },
                    textStyle = TextStyle(
                        color = MinimalWhite,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(MinimalRed),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Pure distraction-free black space sheet for Quick Note
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalQuickNoteBlackSpaceSheet(
    quickNoteText: String,
    onNoteChange: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
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
                .fillMaxHeight(0.85f)
                .background(MinimalBlack)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = MinimalRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "QUICK NOTE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MinimalDarkCardBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PINNED TO STATUS BAR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MinimalRed,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (quickNoteText.isNotBlank()) {
                        Text(
                            text = "CLEAR",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalGray,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier
                                .clickable { onClear() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = "DONE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalRed,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The pure distraction-free black space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalDarkCardBg.copy(alpha = 0.4f))
                    .border(1.dp, MinimalDarkGray.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                if (quickNoteText.isEmpty()) {
                    Text(
                        text = "What's on your mind? Jot anything here...\n\nWhatever you write here stays persistently pinned to your phone's notification bar so you never forget.",
                        color = MinimalDarkGray,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }

                BasicTextField(
                    value = quickNoteText,
                    onValueChange = { newText ->
                        onNoteChange(newText)
                        QuickNoteManager.postNotification(context, newText)
                    },
                    textStyle = TextStyle(
                        color = MinimalWhite,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(MinimalRed),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                )
            }
        }
    }
}

