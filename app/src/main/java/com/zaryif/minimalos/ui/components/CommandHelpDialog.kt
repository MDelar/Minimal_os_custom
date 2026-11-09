package com.zaryif.minimalos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zaryif.minimalos.ui.theme.MinimalBlack
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite

@Composable
fun CommandHelpDialog(
    onDismiss: () -> Unit,
    onSelectExample: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .background(MinimalBlack)
                .border(1.dp, MinimalDarkGray, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMMAND GUIDE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalWhite
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MinimalGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanatory Intro Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "HOW THE COMMAND BOX WORKS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "The command box is an intelligent unified terminal for your phone. When you type, it evaluates input in order:\n" +
                                    "1. Custom Aliases (e.g. 'yt', 'wifi', 'gh')\n" +
                                    "2. Direct Symbols ('-', '!', '*', '+', ':', '@', '#', '?', '^', '&', '$', '%', '/')\n" +
                                    "3. Natural Language ('uninstall chatgpt', 'pomo 50m', 'yt lofi', 'set alarm 7am')\n" +
                                    "4. App Launcher (matching installed apps)\n" +
                                    "5. Seamless Web Search (searches Google automatically)",
                            fontSize = 12.sp,
                            color = MinimalWhite,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SYMBOLS & COMMAND SHORTCUTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MinimalRed,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val commands = listOf(
                    Triple("uninstall or /", "uninstall chatgpt", "Uninstalls app via system confirmation dialog"),
                    Triple("pin", "pin chrome", "Pins app shortcut to Home screen"),
                    Triple("unpin", "unpin chrome", "Removes pinned app shortcut from Home screen"),
                    Triple("yt or $", "yt lofi beats", "Directly searches in YouTube app or web"),
                    Triple("pomo or %", "pomo 50m", "Starts live home screen Pomodoro countdown timer"),
                    Triple("assistant or ?", "assistant what is gravity", "Direct query to your AI Assistant (Gemini/ChatGPT/Claude)"),
                    Triple("- or todo", "- Buy coffee beans", "Adds a focus task to your Tasks list"),
                    Triple("! or note", "! Great app idea here", "Saves a quick note to Minimal Memo"),
                    Triple("* or event", "* Lunch with Sarah tomorrow 1pm", "Creates calendar event without opening app"),
                    Triple("+ or timer", "+ 25m", "Starts countdown timer"),
                    Triple(": or alarm", ": 7:30 AM", "Sets clock alarm for tomorrow morning"),
                    Triple("@ or msg", "@ Alex running 5 mins late", "Directly opens SMS compose to contact"),
                    Triple("# or call", "# John Doe", "Dials phone number or contact immediately"),
                    Triple("& or email", "& team@minimal.io weekly sync", "Composes an email"),
                    Triple("^ or drive", "^ pitch deck", "Searches Google Drive files"),
                    Triple("help", "help", "Opens this guide anytime"),
                    Triple("app name", "Spotify", "Launches any installed application"),
                    Triple("web search", "weather this weekend", "Searches the web in default browser")
                )

                commands.forEach { (trigger, example, desc) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MinimalCardBg)
                            .border(1.dp, MinimalDarkGray, RoundedCornerShape(10.dp))
                            .clickable {
                                onSelectExample(example)
                                onDismiss()
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trigger,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalRed,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "TAP TO USE ->",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalGray
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Example: \"$example\"",
                                fontSize = 12.sp,
                                color = MinimalWhite,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 11.sp,
                                color = MinimalGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Commands explanation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MinimalDarkCardBg)
                        .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "CUSTOMIZE YOUR OWN COMMANDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You can create custom shortcuts directly in the Command Box or in Settings!\n\n" +
                                    "• Set directly: alias yt = https://youtube.com\n" +
                                    "• System settings: alias wifi = android.settings.WIFI_SETTINGS\n" +
                                    "• View all: alias list\n" +
                                    "• Delete: unalias yt\n\n" +
                                    "You can also manage them visually in Settings -> Custom Commands.",
                            fontSize = 12.sp,
                            color = MinimalWhite,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}
