package com.zaryif.minimalos.domain.parser

import com.zaryif.minimalos.domain.model.CommandType
import org.junit.Assert.assertEquals
import org.junit.Test

class CommandParserTest {

    @Test
    fun testSymbolCommands() {
        assertEquals(CommandType.TODO, CommandParser.parse("-Buy groceries").type)
        assertEquals("Buy groceries", CommandParser.parse("-Buy groceries").argument)

        assertEquals(CommandType.NOTE, CommandParser.parse("!Quick memo").type)
        assertEquals("Quick memo", CommandParser.parse("!Quick memo").argument)

        assertEquals(CommandType.CALL, CommandParser.parse("#Alex").type)
        assertEquals("Alex", CommandParser.parse("#Alex").argument)

        assertEquals(CommandType.MESSAGE, CommandParser.parse("@Mom on my way").type)
        assertEquals("Mom on my way", CommandParser.parse("@Mom on my way").argument)

        assertEquals(CommandType.POMODORO, CommandParser.parse("%25m").type)
        assertEquals("25m", CommandParser.parse("%25m").argument)

        assertEquals(CommandType.YOUTUBE, CommandParser.parse("\$lofi beats").type)
        assertEquals("lofi beats", CommandParser.parse("\$lofi beats").argument)

        assertEquals(CommandType.UNINSTALL, CommandParser.parse("/chatgpt").type)
        assertEquals("chatgpt", CommandParser.parse("/chatgpt").argument)
    }

    @Test
    fun testKeywordCommands() {
        val todo = CommandParser.parse("todo Buy milk")
        assertEquals(CommandType.TODO, todo.type)
        assertEquals("Buy milk", todo.argument)

        val alarm = CommandParser.parse("alarm 7:00 am")
        assertEquals(CommandType.ALARM, alarm.type)
        assertEquals("7:00 am", alarm.argument)

        val uninstall = CommandParser.parse("uninstall com.example.app")
        assertEquals(CommandType.UNINSTALL, uninstall.type)
        assertEquals("com.example.app", uninstall.argument)

        val pomo = CommandParser.parse("pomo 45m")
        assertEquals(CommandType.POMODORO, pomo.type)
        assertEquals("45m", pomo.argument)

        val pin = CommandParser.parse("pin chrome")
        assertEquals(CommandType.PIN, pin.type)
        assertEquals("chrome", pin.argument)

        val unpin = CommandParser.parse("unpin chrome")
        assertEquals(CommandType.UNPIN, unpin.type)
        assertEquals("chrome", unpin.argument)

        val fav = CommandParser.parse("fav spotify")
        assertEquals(CommandType.PIN, fav.type)
        assertEquals("spotify", fav.argument)

        val help = CommandParser.parse("help")
        assertEquals(CommandType.HELP, help.type)
    }

    @Test
    fun testRedundantPrefixStripping() {
        val todo = CommandParser.parse("- todo Buy coffee")
        assertEquals(CommandType.TODO, todo.type)
        assertEquals("Buy coffee", todo.argument)

        val note = CommandParser.parse("! note Meeting notes")
        assertEquals(CommandType.NOTE, note.type)
        assertEquals("Meeting notes", note.argument)

        val uninstall = CommandParser.parse("/ uninstall chatgpt")
        assertEquals(CommandType.UNINSTALL, uninstall.type)
        assertEquals("chatgpt", uninstall.argument)
    }

    @Test
    fun testSearchFallback() {
        val search = CommandParser.parse("spotify")
        assertEquals(CommandType.SEARCH, search.type)
        assertEquals("spotify", search.argument)
    }
}
