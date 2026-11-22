package com.zaryif.minimalos.util

/** How long on-screen clocks should sleep, so they wake as rarely as possible. */
object Ticks {
    /** Milliseconds until the next minute starts (plus a little, so the new minute shows). */
    fun untilNextMinute(now: Long = System.currentTimeMillis()): Long = 60_000L - now % 60_000L + 50L

    /**
     * For an "Xh Ym left" / "Ym Zs left" countdown: per second in the last hour,
     * when seconds are shown; otherwise on the minute (at most 30s, to re-read the alarm).
     */
    fun countdownDelay(remainingMs: Long, now: Long = System.currentTimeMillis()): Long =
        if (remainingMs in 1..3_600_000L) 1000L - now % 1000L
        else minOf(untilNextMinute(now), 30_000L)
}
