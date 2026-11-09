package com.zaryif.minimalos.domain.parser

import com.zaryif.minimalos.domain.model.Command
import com.zaryif.minimalos.domain.model.CommandType

object CommandParser {

    fun parse(input: String): Command {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Command(
                type = CommandType.SEARCH,
                rawInput = input,
                argument = ""
            )
        }

        // 1. Check for single symbol prefix
        val firstChar = trimmed.first()
        val symbolCommand = CommandType.fromSymbol(firstChar)
        if (symbolCommand != null) {
            var arg = trimmed.drop(1).trim()
            val redundantPrefixes = when (symbolCommand) {
                CommandType.NOTE -> listOf("note ", "notes ", "memo ", "keep ")
                CommandType.TODO -> listOf("todo ", "task ", "reminder ", "remind ")
                CommandType.CALL -> listOf("call ", "dial ")
                CommandType.MESSAGE -> listOf("msg ", "sms ", "text ", "message ")
                CommandType.EVENT -> listOf("event ", "cal ", "calendar ", "schedule ")
                CommandType.ALARM -> listOf("set alarm ", "alarm ")
                CommandType.TIMER -> listOf("set timer ", "timer ")
                CommandType.DRIVE -> listOf("drive ")
                CommandType.EMAIL -> listOf("mail ", "email ")
                CommandType.WHATSAPP -> listOf("whatsapp ", "wa ")
                CommandType.ASK -> listOf("assistant ", "ai ", "ask ", "search ", "google ", "find ")
                CommandType.YOUTUBE -> listOf("yt ", "youtube ")
                CommandType.POMODORO -> listOf("pomodoro ", "pomo ")
                CommandType.UNINSTALL -> listOf("uninstall ", "remove ", "rmapp ")
                CommandType.HELP -> listOf("help ", "commands ", "guide ")
                else -> emptyList()
            }
            for (p in redundantPrefixes) {
                if (arg.lowercase().startsWith(p)) {
                    arg = arg.substring(p.length).trim()
                    break
                }
            }
            return Command(
                type = symbolCommand,
                rawInput = input,
                argument = arg
            )
        }

        val lower = trimmed.lowercase()

        // 2. Check for help commands
        if (lower == "help" || lower.startsWith("help ") || lower == "commands" || lower == "man" || lower == "guide") {
            return Command(
                type = CommandType.HELP,
                rawInput = input,
                argument = ""
            )
        }

        // 3. Check for natural language keyword prefixes
        val keywordMatches = listOf(
            PrefixMapping(listOf("set alarm", "alarm"), CommandType.ALARM, requireSpace = true),
            PrefixMapping(listOf("set timer", "timer"), CommandType.TIMER, requireSpace = false),
            PrefixMapping(listOf("pomodoro", "pomo"), CommandType.POMODORO, requireSpace = false),
            PrefixMapping(listOf("yt", "youtube"), CommandType.YOUTUBE, requireSpace = false),
            PrefixMapping(listOf("uninstall", "remove", "rmapp"), CommandType.UNINSTALL, requireSpace = false),
            PrefixMapping(listOf("pin", "fav"), CommandType.PIN, requireSpace = false),
            PrefixMapping(listOf("unpin", "unfav"), CommandType.UNPIN, requireSpace = false),
            PrefixMapping(listOf("call", "dial"), CommandType.CALL, requireSpace = false),
            PrefixMapping(listOf("whatsapp", "wa"), CommandType.WHATSAPP, requireSpace = false),
            PrefixMapping(listOf("msg", "sms", "text", "message"), CommandType.MESSAGE, requireSpace = false),
            PrefixMapping(listOf("notes", "note", "keep", "memo"), CommandType.NOTE, requireSpace = false),
            PrefixMapping(listOf("todo", "task", "reminder", "remind"), CommandType.TODO, requireSpace = false),
            PrefixMapping(listOf("event", "cal", "calendar", "schedule"), CommandType.EVENT, requireSpace = true),
            PrefixMapping(listOf("drive"), CommandType.DRIVE, requireSpace = true),
            PrefixMapping(listOf("email", "mail"), CommandType.EMAIL, requireSpace = true),
            PrefixMapping(listOf("assistant", "ai", "ask", "search", "google", "find"), CommandType.ASK, requireSpace = false)
        )

        for (mapping in keywordMatches) {
            for (prefix in mapping.prefixes) {
                if (lower.startsWith("$prefix ")) {
                    val arg = trimmed.substring(prefix.length).trim()
                    return Command(
                        type = mapping.commandType,
                        rawInput = input,
                        argument = arg
                    )
                } else if (!mapping.requireSpace && lower == prefix) {
                    return Command(
                        type = mapping.commandType,
                        rawInput = input,
                        argument = ""
                    )
                }
            }
        }

        // 4. Fallback: app search
        return Command(
            type = CommandType.SEARCH,
            rawInput = input,
            argument = trimmed
        )
    }

    private data class PrefixMapping(
        val prefixes: List<String>,
        val commandType: CommandType,
        val requireSpace: Boolean = false
    )
}
