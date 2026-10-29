package com.zaryif.minimalos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaryif.minimalos.domain.model.AppInfo
import com.zaryif.minimalos.domain.model.CommandType
import com.zaryif.minimalos.domain.model.Contact
import com.zaryif.minimalos.domain.parser.CommandParser
import com.zaryif.minimalos.ui.theme.MinimalCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkCardBg
import com.zaryif.minimalos.ui.theme.MinimalDarkGray
import com.zaryif.minimalos.ui.theme.MinimalGray
import com.zaryif.minimalos.ui.theme.MinimalRed
import com.zaryif.minimalos.ui.theme.MinimalWhite
import com.zaryif.minimalos.ui.theme.MinimalOnAccent

private val BUILTIN_ACTION_KEYWORDS = setOf(
    "todo", "task",
    "note", "notes", "memo",
    "call", "dial",
    "msg", "sms", "message", "text",
    "wa", "whatsapp",
    "alarm", "timer", "pomodoro", "pomo", "event", "cal", "calendar", "schedule",
    "email", "mail", "drive",
    "assistant", "ai", "ask", "search", "google", "find",
    "yt", "youtube",
    "uninstall", "remove", "rmapp",
    "help", "commands", "man", "guide",
    "alias", "custom", "setalias", "unalias", "rmalias"
)

@Composable
fun CommandBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onExecute: (String) -> Unit,
    matchingApps: List<AppInfo>,
    onAppSelected: (AppInfo) -> Unit,
    matchingContacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    commandHistory: List<String> = emptyList(),
    isHistoryEnabled: Boolean = true,
    customAliases: Set<String> = emptySet(),
    hasContactPermission: Boolean = true,
    onRequestContactPermission: (() -> Unit)? = null,
    onHelpRequested: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val command = CommandParser.parse(query)
    val hasPrefix = query.isNotBlank() && CommandType.fromSymbol(query.trim().first()) != null
    val isCallingOrMessaging = command.type == CommandType.CALL ||
            command.type == CommandType.MESSAGE ||
            command.type == CommandType.WHATSAPP

    val trimmed = query.trim()
    val lower = trimmed.lowercase()
    val firstToken = lower.split(" ").firstOrNull() ?: ""

    // 1. Alias commands: alias, custom, setalias, unalias, rmalias, or user defined aliases
    val isAliasCommand = trimmed.isNotEmpty() && (
        firstToken in listOf("alias", "custom", "setalias", "unalias", "rmalias") ||
        firstToken in customAliases
    )

    // 2. Built-in actions (TODO, NOTE, CALL, MESSAGE, WHATSAPP, ALARM, TIMER, EVENT, EMAIL, DRIVE, ASK, HELP)
    val isBuiltInAction = command.type != CommandType.SEARCH || hasPrefix ||
            lower.let {
                it == "todo" || it.startsWith("todo ") ||
                it == "note" || it.startsWith("note ") ||
                it == "notes" || it.startsWith("notes ") ||
                it == "task" || it.startsWith("task ") ||
                it == "memo" || it.startsWith("memo ") ||
                it == "call" || it.startsWith("call ") ||
                it == "dial" || it.startsWith("dial ") ||
                it == "wa" || it.startsWith("wa ") ||
                it == "whatsapp" || it.startsWith("whatsapp ") ||
                it == "msg" || it.startsWith("msg ") ||
                it == "sms" || it.startsWith("sms ") ||
                it == "text" || it.startsWith("text ") ||
                it == "message" || it.startsWith("message ") ||
                it.startsWith("alarm ") || it.startsWith("set alarm ") ||
                it == "timer" || it.startsWith("timer ") || it.startsWith("set timer ") ||
                it == "pomodoro" || it.startsWith("pomodoro ") ||
                it == "pomo" || it.startsWith("pomo ") ||
                it.startsWith("event ") || it.startsWith("cal ") || it.startsWith("calendar ") || it.startsWith("schedule ") ||
                it.startsWith("email ") || it.startsWith("mail ") ||
                it.startsWith("drive ") ||
                it == "assistant" || it.startsWith("assistant ") ||
                it == "ai" || it.startsWith("ai ") ||
                it == "yt" || it.startsWith("yt ") ||
                it == "youtube" || it.startsWith("youtube ") ||
                it == "uninstall" || it.startsWith("uninstall ") || it == "remove" || it.startsWith("remove ") || it == "rmapp" || it.startsWith("rmapp ") ||
                it.startsWith("ask ") || it.startsWith("search ") || it.startsWith("google ") || it.startsWith("find ") ||
                it == "help" || it.startsWith("help ") || it == "commands" || it == "man" || it == "guide"
            }

    // Action detection
    val isActionTriggered = isBuiltInAction || isAliasCommand

    // Inline syntax highlighting: only the action keyword, alias, or trigger symbol is colored in the theme accent
    val actionSyntaxVisualTransformation = remember(customAliases) {
        VisualTransformation { text ->
            val s = text.text
            val trimmedInput = s.trimStart()
            val leadingSpaces = s.length - trimmedInput.length

            var triggerEnd = -1

            if (trimmedInput.isNotEmpty()) {
                val firstChar = trimmedInput[0]
                if (firstChar in setOf('-', '!', '#', '@', '=', '+', ':', '*', '?', '^', '&', '$', '%', '/', '~')) {
                    triggerEnd = leadingSpaces + 1
                } else if (trimmedInput.lowercase().startsWith("set alarm ") || trimmedInput.lowercase() == "set alarm") {
                    triggerEnd = leadingSpaces + 9
                } else if (trimmedInput.lowercase().startsWith("set timer ") || trimmedInput.lowercase() == "set timer") {
                    triggerEnd = leadingSpaces + 9
                } else {
                    val firstWord = trimmedInput.substringBefore(" ").lowercase()
                    val isKnownAction = firstWord in BUILTIN_ACTION_KEYWORDS || customAliases.any { it.equals(firstWord, ignoreCase = true) }
                    if (isKnownAction && firstWord.isNotEmpty()) {
                        triggerEnd = leadingSpaces + firstWord.length
                    }
                }
            }

            val builder = AnnotatedString.Builder()
            if (triggerEnd in 1..s.length) {
                // Highlight ONLY the trigger keyword/symbol in theme accent color (bold)
                builder.pushStyle(SpanStyle(color = MinimalRed, fontWeight = FontWeight.Bold))
                builder.append(s.substring(0, triggerEnd))
                builder.pop()

                // Keep parameters / payload text in clean crisp white
                if (triggerEnd < s.length) {
                    builder.pushStyle(SpanStyle(color = MinimalWhite, fontWeight = FontWeight.Normal))
                    builder.append(s.substring(triggerEnd))
                    builder.pop()
                }
            } else {
                builder.pushStyle(SpanStyle(color = MinimalWhite, fontWeight = FontWeight.Normal))
                builder.append(s)
                builder.pop()
            }

            TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
        }
    }

    // Terminal command history state
    var historyIndex by remember { mutableIntStateOf(-1) }
    var tempQueryBeforeHistory by remember { mutableStateOf("") }

    LaunchedEffect(query, commandHistory) {
        val trimmedQ = query.trim()
        val found = commandHistory.indexOfFirst { it.equals(trimmedQ, ignoreCase = true) }
        if (found != -1) {
            historyIndex = found
        }
    }

    fun navigateHistory(up: Boolean) {
        if (!isHistoryEnabled || commandHistory.isEmpty()) return
        if (up) {
            if (historyIndex == -1) {
                tempQueryBeforeHistory = query
            }
            if (historyIndex < commandHistory.size - 1) {
                historyIndex++
                onQueryChange(commandHistory[historyIndex])
            }
        } else {
            if (historyIndex > 0) {
                historyIndex--
                onQueryChange(commandHistory[historyIndex])
            } else if (historyIndex == 0) {
                historyIndex = -1
                onQueryChange(tempQueryBeforeHistory)
            }
        }
    }

    fun submitCommand(input: String) {
        keyboardController?.hide()
        historyIndex = -1
        onExecute(input)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // 1. Contacts auto-complete suggestions if calling/messaging/whatsapp
        val showContactSuggestions = isCallingOrMessaging && !query.contains(":") &&
                (matchingContacts.isNotEmpty() || !hasContactPermission || query.isNotBlank())

        AnimatedVisibility(
            visible = showContactSuggestions,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalCardBg)
                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(12.dp))
                    .heightIn(max = 135.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (!hasContactPermission) {
                    // Contact permission request card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRequestContactPermission?.invoke() }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MinimalRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Contacts Permission Required",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalWhite
                            )
                            Text(
                                text = "Tap here to grant access for @messages and #calls",
                                fontSize = 11.sp,
                                color = MinimalGray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MinimalRed)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "GRANT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalOnAccent
                            )
                        }
                    }
                } else if (matchingContacts.isEmpty() && query.trim().isNotEmpty()) {
                    val searchName = when {
                        query.trim().startsWith("#") || query.trim().startsWith("@") || query.trim().startsWith("=") -> query.trim().drop(1).trim()
                        query.trim().lowercase().startsWith("call ") -> query.trim().substring(5).trim()
                        query.trim().lowercase().startsWith("dial ") -> query.trim().substring(5).trim()
                        query.trim().lowercase().startsWith("whatsapp ") -> query.trim().substring(9).trim()
                        query.trim().lowercase().startsWith("wa ") -> query.trim().substring(3).trim()
                        query.trim().lowercase().startsWith("msg ") -> query.trim().substring(4).trim()
                        query.trim().lowercase().startsWith("sms ") -> query.trim().substring(4).trim()
                        query.trim().lowercase().startsWith("message ") -> query.trim().substring(8).trim()
                        else -> query.trim()
                    }
                    val noFoundText = if (searchName.isNotEmpty()) "No contacts found matching \"$searchName\"" else "No contacts found"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MinimalGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = noFoundText,
                            fontSize = 12.sp,
                            color = MinimalGray
                        )
                    }
                } else {
                    matchingContacts.take(5).forEach { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onContactSelected(contact)
                                }
                                .padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MinimalRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MinimalWhite
                                )
                                Text(
                                    text = contact.phoneNumber,
                                    fontSize = 11.sp,
                                    color = MinimalGray
                                )
                            }

                            // WhatsApp shortcut chip
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                    .clickable { onQueryChange("wa ${contact.name}: ") }
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "WhatsApp",
                                    tint = MinimalRed,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "WA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MinimalWhite)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // SMS shortcut chip
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalDarkGray, RoundedCornerShape(6.dp))
                                    .clickable { onQueryChange("@ ${contact.name}: ") }
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "SMS",
                                    tint = MinimalRed,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "SMS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MinimalWhite)
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Call direct button
                            IconButton(
                                onClick = { submitCommand("#${contact.phoneNumber}") },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Call",
                                    tint = MinimalRed,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. App auto-complete suggestions if searching apps or uninstalling
        val isUninstallMode = lower.let {
            it.startsWith("uninstall") || it.startsWith("remove") || it.startsWith("rmapp")
        } || query.startsWith("/")
        val showAppSuggestions = query.isNotBlank() && !isCallingOrMessaging && matchingApps.isNotEmpty() &&
                (!hasPrefix || query.startsWith("/"))

        AnimatedVisibility(
            visible = showAppSuggestions,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MinimalCardBg)
                    .border(1.dp, if (isUninstallMode) MinimalRed.copy(alpha = 0.5f) else MinimalDarkGray, RoundedCornerShape(12.dp))
                    .heightIn(max = 145.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                matchingApps.take(4).forEach { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                keyboardController?.hide()
                                onAppSelected(app)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = app.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalWhite,
                            modifier = Modifier.weight(1f)
                        )
                        if (isUninstallMode) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MinimalDarkCardBg)
                                    .border(1.dp, MinimalRed, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "UNINSTALL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalRed
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open app",
                                tint = MinimalGray,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Command Bar Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MinimalCardBg)
                .border(1.dp, if (isActionTriggered) MinimalRed.copy(alpha = 0.5f) else MinimalDarkGray, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Terminal History Indicator Badge
                if (isHistoryEnabled && historyIndex >= 0 && commandHistory.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MinimalDarkCardBg)
                            .border(0.5.dp, MinimalDarkGray, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "HIST ${historyIndex + 1}/${commandHistory.size}",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MinimalRed
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Text field with action syntax highlighting and keyboard terminal arrow intercept
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 24.dp, max = 80.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "type to do things or search…",
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = MinimalGray,
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = {
                            historyIndex = -1
                            onQueryChange(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .onPreviewKeyEvent { keyEvent ->
                                if (isHistoryEnabled && keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionUp -> {
                                            navigateHistory(up = true)
                                            true
                                        }
                                        Key.DirectionDown -> {
                                            navigateHistory(up = false)
                                            true
                                        }
                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            },
                        singleLine = false,
                        maxLines = 3,
                        cursorBrush = SolidColor(MinimalRed),
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MinimalWhite,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 20.sp
                        ),
                        visualTransformation = actionSyntaxVisualTransformation,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = { submitCommand(query) }
                        )
                    )
                }

                // Terminal history Up / Down cycling buttons
                if (isHistoryEnabled && commandHistory.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { navigateHistory(up = true) },
                            modifier = Modifier.size(26.dp),
                            enabled = historyIndex < commandHistory.size - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Previous Command",
                                tint = if (historyIndex < commandHistory.size - 1) MinimalWhite else MinimalDarkGray,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        IconButton(
                            onClick = { navigateHistory(up = false) },
                            modifier = Modifier.size(26.dp),
                            enabled = historyIndex >= 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Next Command",
                                tint = if (historyIndex >= 0) MinimalWhite else MinimalDarkGray,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                // Help or Clear / Execute buttons
                if (query.isEmpty() && onHelpRequested != null) {
                    IconButton(
                        onClick = onHelpRequested,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Commands Help",
                            tint = MinimalGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else if (query.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            historyIndex = -1
                            onQueryChange("")
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MinimalGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    IconButton(
                        onClick = { submitCommand(query) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Execute",
                            tint = MinimalRed,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}
