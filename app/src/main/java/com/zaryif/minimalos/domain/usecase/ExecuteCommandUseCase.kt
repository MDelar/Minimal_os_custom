package com.zaryif.minimalos.domain.usecase

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.zaryif.minimalos.util.AlarmClockPermissionHelper
import com.zaryif.minimalos.data.preferences.AiProvider
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.data.repository.AppRepository
import com.zaryif.minimalos.data.repository.ContactRepository
import com.zaryif.minimalos.data.repository.StorageRepository
import com.zaryif.minimalos.domain.model.CommandType
import com.zaryif.minimalos.domain.parser.CommandParser
import com.zaryif.minimalos.domain.parser.NaturalDateTimeParser
import com.zaryif.minimalos.domain.timer.PomodoroTimerManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.regex.Pattern

sealed class CommandResult {
    data class Success(val message: String) : CommandResult()
    data class LaunchedApp(val appName: String) : CommandResult()
    data class ShowHelp(val commands: List<Pair<String, String>>) : CommandResult()
    data class OpenQuickNote(val initialText: String? = null) : CommandResult()
    data class Error(val error: String) : CommandResult()
}

class ExecuteCommandUseCase(
    private val context: Context,
    private val appRepository: AppRepository,
    private val storageRepository: StorageRepository,
    private val contactRepository: ContactRepository,
    private val userPreferences: UserPreferences
) {

    fun execute(rawInput: String): CommandResult {
        if (rawInput.isBlank()) {
            return CommandResult.Error("Empty command")
        }

        val trimmed = rawInput.trim()

        // 1. Check custom commands/aliases first
        val customMap = runBlocking {
            try {
                userPreferences.customCommands.first()
            } catch (_: Exception) {
                emptyMap()
            }
        }
        val firstToken = trimmed.split(" ").first().lowercase()
        val remainingArgs = if (trimmed.contains(" ")) trimmed.substringAfter(" ").trim() else ""

        // 1a. Create, list, or delete custom aliases directly in command box!
        if (firstToken == "alias" || firstToken == "custom" || firstToken == "setalias") {
            return handleAliasCommand(remainingArgs)
        }
        if (firstToken == "unalias" || firstToken == "rmalias") {
            return handleUnaliasCommand(remainingArgs)
        }

        // 1b. Check user-defined custom commands/aliases
        if (customMap.containsKey(firstToken)) {
            val target = customMap[firstToken] ?: ""
            return executeCustomCommand(target, remainingArgs)
        }

        val command = CommandParser.parse(rawInput)

        return when (command.type) {
            CommandType.MESSAGE -> handleMessage(command.argument)
            CommandType.WHATSAPP -> handleWhatsapp(command.argument)
            CommandType.CALL -> handleCall(command.argument)
            CommandType.TODO -> handleTodo(command.argument)
            CommandType.NOTE -> handleNote(command.argument)
            CommandType.EVENT -> handleEvent(command.argument)
            CommandType.TIMER -> handleTimer(command.argument)
            CommandType.POMODORO -> handlePomodoro(command.argument)
            CommandType.ALARM -> handleAlarm(command.argument)
            CommandType.ASK -> handleAsk(command.argument)
            CommandType.YOUTUBE -> handleYouTube(command.argument)
            CommandType.UNINSTALL -> handleUninstall(command.argument)
            CommandType.PIN -> handlePin(command.argument)
            CommandType.UNPIN -> handleUnpin(command.argument)
            CommandType.QUICK_NOTE -> handleQuickNote(command.argument)
            CommandType.DRIVE -> handleDrive(command.argument)
            CommandType.EMAIL -> handleEmail(command.argument)
            CommandType.HELP -> handleHelp()
            CommandType.SEARCH -> handleAppSearchOrWebFallback(command.argument)
        }
    }

    private data class ResolvedMessageTarget(
        val targetNumber: String,
        val recipientName: String,
        val body: String
    )

    private fun resolveMessageTarget(arg: String): ResolvedMessageTarget {
        if (arg.isBlank()) return ResolvedMessageTarget("", "", "")

        // Case 1: Explicit colon separator (e.g. "@John Doe: on my way" or "wa Sarah: hello")
        if (arg.contains(":")) {
            val contactQuery = arg.substringBefore(":").trim()
            val body = arg.substringAfter(":").trim()
            val matched = contactRepository.searchContacts(contactQuery)
            val best = matched.firstOrNull()
            return if (best != null) {
                ResolvedMessageTarget(best.phoneNumber, best.name, body)
            } else if (contactQuery.any { it.isDigit() }) {
                ResolvedMessageTarget(contactQuery, contactQuery, body)
            } else {
                ResolvedMessageTarget("", contactQuery, body)
            }
        }

        val words = arg.split(Regex("\\s+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return ResolvedMessageTarget("", "", "")

        if (words.size == 1) {
            val single = words[0]
            val isDigits = single.all { it.isDigit() || it == '+' || it == '-' }
            if (isDigits && single.length >= 3) {
                return ResolvedMessageTarget(single, single, "")
            }
            val matched = contactRepository.searchContacts(single)
            val best = matched.firstOrNull()
            return if (best != null) {
                ResolvedMessageTarget(best.phoneNumber, best.name, "")
            } else {
                ResolvedMessageTarget("", "", single)
            }
        }

        // Case 2: Multi-word greedy resolution (from longest recipient candidate down to 1 word)
        // E.g., for "@John Doe on my way", checks "John Doe" (k=2) first and matches John Doe!
        val maxWordsToCheck = minOf(words.size - 1, 4)
        for (k in maxWordsToCheck downTo 1) {
            val candidateName = words.take(k).joinToString(" ")
            val candidates = contactRepository.searchContacts(candidateName)
            val candLower = candidateName.lowercase()
            val highConfidence = candidates.firstOrNull { c ->
                val cLower = c.name.lowercase()
                cLower == candLower ||
                cLower.startsWith(candLower) ||
                cLower.split(Regex("\\s+")).any { it == candLower }
            }
            if (highConfidence != null) {
                val body = words.drop(k).joinToString(" ")
                return ResolvedMessageTarget(highConfidence.phoneNumber, highConfidence.name, body)
            }
        }

        // Case 3: First token is a phone number (e.g. "@+1234567890 hello")
        val firstToken = words[0]
        if (firstToken.all { it.isDigit() || it == '+' || it == '-' } && firstToken.length >= 3) {
            val body = words.drop(1).joinToString(" ")
            return ResolvedMessageTarget(firstToken, firstToken, body)
        }

        // Case 4: Fallback to single token search
        val singleMatch = contactRepository.searchContacts(firstToken).firstOrNull()
        if (singleMatch != null) {
            val body = words.drop(1).joinToString(" ")
            return ResolvedMessageTarget(singleMatch.phoneNumber, singleMatch.name, body)
        }

        // Default: entire input treated as body
        return ResolvedMessageTarget("", "", arg)
    }

    private fun handleMessage(arg: String): CommandResult {
        if (arg.isBlank()) return CommandResult.Error("Enter recipient or message")

        val target = resolveMessageTarget(arg)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = if (target.targetNumber.isNotEmpty()) {
                Uri.parse("smsto:${target.targetNumber}")
            } else {
                Uri.parse("smsto:")
            }
            if (target.body.isNotBlank()) {
                putExtra("sms_body", target.body)
            }
        }
        return try {
            context.startActivity(intent)
            val desc = if (target.recipientName.isNotBlank()) "Opening SMS to ${target.recipientName}…" else "Opening messages…"
            CommandResult.Success(desc)
        } catch (e: Exception) {
            CommandResult.Error("Could not open messages: ${e.message}")
        }
    }

    private fun handleWhatsapp(arg: String): CommandResult {
        if (arg.isBlank()) return CommandResult.Error("Enter contact or message for WhatsApp")

        val target = resolveMessageTarget(arg)

        val digits = target.targetNumber.filter { it.isDigit() || it == '+' }
        val url = if (digits.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$digits&text=${Uri.encode(target.body)}"
        } else {
            "https://api.whatsapp.com/send?text=${Uri.encode(target.body)}"
        }

        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.whatsapp")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(waIntent)
            val desc = if (target.recipientName.isNotBlank()) "Opening WhatsApp to ${target.recipientName}…" else "Opening WhatsApp…"
            CommandResult.Success(desc)
        } catch (_: Exception) {
            val generalIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(generalIntent)
                CommandResult.Success("Opening WhatsApp…")
            } catch (e: Exception) {
                CommandResult.Error("Could not launch WhatsApp: ${e.message}")
            }
        }
    }

    private fun handleCall(arg: String): CommandResult {
        if (arg.isBlank()) return CommandResult.Error("Enter contact name or phone number")

        var phoneNumber = arg.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }

        if (phoneNumber.length < 3) {
            val contacts = contactRepository.searchContacts(arg)
            val best = contacts.firstOrNull()
            if (best != null) {
                phoneNumber = best.phoneNumber
            }
        }

        if (phoneNumber.isBlank()) {
            return CommandResult.Error("No contact found for '$arg'")
        }

        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val intent = if (hasCallPermission) {
            Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
        } else {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return try {
            context.startActivity(intent)
            CommandResult.Success("Calling $phoneNumber…")
        } catch (e: Exception) {
            CommandResult.Error("Could not place call: ${e.message}")
        }
    }

    private fun handleTodo(arg: String): CommandResult {
        if (arg.isBlank()) return CommandResult.Error("Enter task")
        storageRepository.addTodo(arg)
        return CommandResult.Success("Saved task: $arg")
    }

    private fun handleNote(arg: String): CommandResult {
        if (arg.isBlank()) return CommandResult.Error("Enter note content")

        storageRepository.addNote(arg)

        // Google Keep instant sync
        val keepPackage = "com.google.android.keep"
        val pm = context.packageManager
        val isKeepInstalled = try {
            pm.getPackageInfo(keepPackage, 0)
            true
        } catch (_: Exception) {
            false
        }

        if (isKeepInstalled) {
            try {
                val keepIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage(keepPackage)
                    putExtra(Intent.EXTRA_TEXT, arg)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(keepIntent)
                return CommandResult.Success("Saved note & synced with Google Keep")
            } catch (_: Exception) {}
        }

        return CommandResult.Success("Saved note: $arg")
    }

    private fun handleQuickNote(arg: String): CommandResult {
        if (arg.isBlank()) {
            return CommandResult.OpenQuickNote()
        }
        kotlinx.coroutines.runBlocking {
            userPreferences.setQuickNote(arg)
        }
        com.zaryif.minimalos.domain.quicknote.QuickNoteManager.postNotification(context, arg)
        return CommandResult.Success("Pinned to notification bar: $arg")
    }

    /**
     * Enhanced Event Handler: Natural time parser parses phrases like:
     * "team standup at 4pm", "lunch at 12:30", "gym tomorrow at 8am"
     * and sets DTSTART & DTEND accurately!
     */
    private fun handleEvent(arg: String): CommandResult {
        if (arg.isBlank()) return CommandResult.Error("Enter event title")

        val parsed = NaturalDateTimeParser.parse(arg, "Event")
        val cleanTitle = parsed.cleanTitle
        val startMillis = parsed.startMillis
        val endMillis = startMillis + (60 * 60 * 1000) // Default 1 hour duration

        val hasCalendarPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        // Direct silent event insertion without needing user to click "done"
        if (hasCalendarPermission) {
            try {
                var calendarId = 1L
                val calCursor = context.contentResolver.query(
                    CalendarContract.Calendars.CONTENT_URI,
                    arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY),
                    null,
                    null,
                    null
                )
                calCursor?.use {
                    if (it.moveToFirst()) {
                        calendarId = it.getLong(0)
                    }
                }

                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, startMillis)
                    put(CalendarContract.Events.DTEND, endMillis)
                    put(CalendarContract.Events.TITLE, cleanTitle)
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }
                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                if (uri != null) {
                    val timeStr = "%02d:%02d".format(
                        parsed.calendar.get(Calendar.HOUR_OF_DAY),
                        parsed.calendar.get(Calendar.MINUTE)
                    )
                    return CommandResult.Success("Event '$cleanTitle' at $timeStr set in Calendar")
                }
            } catch (_: Exception) {}
        }

        // Fallback: Open pre-filled Google Calendar with accurate start and end millis
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, cleanTitle)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            setPackage("com.google.android.calendar")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            CommandResult.Success("Opening Google Calendar for '$cleanTitle'")
        } catch (_: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, cleanTitle)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallbackIntent)
                CommandResult.Success("Creating event: $cleanTitle")
            } catch (e: Exception) {
                CommandResult.Error("Could not open calendar: ${e.message}")
            }
        }
    }

    private fun parseNaturalDurationSeconds(arg: String, defaultMinutes: Int): Int {
        val clean = arg.lowercase(Locale.getDefault()).trim()
        if (clean.isEmpty()) return defaultMinutes * 60

        val hourMatch = Regex("(\\d+)\\s*(?:h|hr|hrs|hour|hours)\\b").find(clean)
        val minMatch = Regex("(\\d+)\\s*(?:m|min|mins|minute|minutes)\\b").find(clean)
        val secMatch = Regex("(\\d+)\\s*(?:s|sec|secs|second|seconds)\\b").find(clean)

        var totalSeconds = 0
        var matchedUnit = false

        if (hourMatch != null) {
            totalSeconds += (hourMatch.groupValues[1].toIntOrNull() ?: 0) * 3600
            matchedUnit = true
        }
        if (minMatch != null) {
            totalSeconds += (minMatch.groupValues[1].toIntOrNull() ?: 0) * 60
            matchedUnit = true
        }
        if (secMatch != null) {
            totalSeconds += (secMatch.groupValues[1].toIntOrNull() ?: 0)
            matchedUnit = true
        }

        if (matchedUnit && totalSeconds > 0) {
            return totalSeconds
        }

        val digits = clean.filter { it.isDigit() }
        if (digits.isNotEmpty()) {
            val num = digits.toIntOrNull() ?: defaultMinutes
            return num * 60
        }

        return defaultMinutes * 60
    }

    private fun handlePomodoro(arg: String): CommandResult {
        val seconds = parseNaturalDurationSeconds(arg, defaultMinutes = 25)
        PomodoroTimerManager.start(seconds, "pomodoro")

        val minutes = seconds / 60
        val remainingSec = seconds % 60
        val timeLabel = if (remainingSec > 0) "${minutes}m ${remainingSec}s" else "${minutes}m"

        AlarmClockPermissionHelper.setTimer(context, seconds, "Pomodoro Focus")

        return CommandResult.Success("Live Pomodoro started: $timeLabel focus")
    }

    private fun handleTimer(arg: String): CommandResult {
        if (arg.lowercase(Locale.getDefault()).contains("pomo")) {
            return handlePomodoro(arg)
        }
        val seconds = parseNaturalDurationSeconds(arg, defaultMinutes = 5)
        PomodoroTimerManager.start(seconds, "timer")

        val minutes = seconds / 60
        val remainingSec = seconds % 60
        val timeLabel = if (remainingSec > 0) "${minutes}m ${remainingSec}s" else "${minutes}m"

        AlarmClockPermissionHelper.setTimer(context, seconds, "Minimal Timer")

        return CommandResult.Success("Live timer started: $timeLabel")
    }

    private fun handleAlarm(arg: String): CommandResult {
        val parsed = NaturalDateTimeParser.parse(arg, "Alarm")
        val hour = parsed.hour
        val minute = parsed.minute

        val result = AlarmClockPermissionHelper.setAlarm(context, hour, minute, "Minimal Alarm")
        return if (result.isSuccess) {
            CommandResult.Success("Alarm set for %02d:%02d".format(hour, minute))
        } else {
            CommandResult.Error("Could not set alarm: ${result.exceptionOrNull()?.message}")
        }
    }

    private fun handleDrive(arg: String): CommandResult {
        val drivePackage = "com.google.android.apps.docs"
        val query = if (arg.isBlank()) "" else arg
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://drive.google.com/drive/search?q=${Uri.encode(query)}")
            setPackage(drivePackage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            CommandResult.Success("Searching Drive for '$query'")
        } catch (_: Exception) {
            val fallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://drive.google.com/drive/search?q=${Uri.encode(query)}")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            try {
                context.startActivity(fallback)
                CommandResult.Success("Opening Drive search")
            } catch (e: Exception) {
                CommandResult.Error("Could not open Drive: ${e.message}")
            }
        }
    }

    private fun handleEmail(arg: String): CommandResult {
        val gmailPackage = "com.google.android.gm"
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_SUBJECT, arg)
            setPackage(gmailPackage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(intent)
            CommandResult.Success("Opening Gmail compose…")
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(fallback)
                CommandResult.Success("Opening email client…")
            } catch (e: Exception) {
                CommandResult.Error("Could not open email: ${e.message}")
            }
        }
    }

    private fun handleUninstall(arg: String): CommandResult {
        val appQuery = arg.trim()
        if (appQuery.isEmpty()) {
            return CommandResult.Error("Usage: uninstall <app_name>\nExample: uninstall chatgpt")
        }

        val installedApps = appRepository.getInstalledApps()
        val match = installedApps.find { it.label.equals(appQuery, ignoreCase = true) }
            ?: installedApps.find { it.packageName.equals(appQuery, ignoreCase = true) }
            ?: installedApps.find { it.label.lowercase(Locale.getDefault()).startsWith(appQuery.lowercase(Locale.getDefault())) }
            ?: installedApps.find { it.label.lowercase(Locale.getDefault()).contains(appQuery.lowercase(Locale.getDefault())) }

        if (match == null) {
            return CommandResult.Error("App \"$appQuery\" not found on this device")
        }

        if (match.isSystemApp) {
            val settingsIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", match.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(settingsIntent)
                return CommandResult.Success("${match.label} is a system app. Opening App Info to disable.")
            } catch (e: Exception) {
                return CommandResult.Error("Cannot uninstall system app ${match.label}")
            }
        }

        val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", match.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }

        return try {
            context.startActivity(uninstallIntent)
            CommandResult.Success("Uninstalling ${match.label}…")
        } catch (e: Exception) {
            try {
                @Suppress("DEPRECATION")
                val legacyIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                    data = Uri.fromParts("package", match.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                }
                context.startActivity(legacyIntent)
                CommandResult.Success("Uninstalling ${match.label}…")
            } catch (e2: Exception) {
                try {
                    val settingsIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", match.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    CommandResult.Success("Opening App Info to uninstall ${match.label}")
                } catch (e3: Exception) {
                    CommandResult.Error("Could not start uninstall: ${e.message}")
                }
            }
        }
    }

    private fun handlePin(arg: String): CommandResult {
        val appQuery = arg.trim()
        if (appQuery.isEmpty()) {
            return CommandResult.Error("Usage: pin <app_name>\nExample: pin chrome")
        }

        val installedApps = appRepository.getInstalledApps()
        val match = installedApps.find { it.label.equals(appQuery, ignoreCase = true) }
            ?: installedApps.find { it.packageName.equals(appQuery, ignoreCase = true) }
            ?: installedApps.find { it.label.lowercase(Locale.getDefault()).startsWith(appQuery.lowercase(Locale.getDefault())) }
            ?: installedApps.find { it.label.lowercase(Locale.getDefault()).contains(appQuery.lowercase(Locale.getDefault())) }

        if (match == null) {
            return CommandResult.Error("App \"$appQuery\" not found on this device")
        }

        return runBlocking {
            val currentFavs = try {
                userPreferences.favoriteApps.first()
            } catch (_: Exception) {
                emptySet()
            }

            if (match.packageName in currentFavs) {
                CommandResult.Success("${match.label} is already pinned to Home")
            } else {
                val updated = currentFavs + match.packageName
                userPreferences.setFavoriteApps(updated)

                // Ensure "SHORTCUTS" widget is active on Home screen so the pinned shortcut is immediately visible!
                try {
                    val currentHomeWidgets = userPreferences.homeWidgets.first()
                    if ("SHORTCUTS" !in currentHomeWidgets) {
                        userPreferences.setHomeWidgets(currentHomeWidgets + "SHORTCUTS")
                    }
                } catch (_: Exception) {}

                CommandResult.Success("Pinned ${match.label} to Home")
            }
        }
    }

    private fun handleUnpin(arg: String): CommandResult {
        val appQuery = arg.trim()
        if (appQuery.isEmpty()) {
            return CommandResult.Error("Usage: unpin <app_name>\nExample: unpin chrome")
        }

        val installedApps = appRepository.getInstalledApps()
        val match = installedApps.find { it.label.equals(appQuery, ignoreCase = true) }
            ?: installedApps.find { it.packageName.equals(appQuery, ignoreCase = true) }
            ?: installedApps.find { it.label.lowercase(Locale.getDefault()).startsWith(appQuery.lowercase(Locale.getDefault())) }
            ?: installedApps.find { it.label.lowercase(Locale.getDefault()).contains(appQuery.lowercase(Locale.getDefault())) }

        if (match == null) {
            return CommandResult.Error("App \"$appQuery\" not found on this device")
        }

        return runBlocking {
            val currentFavs = try {
                userPreferences.favoriteApps.first()
            } catch (_: Exception) {
                emptySet()
            }

            if (match.packageName !in currentFavs) {
                CommandResult.Success("${match.label} is not pinned to Home")
            } else {
                val updated = currentFavs - match.packageName
                userPreferences.setFavoriteApps(updated)
                CommandResult.Success("Unpinned ${match.label} from Home")
            }
        }
    }

    private fun handleYouTube(arg: String): CommandResult {
        val query = arg.trim()
        val pm = context.packageManager

        if (query.isEmpty()) {
            val launchIntent = pm.getLaunchIntentForPackage("com.google.android.youtube")
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return CommandResult.Success("Opening YouTube…")
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                return CommandResult.Success("Opening YouTube…")
            }
        }

        // 1. YouTube ACTION_SEARCH intent directly targeting YouTube package
        val searchIntent = Intent(Intent.ACTION_SEARCH).apply {
            `package` = "com.google.android.youtube"
            putExtra("query", query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (pm.resolveActivity(searchIntent, 0) != null) {
            context.startActivity(searchIntent)
            return CommandResult.Success("Searching YouTube for \"$query\"")
        }

        // 2. YouTube URI search intent
        val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")).apply {
            `package` = "com.google.android.youtube"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (pm.resolveActivity(viewIntent, 0) != null) {
            context.startActivity(viewIntent)
            return CommandResult.Success("Searching YouTube for \"$query\"")
        }

        // 3. Browser fallback
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(browserIntent)
            CommandResult.Success("Searching YouTube for \"$query\"")
        } catch (e: Exception) {
            CommandResult.Error("Could not search YouTube: ${e.message}")
        }
    }

    private fun handleAsk(arg: String): CommandResult {
        val query = arg.trim()
        val preferredPkg = runBlocking {
            try { userPreferences.assistantPackage.first() } catch (_: Exception) { null }
        }
        val pm = context.packageManager

        val knownAi = listOf(
            "com.google.android.apps.bard" to "Gemini",
            "com.openai.chatgpt" to "ChatGPT",
            "com.anthropic.claude" to "Claude",
            "ai.perplexity.app" to "Perplexity",
            "com.microsoft.copilot" to "Copilot",
            "com.google.android.googlequicksearchbox" to "Google Assistant"
        )

        val targetPkg = if (!preferredPkg.isNullOrBlank() && preferredPkg != "SYSTEM_DEFAULT") {
            preferredPkg
        } else {
            knownAi.firstOrNull { (pkg, _) ->
                try {
                    pm.getApplicationInfo(pkg, 0).enabled
                } catch (_: Exception) { false }
            }?.first
        }

        // If query is provided, send it directly to the assistant!
        if (query.isNotEmpty()) {
            if (targetPkg != null) {
                val label = try {
                    val ai = pm.getApplicationInfo(targetPkg, 0)
                    pm.getApplicationLabel(ai).toString()
                } catch (_: Exception) { "Assistant" }

                // 1. Try ACTION_SEND (text/plain) - supported by ChatGPT, Gemini, Claude, Copilot
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, query)
                    `package` = targetPkg
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (pm.resolveActivity(sendIntent, 0) != null) {
                    context.startActivity(sendIntent)
                    return CommandResult.Success("Sent to $label: \"$query\"")
                }

                // 2. Try ACTION_ASSIST with query payload
                val assistIntent = Intent(Intent.ACTION_ASSIST).apply {
                    `package` = targetPkg
                    putExtra(Intent.EXTRA_TEXT, query)
                    putExtra("query", query)
                    putExtra(android.app.SearchManager.QUERY, query)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (pm.resolveActivity(assistIntent, 0) != null) {
                    context.startActivity(assistIntent)
                    return CommandResult.Success("Sent to $label: \"$query\"")
                }

                // 3. Try launch intent with EXTRA_TEXT
                val launchIntent = pm.getLaunchIntentForPackage(targetPkg)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Intent.EXTRA_TEXT, query)
                    putExtra("query", query)
                }
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return CommandResult.Success("Opening $label with prompt…")
                }
            }

            // 4. Try Android System Assist overlay with query extras
            val systemAssist = Intent(Intent.ACTION_ASSIST).apply {
                putExtra(Intent.EXTRA_TEXT, query)
                putExtra("query", query)
                putExtra(android.app.SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (pm.resolveActivity(systemAssist, 0) != null) {
                try {
                    context.startActivity(systemAssist)
                    return CommandResult.Success("Sent to Assistant: \"$query\"")
                } catch (_: Exception) {}
            }

            // 5. Final fallback: Web Search with the user's prompt
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(android.app.SearchManager.QUERY, query)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return try {
                context.startActivity(searchIntent)
                CommandResult.Success("Searching '$query'…")
            } catch (_: Exception) {
                val browserFallback = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
                ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                try {
                    context.startActivity(browserFallback)
                    CommandResult.Success("Opening search for '$query'")
                } catch (e: Exception) {
                    CommandResult.Error("Could not launch assistant: ${e.message}")
                }
            }
        } else {
            // Query is empty: simply launch the assistant overlay or app
            if (targetPkg != null) {
                val label = try {
                    val ai = pm.getApplicationInfo(targetPkg, 0)
                    pm.getApplicationLabel(ai).toString()
                } catch (_: Exception) { "Assistant" }

                val launchIntent = pm.getLaunchIntentForPackage(targetPkg)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return CommandResult.Success("Opening $label…")
                }
            }

            val assistIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(assistIntent)
                return CommandResult.Success("Opening Assistant…")
            } catch (_: Exception) {
                val assistFallback = Intent(Intent.ACTION_ASSIST).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    context.startActivity(assistFallback)
                    return CommandResult.Success("Opening Assistant…")
                } catch (_: Exception) {
                    return CommandResult.Error("Could not open digital assistant")
                }
            }
        }
    }


    /**
     * If user typed arbitrary natural text and it doesn't match an app name,
     * automatically search it in the default web browser instead of failing!
     */
    private fun handleAppSearchOrWebFallback(query: String): CommandResult {
        val apps = appRepository.getInstalledApps()
        val matches = appRepository.searchApps(query, apps)

        if (matches.isNotEmpty()) {
            val target = matches.first()
            val launchIntent = appRepository.getLaunchIntent(target.packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return CommandResult.LaunchedApp(target.label)
            }
        }

        // Natural text fallback -> search in default browser seamlessly
        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", query)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return try {
            context.startActivity(searchIntent)
            CommandResult.Success("Searching '$query' in browser…")
        } catch (_: Exception) {
            val browserFallback = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            try {
                context.startActivity(browserFallback)
                CommandResult.Success("Searching '$query' in browser…")
            } catch (e: Exception) {
                CommandResult.Error("Could not search web: ${e.message}")
            }
        }
    }

    private fun executeCustomCommand(target: String, args: String): CommandResult {
        if (target.isBlank()) return CommandResult.Error("Invalid custom command target")

        // Case 1: Web URL
        if (target.startsWith("http://") || target.startsWith("https://")) {
            val finalUrl = when {
                target.contains("%s") -> String.format(target, Uri.encode(args))
                args.isNotBlank() && target.contains("?") -> "$target&q=${Uri.encode(args)}"
                args.isNotBlank() -> "$target/search?q=${Uri.encode(args)}"
                else -> target
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return try {
                context.startActivity(intent)
                CommandResult.Success("Opening $finalUrl…")
            } catch (e: Exception) {
                CommandResult.Error("Could not open URL: ${e.message}")
            }
        }

        // Case 2: Android System Settings Action
        if (target.startsWith("android.settings.") || target.startsWith("android.provider.Settings.")) {
            val action = if (target.startsWith("android.settings.")) {
                target
            } else {
                target.replace("android.provider.Settings.", "android.settings.")
            }
            val intent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return try {
                context.startActivity(intent)
                CommandResult.Success("Opening system settings…")
            } catch (e: Exception) {
                CommandResult.Error("Settings action failed: ${e.message}")
            }
        }

        // Case 3: Re-evaluate as an expanded launcher command (e.g. target is "- todo", "!", or "event")
        val isPrefixed = CommandType.fromSymbol(target.first()) != null ||
                listOf("alarm", "timer", "todo", "note", "event", "call", "msg", "email", "drive", "ask", "help", "pomo", "pomodoro", "yt", "youtube", "uninstall", "wa", "whatsapp", "pin", "unpin", "fav", "unfav")
                    .any { target.lowercase().startsWith(it) }
        if (isPrefixed) {
            val expanded = if (args.isNotBlank()) "$target $args" else target
            return execute(expanded)
        }

        // Case 4: Package or App Name
        val launchIntent = context.packageManager.getLaunchIntentForPackage(target)
        if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            return CommandResult.LaunchedApp(target)
        }

        // Check by app label
        val apps = appRepository.getInstalledApps()
        val matched = apps.firstOrNull { it.label.equals(target, ignoreCase = true) }
        if (matched != null) {
            val appIntent = appRepository.getLaunchIntent(matched.packageName)
            if (appIntent != null) {
                appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(appIntent)
                return CommandResult.LaunchedApp(matched.label)
            }
        }

        // Fallback: search web with target and args
        val query = if (args.isNotBlank()) "$target $args" else target
        return handleAppSearchOrWebFallback(query)
    }

    private fun handleAliasCommand(args: String): CommandResult {
        val trimmed = args.trim()
        if (trimmed.isEmpty() || trimmed.equals("list", ignoreCase = true)) {
            val customMap = runBlocking {
                try { userPreferences.customCommands.first() } catch (_: Exception) { emptyMap() }
            }
            if (customMap.isEmpty()) {
                return CommandResult.Success("No custom aliases set. Type: alias <key> = <action>")
            }
            val formatted = customMap.entries.joinToString("\n") { (k, v) -> "• $k -> $v" }
            return CommandResult.Success("Custom Aliases:\n$formatted")
        }

        val key: String
        val target: String
        if (trimmed.contains("=")) {
            key = trimmed.substringBefore("=").trim().lowercase()
            target = trimmed.substringAfter("=").trim()
        } else if (trimmed.contains(" ")) {
            key = trimmed.substringBefore(" ").trim().lowercase()
            target = trimmed.substringAfter(" ").trim()
        } else {
            return CommandResult.Error("Usage: alias <key> = <target>\nExample: alias yt = https://youtube.com")
        }

        if (key.isBlank() || target.isBlank()) {
            return CommandResult.Error("Invalid alias format. Example: alias yt = https://youtube.com")
        }

        runBlocking {
            userPreferences.setCustomCommand(key, target)
        }
        return CommandResult.Success("Saved custom alias '$key' -> $target")
    }

    private fun handleUnaliasCommand(args: String): CommandResult {
        val key = args.trim().lowercase()
        if (key.isEmpty()) {
            return CommandResult.Error("Usage: unalias <key>\nExample: unalias yt")
        }
        runBlocking {
            userPreferences.removeCustomCommand(key)
        }
        return CommandResult.Success("Removed custom alias '$key'")
    }

    private fun handleHelp(): CommandResult {
        val helpList = listOf(
            Pair("help or ~", "Show this complete commands and guide cheat sheet"),
            Pair("uninstall or /", "uninstall <app> — uninstall an application via native system dialog"),
            Pair("pin", "pin <app> — pin app shortcut to Home screen"),
            Pair("unpin", "unpin <app> — remove pinned app shortcut from Home screen"),
            Pair("- or todo", "- Buy groceries — add item to Focus Tasks"),
            Pair("! or note", "! Ideas for project — save note to Minimal Memo"),
            Pair("* or event", "* Meeting tomorrow at 3pm — add to Calendar"),
            Pair("+ or timer", "+ 25m or timer 15 mins — set countdown timer"),
            Pair(": or alarm", ": 7:30 AM or alarm 6:30 — set wake-up alarm"),
            Pair("@ or msg", "@ Alex see you soon — direct SMS to contact"),
            Pair("# or call", "# John or call 911 — dial phone number or contact"),
            Pair("? or ask", "? how far is the moon — ask AI / Gemini / Web"),
            Pair("& or email", "& team@co.com Update — compose email"),
            Pair("^ or drive", "^ project_doc — search files in Google Drive"),
            Pair("alias", "alias yt = https://youtube.com — set custom command directly"),
            Pair("unalias", "unalias yt — delete custom command"),
            Pair("app search", "Type any app name (e.g. 'Spotify') to launch"),
            Pair("web search", "Type any question or text to search web"),
            Pair("custom", "Create and manage custom aliases in Settings")
        )
        return CommandResult.ShowHelp(helpList)
    }
}
