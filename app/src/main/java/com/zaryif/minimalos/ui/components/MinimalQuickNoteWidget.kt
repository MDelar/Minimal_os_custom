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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.domain.quicknote.QuickNoteManager
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.util.AppStrings
import com.zaryif.minimalos.util.NotificationExporter

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
            .clip(RoundedCornerShape(14.dp))
            .background(MinimalCardBg)
            .border(1.dp, MinimalDarkGray, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = MinimalRed,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.quickNoteTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalWhite,
                        letterSpacing = 0.3.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (quickNoteText.isNotBlank()) {
                        // Quick Pin Button (commits and pins immediately)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalRed.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                .clickable {
                                    QuickNoteManager.postNotification(context, quickNoteText.trim())
                                }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    tint = MinimalRed,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = AppStrings.quickNotePinBtn,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MinimalWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Quick send to AI Assistant button
                        IconButton(
                            onClick = {
                                NotificationExporter.shareTextWithAssistant(
                                    context = context,
                                    text = quickNoteText,
                                    promptPrefix = AppStrings.quickNoteAiPromptPrefix
                                )
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Summarize with AI",
                                tint = MinimalRed,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Clear Button
                        Text(
                            text = AppStrings.quickNoteClearBtn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalGray,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onClear()
                                    QuickNoteManager.cancelNotification(context)
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }

                    // Expand to Fullscreen
                    IconButton(
                        onClick = onOpenFullScreen,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Expand",
                            tint = MinimalGray,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Smooth direct text input box: hitting Enter or Done pins notification
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = 150.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                BasicTextField(
                    value = quickNoteText,
                    onValueChange = { newText ->
                        if (newText.contains("\n")) {
                            // User pressed Enter key on keyboard -> pin to status bar!
                            val cleanText = newText.replace("\n", "").trim()
                            onNoteChange(cleanText)
                            if (cleanText.isNotBlank()) {
                                QuickNoteManager.postNotification(context, cleanText)
                            } else {
                                QuickNoteManager.cancelNotification(context)
                            }
                        } else {
                            onNoteChange(newText)
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val trimmed = quickNoteText.trim()
                            if (trimmed.isNotBlank()) {
                                QuickNoteManager.postNotification(context, trimmed)
                            } else {
                                QuickNoteManager.cancelNotification(context)
                            }
                        }
                    ),
                    textStyle = TextStyle(
                        color = MinimalWhite,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(MinimalRed),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (quickNoteText.isEmpty()) {
                            Text(
                                text = AppStrings.quickNotePlaceholder,
                                color = MinimalDarkGray,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                        innerTextField()
                    }
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
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.quickNoteTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalWhite,
                        letterSpacing = (-0.2).sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (quickNoteText.isNotBlank()) {
                        // AI Summary action
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MinimalDarkCardBg)
                                .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                .clickable {
                                    NotificationExporter.shareTextWithAssistant(
                                        context = context,
                                        text = quickNoteText,
                                        promptPrefix = AppStrings.quickNoteAiPromptPrefix
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MinimalRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.quickNoteAiSummarize,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MinimalWhite
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = AppStrings.quickNoteClearBtn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalGray,
                            modifier = Modifier
                                .clickable {
                                    onClear()
                                    QuickNoteManager.cancelNotification(context)
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = AppStrings.quickNoteDoneBtn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MinimalRed,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MinimalDarkCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                            .clickable {
                                val trimmed = quickNoteText.trim()
                                if (trimmed.isNotBlank()) {
                                    QuickNoteManager.postNotification(context, trimmed)
                                } else {
                                    QuickNoteManager.cancelNotification(context)
                                }
                                onDismiss()
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The pure distraction-free text editor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalDarkCardBg.copy(alpha = 0.5f))
                    .border(1.dp, MinimalDarkGray.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                BasicTextField(
                    value = quickNoteText,
                    onValueChange = { newText ->
                        onNoteChange(newText)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val trimmed = quickNoteText.trim()
                            if (trimmed.isNotBlank()) {
                                QuickNoteManager.postNotification(context, trimmed)
                            } else {
                                QuickNoteManager.cancelNotification(context)
                            }
                        }
                    ),
                    textStyle = TextStyle(
                        color = MinimalWhite,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(MinimalRed),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        if (quickNoteText.isEmpty()) {
                            Text(
                                text = AppStrings.quickNoteSheetPlaceholder,
                                color = MinimalDarkGray,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}
