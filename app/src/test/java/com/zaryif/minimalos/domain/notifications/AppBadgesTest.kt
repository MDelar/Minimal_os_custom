package com.zaryif.minimalos.domain.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class AppBadgesTest {
    private fun n(pkg: String, ongoing: Boolean = false, summary: Boolean = false, badge: Boolean = true) =
        BadgeCandidate(pkg, ongoing, summary, badge)

    @Test
    fun countsOnePerChatAndIgnoresTheGroupHeader() {
        val counts = AppBadges.count(
            listOf(n("com.whatsapp"), n("com.whatsapp"), n("com.whatsapp", summary = true)),
            ownPackage = "com.minimal.os"
        )
        assertEquals(mapOf("com.whatsapp" to 2), counts)
    }

    @Test
    fun ignoresOngoingOwnAndBadgeDisabledNotifications() {
        val counts = AppBadges.count(
            listOf(
                n("com.waze", ongoing = true),
                n("com.google.android.projection.gearhead", ongoing = true),
                n("com.minimal.os"),
                n("com.google.android.gm", badge = false),
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
