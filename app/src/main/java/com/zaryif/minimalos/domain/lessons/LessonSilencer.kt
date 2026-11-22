package com.zaryif.minimalos.domain.lessons

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract

/**
 * Turns Do Not Disturb on while a lesson from the chosen calendars is under way
 * and off when it ends. Runs from an alarm at each start and end (plus a re-check
 * at least every 3 hours), so it works whether or not the launcher is open.
 *
 * It only undoes what it did: if Do Not Disturb was already on when a lesson
 * started, or you switch it off mid-lesson, it leaves it alone.
 */
object LessonSilencer {
    private const val PREFS = "lesson_silencer"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_CALENDARS = "calendar_ids"
    private const val KEY_ACTIVE = "we_silenced"
    private const val KEY_UNTIL = "silenced_until"
    private const val KEY_HANDLED_UNTIL = "handled_until"
    const val ACTION_SYNC = "com.zaryif.minimalos.LESSON_SYNC"

    data class CalendarOption(val id: Long, val name: String, val account: String)

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context) = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
        sync(context)
    }

    fun calendarIds(context: Context): Set<Long> =
        prefs(context).getStringSet(KEY_CALENDARS, emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet()

    fun setCalendarIds(context: Context, ids: Set<Long>) {
        prefs(context).edit().putStringSet(KEY_CALENDARS, ids.map { it.toString() }.toSet()).apply()
        sync(context)
    }

    fun hasDndAccess(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted

    private fun hasCalendarAccess(context: Context) =
        context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    /** When the current lesson silence ends, if the launcher silenced the phone; else null. */
    fun silencedUntil(context: Context, now: Long = System.currentTimeMillis()): Long? {
        val p = prefs(context)
        val until = p.getLong(KEY_UNTIL, 0L)
        return if (p.getBoolean(KEY_ACTIVE, false) && until > now) until else null
    }

    fun calendars(context: Context): List<CalendarOption> {
        if (!hasCalendarAccess(context)) return emptyList()
        val list = mutableListOf<CalendarOption>()
        try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.ACCOUNT_NAME
                ),
                null, null, CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " ASC"
            )?.use { c ->
                while (c.moveToNext()) list += CalendarOption(c.getLong(0), c.getString(1) ?: "(unnamed)", c.getString(2) ?: "")
            }
        } catch (_: Exception) {}
        return list
    }

    private fun events(context: Context, ids: Set<Long>, now: Long): List<LessonEvent> {
        if (ids.isEmpty() || !hasCalendarAccess(context)) return emptyList()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, now - 12 * 60 * 60_000L)
            ContentUris.appendId(it, now + 24 * 60 * 60_000L)
        }.build()
        val list = mutableListOf<LessonEvent>()
        try {
            context.contentResolver.query(
                uri,
                arrayOf(
                    CalendarContract.Instances.CALENDAR_ID,
                    CalendarContract.Instances.BEGIN,
                    CalendarContract.Instances.END,
                    CalendarContract.Instances.ALL_DAY,
                    CalendarContract.Instances.STATUS
                ),
                null, null, null
            )?.use { c ->
                while (c.moveToNext()) {
                    if (c.getLong(0) !in ids || c.getInt(3) == 1) continue
                    if (!c.isNull(4) && c.getInt(4) == CalendarContract.Instances.STATUS_CANCELED) continue
                    list += LessonEvent(c.getLong(1), c.getLong(2))
                }
            }
        } catch (_: Exception) {}
        return list
    }

    /** Brings Do Not Disturb in line with the timetable and schedules the next check. */
    @Synchronized
    fun sync(context: Context, now: Long = System.currentTimeMillis()) {
        val app = context.applicationContext
        val p = prefs(app)
        val nm = app.getSystemService(NotificationManager::class.java)
        val canDnd = hasDndAccess(app)
        val weSilenced = p.getBoolean(KEY_ACTIVE, false)

        if (!isEnabled(app)) {
            if (weSilenced && canDnd) restore(nm)
            p.edit().putBoolean(KEY_ACTIVE, false).remove(KEY_UNTIL).remove(KEY_HANDLED_UNTIL).apply()
            cancelAlarm(app)
            return
        }

        val plan = LessonPlanner.plan(events(app, calendarIds(app), now), now)
        val lessonEnd = plan.silenceUntil
        if (lessonEnd != null) {
            // Each lesson block is handled once: if you turn DND off mid-lesson, it stays off.
            val handled = p.getLong(KEY_HANDLED_UNTIL, 0L) == lessonEnd
            if (!handled && canDnd) {
                val alreadyQuiet = nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
                if (!alreadyQuiet) nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                p.edit()
                    .putBoolean(KEY_ACTIVE, !alreadyQuiet)
                    .putLong(KEY_UNTIL, lessonEnd)
                    .putLong(KEY_HANDLED_UNTIL, lessonEnd)
                    .apply()
            } else if (weSilenced) {
                p.edit().putLong(KEY_UNTIL, lessonEnd).apply()
            }
        } else {
            if (weSilenced && canDnd) restore(nm)
            p.edit().putBoolean(KEY_ACTIVE, false).remove(KEY_UNTIL).apply()
        }
        scheduleAlarm(app, plan.nextCheck)
    }

    // Only turn DND off if it's still the setting we chose (you may have changed it since).
    private fun restore(nm: NotificationManager) {
        if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    private fun alarmIntent(context: Context) = PendingIntent.getBroadcast(
        context, 4711,
        Intent(context, LessonSilenceReceiver::class.java).setAction(ACTION_SYNC),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    private fun scheduleAlarm(context: Context, at: Long) {
        val am = context.getSystemService(AlarmManager::class.java)
        val pi = alarmIntent(context)
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
            }
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    private fun cancelAlarm(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(alarmIntent(context))
    }
}

/** Lesson start/end alarms, plus boot and clock changes. */
class LessonSilenceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        Thread {
            try { LessonSilencer.sync(context) } finally { pending.finish() }
        }.start()
    }
}
