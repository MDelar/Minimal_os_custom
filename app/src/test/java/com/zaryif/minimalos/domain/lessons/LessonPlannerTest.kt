package com.zaryif.minimalos.domain.lessons

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LessonPlannerTest {
    private val min = 60_000L
    private fun m(minutes: Int) = minutes * min

    // Period 1 09:00-10:00, period 2 10:05-11:00 (5 min changeover), period 3 11:20-12:20 (break before it).
    private val day = listOf(
        LessonEvent(m(540), m(600)),
        LessonEvent(m(605), m(660)),
        LessonEvent(m(680), m(740))
    )

    @Test
    fun backToBackLessonsAreOneBlock() {
        val plan = LessonPlanner.plan(day, now = m(550))
        assertEquals(m(660), plan.silenceUntil)
        assertEquals(m(660), plan.nextCheck)
    }

    @Test
    fun breakBetweenLessonsIsNotSilenced() {
        val plan = LessonPlanner.plan(day, now = m(665))
        assertNull(plan.silenceUntil)
        assertEquals(m(680), plan.nextCheck)
    }

    @Test
    fun alarmLandingJustBeforeTheStartCountsAsStarted() {
        val plan = LessonPlanner.plan(day, now = m(680) - 5_000L)
        assertEquals(m(740), plan.silenceUntil)
    }

    @Test
    fun nothingTodayStillRechecksWithinThreeHours() {
        val plan = LessonPlanner.plan(emptyList(), now = m(800))
        assertNull(plan.silenceUntil)
        assertEquals(m(800) + LessonPlanner.MAX_WAIT_MS, plan.nextCheck)
    }

    @Test
    fun overlappingAndZeroLengthEvents() {
        val events = listOf(LessonEvent(m(100), m(200)), LessonEvent(m(150), m(180)), LessonEvent(m(300), m(300)))
        assertEquals(listOf(LessonEvent(m(100), m(200))), LessonPlanner.mergeBlocks(events.filter { it.end > it.begin }, LessonPlanner.JOIN_GAP_MS))
        assertNull(LessonPlanner.plan(events, now = m(300)).silenceUntil)
    }
}
