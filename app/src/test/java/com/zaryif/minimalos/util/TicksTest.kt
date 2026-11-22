package com.zaryif.minimalos.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TicksTest {
    @Test
    fun wakesJustAfterTheNextMinute() {
        assertEquals(60_050L, Ticks.untilNextMinute(now = 120_000L))
        assertEquals(1_050L, Ticks.untilNextMinute(now = 179_000L))
    }

    @Test
    fun countdownTicksPerSecondOnlyInTheLastHour() {
        assertEquals(800L, Ticks.countdownDelay(remainingMs = 45 * 60_000L, now = 10_200L))
        assertEquals(30_000L, Ticks.countdownDelay(remainingMs = 5 * 3_600_000L, now = 0L))
        assertEquals(10_050L, Ticks.countdownDelay(remainingMs = 0L, now = 50_000L))
    }
}
