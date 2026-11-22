package com.zaryif.minimalos.domain.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class PrayerTimesCalcTest {
    private val london = ZoneId.of("Europe/London")
    private val batleyLat = 53.70
    private val batleyLon = -1.63

    private fun at(h: Int, m: Int, date: LocalDate = LocalDate.of(2026, 11, 23)) =
        ZonedDateTime.of(date.atTime(h, m), london).toInstant().toEpochMilli()

    @Test
    fun prayersComeInOrderAndLookRightForBatleyInNovember() {
        val day = PrayerTimesCalc.day(batleyLat, batleyLon, LocalDate.of(2026, 11, 23), PrayerMethod.MOON_SIGHTING_COMMITTEE, AsrSchool.HANAFI)
        assertEquals(listOf("fajr", "dhuhr", "asr", "maghrib", "isha"), day.map { it.name })
        assertTrue(day.zipWithNext().all { (a, b) -> a.at < b.at })
        // Late November in West Yorkshire: dhuhr around midday, maghrib around 4pm.
        assertTrue(day[1].at in at(11, 45)..at(12, 15))
        assertTrue(day[3].at in at(15, 40)..at(16, 15))
    }

    @Test
    fun hanafiAsrIsLaterThanShafi() {
        val date = LocalDate.of(2026, 11, 23)
        val hanafi = PrayerTimesCalc.day(batleyLat, batleyLon, date, PrayerMethod.MUSLIM_WORLD_LEAGUE, AsrSchool.HANAFI)[2].at
        val shafi = PrayerTimesCalc.day(batleyLat, batleyLon, date, PrayerMethod.MUSLIM_WORLD_LEAGUE, AsrSchool.SHAFI)[2].at
        assertTrue(hanafi > shafi)
    }

    @Test
    fun fridayDhuhrIsJumuah() {
        val friday = PrayerTimesCalc.day(batleyLat, batleyLon, LocalDate.of(2026, 11, 27), PrayerMethod.MOON_SIGHTING_COMMITTEE, AsrSchool.HANAFI)
        assertEquals("jumuah", friday[1].name)
    }

    @Test
    fun midsummerStillGivesFiveTimesForEveryMethod() {
        PrayerMethod.entries.forEach { method ->
            val day = PrayerTimesCalc.day(batleyLat, batleyLon, LocalDate.of(2026, 6, 21), method, AsrSchool.HANAFI)
            assertTrue(method.name, day.zipWithNext().all { (a, b) -> a.at < b.at })
        }
    }

    @Test
    fun afterIshaTheNextIsTomorrowsFajr() {
        val today = listOf(PrayerSlot("fajr", 100), PrayerSlot("isha", 200))
        val tomorrow = listOf(PrayerSlot("fajr", 300), PrayerSlot("isha", 400))
        assertEquals(PrayerSlot("isha", 200), PrayerTimesCalc.pickNext(today, tomorrow, now = 150))
        assertEquals(PrayerSlot("fajr", 300), PrayerTimesCalc.pickNext(today, tomorrow, now = 250))
    }

    @Test
    fun labelsTheCountdown() {
        val slot = PrayerSlot("asr", at(14, 5))
        assertEquals("asr 14:05 · in 1h 20m", PrayerTimesCalc.label(slot, at(12, 45), london))
        assertEquals("asr 14:05 · in 35m", PrayerTimesCalc.label(slot, at(13, 30), london))
        assertEquals("asr 14:05 · in 2h", PrayerTimesCalc.label(slot, at(12, 5), london))
        assertEquals("asr 14:05 · now", PrayerTimesCalc.label(slot, at(14, 5), london))
    }

    @Test
    fun unknownSettingsFallBackToDefaults() {
        assertEquals(PrayerMethod.MOON_SIGHTING_COMMITTEE, PrayerMethod.from(null))
        assertEquals(AsrSchool.HANAFI, AsrSchool.from("nonsense"))
    }
}
