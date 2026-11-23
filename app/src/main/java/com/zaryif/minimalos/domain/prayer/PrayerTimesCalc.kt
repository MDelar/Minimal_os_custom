package com.zaryif.minimalos.domain.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Calculation methods offered in Settings, in the order they cycle. */
enum class PrayerMethod(val label: String, internal val adhan: CalculationMethod) {
    MOON_SIGHTING_COMMITTEE("Moonsighting Committee", CalculationMethod.MOON_SIGHTING_COMMITTEE),
    MUSLIM_WORLD_LEAGUE("Muslim World League", CalculationMethod.MUSLIM_WORLD_LEAGUE),
    KARACHI("Karachi", CalculationMethod.KARACHI),
    NORTH_AMERICA("ISNA (North America)", CalculationMethod.NORTH_AMERICA),
    EGYPTIAN("Egyptian", CalculationMethod.EGYPTIAN),
    UMM_AL_QURA("Umm al-Qura", CalculationMethod.UMM_AL_QURA);

    companion object {
        fun from(name: String?): PrayerMethod = entries.firstOrNull { it.name == name } ?: MOON_SIGHTING_COMMITTEE
    }
}

/** Asr: Hanafi is the later time (shadow twice the length), Shafi'i/Maliki/Hanbali the earlier. */
enum class AsrSchool(val label: String, internal val adhan: Madhab) {
    HANAFI("Hanafi (later)", Madhab.HANAFI),
    SHAFI("Shafi'i, Maliki, Hanbali (earlier)", Madhab.SHAFI);

    companion object {
        fun from(name: String?): AsrSchool = entries.firstOrNull { it.name == name } ?: HANAFI
    }
}

data class PrayerSlot(val name: String, val at: Long)

/**
 * Prayer start times worked out on the phone from the town's coordinates (no
 * internet), using the Adhan library. Times are start times, not a mosque's
 * jama'ah times.
 */
object PrayerTimesCalc {

    fun day(lat: Double, lon: Double, date: LocalDate, method: PrayerMethod, school: AsrSchool): List<PrayerSlot> {
        val params = method.adhan.parameters.also {
            it.madhab = school.adhan
            // Far north (the UK in summer) twilight never ends; the Moonsighting
            // Committee method handles that itself, the others need a rule.
            if (kotlin.math.abs(lat) > 48 && method != PrayerMethod.MOON_SIGHTING_COMMITTEE) {
                it.highLatitudeRule = HighLatitudeRule.SEVENTH_OF_THE_NIGHT
            }
        }
        val t = PrayerTimes(Coordinates(lat, lon), DateComponents(date.year, date.monthValue, date.dayOfMonth), params)
        val dhuhr = if (date.dayOfWeek == DayOfWeek.FRIDAY) "jumuah" else "dhuhr"
        return listOf(
            PrayerSlot("fajr", t.fajr.time),
            PrayerSlot(dhuhr, t.dhuhr.time),
            PrayerSlot("asr", t.asr.time),
            PrayerSlot("maghrib", t.maghrib.time),
            PrayerSlot("isha", t.isha.time)
        )
    }

    /** How long a prayer shows as "now" after it starts, before the line moves on. */
    const val NOW_FOR_MS = 15 * 60_000L

    /**
     * The prayer to show: one that started in the last [NOW_FOR_MS] ("now"),
     * otherwise the next one, which after isha is tomorrow's fajr.
     */
    fun next(lat: Double, lon: Double, now: Long, zone: ZoneId, method: PrayerMethod, school: AsrSchool): PrayerSlot {
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        return pickNext(day(lat, lon, today, method, school), day(lat, lon, today.plusDays(1), method, school), now)
    }

    internal fun pickNext(today: List<PrayerSlot>, tomorrow: List<PrayerSlot>, now: Long): PrayerSlot =
        today.firstOrNull { it.at > now - NOW_FOR_MS } ?: tomorrow.first()

    /** "asr 14:05 · in 1h 20m", "maghrib 16:02 · in 35m", "isha 18:10 · now". */
    fun label(slot: PrayerSlot, now: Long, zone: ZoneId): String {
        val time = Instant.ofEpochMilli(slot.at).atZone(zone).format(DateTimeFormatter.ofPattern("HH:mm"))
        // Round up, like the clock: 14:08 with asr at 14:09 is "in 1m", not "now".
        val mins = ((slot.at - now + 59_999L) / 60_000L).toInt()
        val until = when {
            mins <= 0 -> "now"
            mins < 60 -> "in ${mins}m"
            mins % 60 == 0 -> "in ${mins / 60}h"
            else -> "in ${mins / 60}h ${mins % 60}m"
        }
        return "${slot.name} $time · $until"
    }
}
