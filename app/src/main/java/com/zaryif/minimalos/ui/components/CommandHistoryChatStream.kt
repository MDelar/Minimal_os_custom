package com.zaryif.minimalos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite

/**
 * Chatbox-style scrollable command history stream displayed in the upper area when typing.
 * Renders past commands as numbered squared message cards ([1], [2], [3]...) like Gemini/ChatGPT.
 * Tapping any card fills it into the command bar ready to be edited or executed.
 */
@Composable
fun CommandHistoryChatStream(
    history: List<String>,
    currentQuery: String,
    onSelectCommand: (String) -> Unit,
    onClearHistory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to top when history updates
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        // Terminal Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = MinimalRed,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TERMINAL BUFFER // HISTORY",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MinimalGray,
                    letterSpacing = 1.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${history.size} MESSAGES",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MinimalDarkGray
                )
                if (history.isNotEmpty() && onClearHistory != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear History",
                            tint = MinimalDarkGray,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (history.isEmpty()) {
            // Minimal Empty State Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MinimalDarkCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "no command history yet",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = MinimalGray
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "commands and actions you run will appear here as chat cards",
                        fontSize = 10.sp,
                        color = MinimalDarkGray
                    )
                }
            }
        } else {
            // Scrollable chat stream
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(history, key = { _, item -> item }) { index, item ->
                    val isCurrentMatch = item.equals(currentQuery.trim(), ignoreCase = true)

                    // Squared Chat Message Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCurrentMatch) MinimalCardBg else MinimalDarkCardBg)
                            .border(
                                width = if (isCurrentMatch) 1.5.dp else 1.dp,
                                color = if (isCurrentMatch) MinimalRed else MinimalDarkGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectCommand(item) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Number Badge: [1], [2], [3]...
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isCurrentMatch) MinimalRed.copy(alpha = 0.2f) else MinimalBlack)
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isCurrentMatch) MinimalRed else MinimalDarkGray,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "[${index + 1}]",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentMatch) MinimalRed else MinimalWhite
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Message command text
                            Text(
                                text = item,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Normal,
                                color = MinimalWhite,
                                modifier = Modifier.weight(1f),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Arrow hint (ready to send)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Fill to command box",
                                tint = if (isCurrentMatch) MinimalRed else MinimalGray,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
