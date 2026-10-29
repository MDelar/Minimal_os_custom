package com.zaryif.minimalos.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class NaturalDateTimeParserTest {

    @Test
    fun testBareNumbersAreNotExtractedAsTime() {
        val result1 = NaturalDateTimeParser.parse("Meeting with 2 clients", "Event")
        assertFalse("Bare number 2 should not be treated as explicit time", result1.hasExplicitTime)
        assertEquals("Meeting with 2 clients", result1.cleanTitle)

        val result2 = NaturalDateTimeParser.parse("Buy 3 apples", "Event")
        assertFalse("Bare number 3 should not be treated as explicit time", result2.hasExplicitTime)
        assertEquals("Buy 3 apples", result2.cleanTitle)
    }

    @Test
    fun testExplicitTimeWithMinutes() {
        val result = NaturalDateTimeParser.parse("Standup at 9:30 am", "Event")
        assertTrue(result.hasExplicitTime)
        assertEquals(9, result.hour)
        assertEquals(30, result.minute)
        assertEquals("Standup", result.cleanTitle)
    }

    @Test
    fun testExplicitTimeWithAmPm() {
        val result = NaturalDateTimeParser.parse("Lunch 1pm", "Event")
        assertTrue(result.hasExplicitTime)
        assertEquals(13, result.hour)
        assertEquals(0, result.minute)
        assertEquals("Lunch", result.cleanTitle)
    }

    @Test
    fun testExplicitTimeWithAt() {
        val result = NaturalDateTimeParser.parse("Dinner at 7", "Event")
        assertTrue(result.hasExplicitTime)
        assertEquals(19, result.hour) // 7pm
        assertEquals(0, result.minute)
        assertEquals("Dinner", result.cleanTitle)
    }

    @Test
    fun testAlarmDefaultDoesNotConvertToPm() {
        val result = NaturalDateTimeParser.parse("7:00", "Alarm")
        assertTrue(result.hasExplicitTime)
        assertEquals("Alarm 7:00 without am/pm should remain 7am", 7, result.hour)
        assertEquals(0, result.minute)
    }

    @Test
    fun testRelativeOffset() {
        val before = System.currentTimeMillis()
        val result = NaturalDateTimeParser.parse("Check oven in 30 mins", "Event")
        assertTrue(result.hasExplicitTime)
        assertTrue(result.hasExplicitDate)
        assertEquals("Check oven", result.cleanTitle)
        assertTrue(result.startMillis >= before + 29 * 60 * 1000)
    }

    @Test
    fun testRelativeDayTomorrow() {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val expectedDay = cal.get(Calendar.DAY_OF_YEAR)

        val result = NaturalDateTimeParser.parse("Dentist appointment tomorrow at 10am", "Event")
        assertTrue(result.hasExplicitDate)
        assertTrue(result.hasExplicitTime)
        assertEquals(expectedDay, result.calendar.get(Calendar.DAY_OF_YEAR))
        assertEquals(10, result.hour)
        assertEquals("Dentist appointment", result.cleanTitle)
    }
}
