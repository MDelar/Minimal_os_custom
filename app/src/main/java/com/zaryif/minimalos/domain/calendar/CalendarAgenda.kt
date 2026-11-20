package com.zaryif.minimalos.domain.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class AgendaEvent(
    val eventId: Long,
    val title: String,
    val begin: Long,
    val end: Long,
    val allDay: Boolean
)

/**
 * Reads upcoming events from the phone's calendar storage, which Google Calendar
 * and other calendar apps sync into. Local only: nothing goes online.
 */
object CalendarAgenda {

    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    /**
     * The next [limit] events that haven't finished yet, within [days] days, from
     * calendars shown in the calendar app. Ongoing events are included. Returns
     * null without calendar permission.
     */
    fun upcoming(context: Context, limit: Int = 3, days: Int = 7, now: Long = System.currentTimeMillis()): List<AgendaEvent>? {
        if (!hasPermission(context)) return null
        // Start a day back so all-day events (stored at UTC midnight) for today are included.
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, now - 24 * 60 * 60 * 1000L)
            ContentUris.appendId(it, now + days * 24 * 60 * 60 * 1000L)
        }.build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY
        )
        val selection = "${CalendarContract.Instances.VISIBLE} = 1 AND " +
            "(${CalendarContract.Instances.STATUS} IS NULL OR ${CalendarContract.Instances.STATUS} != ${CalendarContract.Instances.STATUS_CANCELED})"
        val events = mutableListOf<AgendaEvent>()
        try {
            context.contentResolver.query(uri, projection, selection, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { c ->
                while (c.moveToNext()) {
                    events += AgendaEvent(
                        eventId = c.getLong(0),
                        title = c.getString(1)?.trim().takeUnless { it.isNullOrEmpty() } ?: "(no title)",
                        begin = c.getLong(2),
                        end = c.getLong(3),
                        allDay = c.getInt(4) == 1
                    )
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return selectUpcoming(events, now, ZoneId.systemDefault(), limit)
    }

    /** Drops finished events and sorts what's left by when it happens locally. */
    fun selectUpcoming(events: List<AgendaEvent>, now: Long, zone: ZoneId, limit: Int): List<AgendaEvent> =
        events
            .filter { localEnd(it, zone) > now }
            .sortedWith(compareBy({ localBegin(it, zone) }, { !it.allDay }))
            .distinctBy { it.eventId to it.begin }
            .take(limit)

    // All-day events are stored as UTC midnights; read them as local dates.
    private fun localBegin(e: AgendaEvent, zone: ZoneId): Long =
        if (e.allDay) utcDate(e.begin).atStartOfDay(zone).toInstant().toEpochMilli() else e.begin

    private fun localEnd(e: AgendaEvent, zone: ZoneId): Long =
        if (e.allDay) utcDate(e.end).atStartOfDay(zone).toInstant().toEpochMilli() else e.end

    private fun utcDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate()

    /**
     * Short label for when an event happens:
     * "now", "14:00", "tmrw 09:00", "sat 09:00", "28 nov"; all-day events are
     * "today", "tmrw", "sat" or "28 nov".
     */
    fun whenLabel(e: AgendaEvent, now: Long, zone: ZoneId): String {
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val day: LocalDate
        val time: String?
        if (e.allDay) {
            day = maxOf(utcDate(e.begin), today) // a multi-day event already under way shows as today
            time = null
        } else {
            if (e.begin <= now) return "now"
            val start = LocalDateTime.ofInstant(Instant.ofEpochMilli(e.begin), zone)
            day = start.toLocalDate()
            time = start.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        val dayLabel = when {
            day == today -> if (time == null) "today" else null
            day == today.plusDays(1) -> "tmrw"
            day.isBefore(today.plusDays(7)) -> day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.UK).lowercase(Locale.UK)
            else -> "${day.dayOfMonth} ${day.month.getDisplayName(TextStyle.SHORT, Locale.UK).lowercase(Locale.UK)}"
        }
        return listOfNotNull(dayLabel, time).joinToString(" ")
    }
}
