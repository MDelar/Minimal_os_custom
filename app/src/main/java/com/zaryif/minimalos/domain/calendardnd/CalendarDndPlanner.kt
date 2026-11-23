package com.zaryif.minimalos.domain.calendardnd

/** A timed event from one of the chosen calendars. */
data class DndEvent(val begin: Long, val end: Long)

/** What should happen now, and when to look again. */
data class DndPlan(
    /** End of the event (or run of back-to-back events) under way, or null if none. */
    val silenceUntil: Long?,
    /** When to check again: the end of this block, the next start, or a safety re-check. */
    val nextCheck: Long
)

/**
 * Pure scheduling logic for Calendar DND. Events that overlap or sit within
 * [joinGapMs] of each other (a changeover between lessons or meetings) count
 * as one block, so the phone doesn't buzz for a moment in between.
 */
object CalendarDndPlanner {
    const val JOIN_GAP_MS = 5 * 60_000L
    /** Re-check at least this often, so edited or newly synced events are picked up. */
    const val MAX_WAIT_MS = 3 * 60 * 60_000L
    /** An alarm can land a moment early; treat an event starting this soon as started. */
    const val START_TOLERANCE_MS = 15_000L

    fun plan(events: List<DndEvent>, now: Long, joinGapMs: Long = JOIN_GAP_MS): DndPlan {
        val blocks = mergeBlocks(events.filter { it.end > it.begin }, joinGapMs)
        val current = blocks.firstOrNull { it.begin <= now + START_TOLERANCE_MS && it.end > now }
        val safety = now + MAX_WAIT_MS
        if (current != null) return DndPlan(current.end, minOf(current.end, safety))
        val next = blocks.firstOrNull { it.begin > now }
        return DndPlan(null, minOf(next?.begin ?: safety, safety))
    }

    internal fun mergeBlocks(events: List<DndEvent>, joinGapMs: Long): List<DndEvent> {
        val merged = mutableListOf<DndEvent>()
        for (e in events.sortedBy { it.begin }) {
            val last = merged.lastOrNull()
            if (last != null && e.begin <= last.end + joinGapMs) {
                merged[merged.size - 1] = DndEvent(last.begin, maxOf(last.end, e.end))
            } else {
                merged += e
            }
        }
        return merged
    }
}
