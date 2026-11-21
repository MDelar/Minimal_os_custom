package com.zaryif.minimalos.domain.notifications

/** One active notification, reduced to what badge counting needs. */
data class BadgeCandidate(
    val packageName: String,
    /** Ongoing or foreground service: navigation, music, Android Auto, charging. */
    val ongoing: Boolean,
    /** The bundle header a chat app posts above its per-chat notifications. */
    val groupSummary: Boolean,
    /** The app or its channel allows launcher badges (Android's own setting). */
    val canShowBadge: Boolean = true
)

/**
 * Notification counts per app for the app drawer: one per notification you'd
 * actually want to read, so persistent ones and group headers don't count.
 */
object AppBadges {
    fun count(candidates: List<BadgeCandidate>, ownPackage: String): Map<String, Int> =
        candidates
            .filter { it.packageName != ownPackage && !it.ongoing && !it.groupSummary && it.canShowBadge }
            .groupingBy { it.packageName }
            .eachCount()

    /** "3", or "99+" for silly numbers. */
    fun label(count: Int): String = if (count > 99) "99+" else count.toString()
}
