package com.zaryif.minimalos.domain.calendar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class CalendarAgendaTest {

    private val zone = ZoneId.of("Europe/London")
    // Friday 20 November 2026, 13:45 in London
    private val now = LocalDateTime.of(2026, 11, 20, 13, 45).atZone(zone).toInstant().toEpochMilli()

    private fun at(day: Int, hour: Int, minute: Int = 0) =
        LocalDateTime.of(2026, 11, day, hour, minute).atZone(zone).toInstant().toEpochMilli()

    // All-day events are stored as UTC midnights.
    private fun utcDay(day: Int) = LocalDate.of(2026, 11, day).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

    private fun timed(id: Long, title: String, begin: Long, end: Long) = AgendaEvent(id, title, begin, end, allDay = false)
    private fun allDay(id: Long, title: String, fromDay: Int, toDayExclusive: Int) =
        AgendaEvent(id, title, utcDay(fromDay), utcDay(toDayExclusive), allDay = true)

    @Test
    fun labelsTimedEvents() {
        assertEquals("15:00", CalendarAgenda.whenLabel(timed(1, "a", at(20, 15), at(20, 16)), now, zone))
        assertEquals("tmrw 09:30", CalendarAgenda.whenLabel(timed(2, "b", at(21, 9, 30), at(21, 10)), now, zone))
        assertEquals("tue 08:00", CalendarAgenda.whenLabel(timed(3, "c", at(24, 8), at(24, 9)), now, zone))
        assertEquals("28 nov 10:00", CalendarAgenda.whenLabel(timed(4, "d", at(28, 10), at(28, 11)), now, zone))
        assertEquals("now", CalendarAgenda.whenLabel(timed(5, "e", at(20, 13), at(20, 14)), now, zone))
    }

    @Test
    fun labelsAllDayEvents() {
        assertEquals("today", CalendarAgenda.whenLabel(allDay(1, "a", 20, 21), now, zone))
        assertEquals("tmrw", CalendarAgenda.whenLabel(allDay(2, "b", 21, 22), now, zone))
        assertEquals("mon", CalendarAgenda.whenLabel(allDay(3, "c", 23, 24), now, zone))
        // A multi-day event that started yesterday reads as today
        assertEquals("today", CalendarAgenda.whenLabel(allDay(4, "d", 19, 22), now, zone))
    }

    @Test
    fun dropsFinishedEventsAndKeepsOrder() {
        val events = listOf(
            timed(1, "finished this morning", at(20, 9), at(20, 10)),
            timed(2, "later today", at(20, 16), at(20, 17)),
            allDay(3, "all day today", 20, 21),
            allDay(4, "all day yesterday", 19, 20),
            timed(5, "on now", at(20, 13), at(20, 14)),
            timed(6, "tomorrow", at(21, 9), at(21, 10))
        )
        val result = CalendarAgenda.selectUpcoming(events, now, zone, limit = 3)
        assertEquals(listOf("all day today", "on now", "later today"), result.map { it.title })
    }

    @Test
    fun respectsTheLimit() {
        val events = (1..6).map { timed(it.toLong(), "e$it", at(21, 8 + it), at(21, 9 + it)) }
        assertEquals(listOf("e1"), CalendarAgenda.selectUpcoming(events, now, zone, limit = 1).map { it.title })
    }
}
