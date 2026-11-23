package com.zaryif.minimalos.domain.calendardnd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarDndPlannerTest {
    private val min = 60_000L
    private fun m(minutes: Int) = minutes * min

    // Period 1 09:00-10:00, period 2 10:05-11:00 (5 min changeover), period 3 11:20-12:20 (break before it).
    private val day = listOf(
        DndEvent(m(540), m(600)),
        DndEvent(m(605), m(660)),
        DndEvent(m(680), m(740))
    )

    @Test
    fun backToBackEventsAreOneBlock() {
        val plan = CalendarDndPlanner.plan(day, now = m(550))
        assertEquals(m(660), plan.silenceUntil)
        assertEquals(m(660), plan.nextCheck)
    }

    @Test
    fun gapBetweenEventsIsNotSilenced() {
        val plan = CalendarDndPlanner.plan(day, now = m(665))
        assertNull(plan.silenceUntil)
        assertEquals(m(680), plan.nextCheck)
    }

    @Test
    fun alarmLandingJustBeforeTheStartCountsAsStarted() {
        val plan = CalendarDndPlanner.plan(day, now = m(680) - 5_000L)
        assertEquals(m(740), plan.silenceUntil)
    }

    @Test
    fun nothingTodayStillRechecksWithinThreeHours() {
        val plan = CalendarDndPlanner.plan(emptyList(), now = m(800))
        assertNull(plan.silenceUntil)
        assertEquals(m(800) + CalendarDndPlanner.MAX_WAIT_MS, plan.nextCheck)
    }

    @Test
    fun overlappingAndZeroLengthEvents() {
        val events = listOf(DndEvent(m(100), m(200)), DndEvent(m(150), m(180)), DndEvent(m(300), m(300)))
        assertEquals(listOf(DndEvent(m(100), m(200))), CalendarDndPlanner.mergeBlocks(events.filter { it.end > it.begin }, CalendarDndPlanner.JOIN_GAP_MS))
        assertNull(CalendarDndPlanner.plan(events, now = m(300)).silenceUntil)
    }
}
