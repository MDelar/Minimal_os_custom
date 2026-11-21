package com.zaryif.minimalos.domain.notifications

/** One active notification, reduced to what badge counting needs. */
data class BadgeCandidate(
    val packageName: String,
    /** Ongoing or foreground service: navigation, music, Android Auto, charging. */
    val ongoing: Boolean,
    /** The bundle header an app posts above its individual notifications. */
    val groupSummary: Boolean,
    /** The count an app attaches itself (Notification.number), 0 if none. */
    val number: Int = 0
)

/**
 * Notification counts per app for the app drawer: one per notification you'd
 * actually want to read, so persistent ones don't count. Bundle headers aren't
 * counted on top of the notifications they hold, but an app that only shows a
 * header (Gmail's "5 new emails") still counts, using its own number if given.
 */
object AppBadges {
    fun count(candidates: List<BadgeCandidate>, ownPackage: String): Map<String, Int> =
        candidates
            .filter { it.packageName != ownPackage && !it.ongoing }
            .groupBy { it.packageName }
            .mapValues { (_, list) ->
                val individual = list.count { !it.groupSummary }
                if (individual > 0) individual
                else list.maxOf { it.number }.coerceAtLeast(1)
            }

    /** "3", or "99+" for silly numbers. */
    fun label(count: Int): String = if (count > 99) "99+" else count.toString()
}
