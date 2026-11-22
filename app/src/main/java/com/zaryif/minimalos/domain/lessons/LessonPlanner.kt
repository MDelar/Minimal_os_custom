package com.zaryif.minimalos.domain.lessons

/** A timed event from one of the chosen calendars. */
data class LessonEvent(val begin: Long, val end: Long)

/** What should happen now, and when to look again. */
data class LessonPlan(
    /** End of the lesson (or run of back-to-back lessons) under way, or null if none. */
    val silenceUntil: Long?,
    /** When to check again: the end of this lesson block, the next start, or a safety re-check. */
    val nextCheck: Long
)

/**
 * Pure scheduling logic for "silence during lessons". Lessons that overlap or
 * sit within [joinGapMs] of each other (changeover between periods) count as
 * one block, so the phone doesn't buzz for a moment between lessons.
 */
object LessonPlanner {
    const val JOIN_GAP_MS = 5 * 60_000L
    /** Re-check at least this often, so edited or newly synced lessons are picked up. */
    const val MAX_WAIT_MS = 3 * 60 * 60_000L
    /** An alarm can land a moment early; treat a lesson starting this soon as started. */
    const val START_TOLERANCE_MS = 15_000L

    fun plan(events: List<LessonEvent>, now: Long, joinGapMs: Long = JOIN_GAP_MS): LessonPlan {
        val blocks = mergeBlocks(events.filter { it.end > it.begin }, joinGapMs)
        val current = blocks.firstOrNull { it.begin <= now + START_TOLERANCE_MS && it.end > now }
        val safety = now + MAX_WAIT_MS
        if (current != null) return LessonPlan(current.end, minOf(current.end, safety))
        val next = blocks.firstOrNull { it.begin > now }
        return LessonPlan(null, minOf(next?.begin ?: safety, safety))
    }

    internal fun mergeBlocks(events: List<LessonEvent>, joinGapMs: Long): List<LessonEvent> {
        val merged = mutableListOf<LessonEvent>()
        for (e in events.sortedBy { it.begin }) {
            val last = merged.lastOrNull()
            if (last != null && e.begin <= last.end + joinGapMs) {
                merged[merged.size - 1] = LessonEvent(last.begin, maxOf(last.end, e.end))
            } else {
                merged += e
            }
        }
        return merged
    }
}
