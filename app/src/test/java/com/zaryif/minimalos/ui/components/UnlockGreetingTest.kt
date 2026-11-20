package com.zaryif.minimalos.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class UnlockGreetingTest {
    @Test
    fun usesTheNameBeforeTheAt() {
        assertEquals("good evening, mdelar", greeting("mdelar@titan:~$", LocalTime.of(18, 30)))
        assertEquals("good morning, mdelar", greeting("mdelar@titan:~$", LocalTime.of(7, 0)))
    }

    @Test
    fun plainGreetingWithoutAUserName() {
        assertEquals("good afternoon", greeting("~$", LocalTime.of(13, 0)))
        assertEquals("up late", greeting("minimal", LocalTime.of(1, 15)))
        assertEquals("good evening", greeting("my phone@home", LocalTime.of(20, 0)))
    }
}
