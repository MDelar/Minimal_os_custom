package com.zaryif.minimalos.domain.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class AppBadgesTest {
    private fun n(pkg: String, ongoing: Boolean = false, summary: Boolean = false, number: Int = 0) =
        BadgeCandidate(pkg, ongoing, summary, number)

    @Test
    fun countsOnePerChatAndIgnoresTheGroupHeader() {
        val counts = AppBadges.count(
            listOf(n("com.whatsapp"), n("com.whatsapp"), n("com.whatsapp", summary = true)),
            ownPackage = "com.minimal.os"
        )
        assertEquals(mapOf("com.whatsapp" to 2), counts)
    }

    @Test
    fun appThatOnlyPostsAHeaderStillCounts() {
        val counts = AppBadges.count(
            listOf(n("com.google.android.gm", summary = true, number = 6), n("com.example", summary = true)),
            ownPackage = "com.minimal.os"
        )
        assertEquals(mapOf("com.google.android.gm" to 6, "com.example" to 1), counts)
    }

    @Test
    fun ignoresOngoingAndOwnNotifications() {
        val counts = AppBadges.count(
            listOf(
                n("com.waze", ongoing = true),
                n("com.google.android.projection.gearhead", ongoing = true),
                n("com.minimal.os"),
                n("com.snapchat.android")
            ),
            ownPackage = "com.minimal.os"
        )
        assertEquals(mapOf("com.snapchat.android" to 1), counts)
    }

    @Test
    fun capsTheLabel() {
        assertEquals("7", AppBadges.label(7))
        assertEquals("99+", AppBadges.label(140))
    }
}
