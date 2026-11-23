package com.zaryif.minimalos.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.json.JSONObject

class BackupCodecTest {
    @Test
    fun everyTypeSurvivesARoundTrip() {
        val values: Map<String, Any> = mapOf(
            "theme_id" to "tokyo_night",
            "unlock_animation" to true,
            "pomodoro_work_minutes" to 25,
            "silenced_until" to 1_790_000_000_000L,
            "scale" to 1.5f,
            "lat" to 53.70,
            "favorite_apps" to setOf("com.whatsapp", "com.google.android.gm"),
            "custom_commands_json" to """[{"name":"hello"}]"""
        )
        // Through text, as in the saved file.
        val back = BackupCodec.decode(JSONObject(BackupCodec.encode(values).toString()))
        assertEquals(values, back)
    }

    @Test
    fun unknownEntriesAreSkipped() {
        val json = JSONObject("""{"a":{"t":"s","v":"x"},"b":{"t":"weird","v":1},"c":"not an entry"}""")
        assertEquals(mapOf("a" to "x"), BackupCodec.decode(json))
    }

    @Test
    fun parseRejectsFilesThatAreNotBackups() {
        assertNull(SettingsBackup.parse("hello"))
        assertNull(SettingsBackup.parse("""{"format":"something-else","version":1,"stores":{}}"""))
        assertNull(SettingsBackup.parse("""{"format":"tilde-launcher-backup","version":99,"stores":{}}"""))
        val ok = SettingsBackup.parse(
            """{"format":"tilde-launcher-backup","version":1,"createdAt":5,"stores":{"settings":{"a":{"t":"b","v":true}}}}"""
        )
        assertTrue(ok != null && ok.createdAt == 5L && ok.settingsCount == 1)
        // Backups saved before the rename still restore.
        assertTrue(SettingsBackup.parse("""{"format":"minimal-os-custom-backup","version":1,"stores":{}}""") != null)
    }
}
