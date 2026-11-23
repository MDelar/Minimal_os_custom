package com.zaryif.minimalos.util

import android.content.Context
import com.zaryif.minimalos.BuildConfig
import com.zaryif.minimalos.data.preferences.UserPreferences
import com.zaryif.minimalos.domain.calendardnd.CalendarDnd
import org.json.JSONArray
import org.json.JSONObject

/**
 * Turns typed settings into JSON and back. Each value keeps its type
 * ({"t":"b","v":true}) so a restore writes exactly what was saved.
 */
object BackupCodec {
    const val FORMAT = "tilde-launcher-backup"
    /** Backups saved before the rename to Tilde Launcher. */
    private const val FORMAT_BEFORE_RENAME = "minimal-os-custom-backup"

    fun isBackupFormat(id: String) = id == FORMAT || id == FORMAT_BEFORE_RENAME
    const val VERSION = 1

    fun encode(values: Map<String, Any?>): JSONObject {
        val out = JSONObject()
        for ((key, v) in values.toSortedMap()) {
            val entry = when (v) {
                is Boolean -> typed("b", v)
                is Int -> typed("i", v)
                is Long -> typed("l", v)
                is Float -> typed("f", v.toDouble())
                is Double -> typed("d", v)
                is String -> typed("s", v)
                is Set<*> -> typed("set", JSONArray(v.map { it.toString() }.sorted()))
                else -> null
            } ?: continue
            out.put(key, entry)
        }
        return out
    }

    fun decode(json: JSONObject): Map<String, Any> {
        val out = mutableMapOf<String, Any>()
        for (key in json.keys()) {
            val e = json.optJSONObject(key) ?: continue
            val value: Any = when (e.optString("t")) {
                "b" -> e.getBoolean("v")
                "i" -> e.getInt("v")
                "l" -> e.getLong("v")
                "f" -> e.getDouble("v").toFloat()
                "d" -> e.getDouble("v")
                "s" -> e.getString("v")
                "set" -> e.getJSONArray("v").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
                else -> continue
            }
            out[key] = value
        }
        return out
    }

    private fun typed(t: String, v: Any) = JSONObject().put("t", t).put("v", v)
}

/**
 * Backs up and restores everything you've set up in the launcher: settings,
 * theme, header, widgets layout, pinned apps, custom commands, notes, to-dos,
 * weather town, prayer and Calendar DND settings. Things that only make sense on
 * this phone (the custom wallpaper file, Android widget IDs, live state) are
 * left out.
 */
object SettingsBackup {
    /** Main settings that point at things on this phone only. */
    private val DEVICE_ONLY_SETTINGS = setOf("custom_wallpaper_uri", "system_widgets", "home_system_widgets")
    private val DND_LIVE_STATE = setOf("we_silenced", "silenced_until", "handled_until")
    private val EXTRA_STORES = listOf("minimal_data", "weather", "calendar_dnd")

    suspend fun export(context: Context): String {
        val prefs = UserPreferences(context)
        val stores = JSONObject()
        stores.put("settings", BackupCodec.encode(prefs.snapshot().filterKeys { it !in DEVICE_ONLY_SETTINGS }))
        for (name in EXTRA_STORES) {
            val all = context.getSharedPreferences(name, Context.MODE_PRIVATE).all
                .filterKeys { name != "calendar_dnd" || it !in DND_LIVE_STATE }
            stores.put(name, BackupCodec.encode(all))
        }
        return JSONObject()
            .put("format", BackupCodec.FORMAT)
            .put("version", BackupCodec.VERSION)
            .put("appVersion", BuildConfig.VERSION_NAME)
            .put("createdAt", System.currentTimeMillis())
            .put("stores", stores)
            .toString(2)
    }

    data class Parsed(val createdAt: Long, val settingsCount: Int, internal val json: JSONObject)

    /** Checks the file is a launcher backup. Returns null if it isn't. */
    fun parse(text: String): Parsed? = try {
        val json = JSONObject(text)
        if (!BackupCodec.isBackupFormat(json.optString("format")) || json.optInt("version") > BackupCodec.VERSION) null
        else {
            val stores = json.getJSONObject("stores")
            Parsed(json.optLong("createdAt"), stores.optJSONObject("settings")?.length() ?: 0, json)
        }
    } catch (_: Exception) {
        null
    }

    /** Replaces the current setup with the backup's. Restart the launcher afterwards. */
    @android.annotation.SuppressLint("ApplySharedPref") // commit(): written before the restart, not after
    suspend fun restore(context: Context, backup: Parsed) {
        val stores = backup.json.getJSONObject("stores")
        stores.optJSONObject("settings")?.let { s ->
            val values = BackupCodec.decode(s).filterKeys { it !in DEVICE_ONLY_SETTINGS } + ("onboarding_completed" to true)
            UserPreferences(context).replaceAll(values, keep = DEVICE_ONLY_SETTINGS)
        }
        for (name in EXTRA_STORES) {
            val s = stores.optJSONObject(name) ?: continue
            val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
            for ((key, v) in BackupCodec.decode(s)) {
                if (name == "calendar_dnd" && key in DND_LIVE_STATE) continue
                @Suppress("UNCHECKED_CAST")
                when (v) {
                    is Boolean -> editor.putBoolean(key, v)
                    is Int -> editor.putInt(key, v)
                    is Long -> editor.putLong(key, v)
                    is Float -> editor.putFloat(key, v)
                    is Double -> editor.putFloat(key, v.toFloat())
                    is String -> editor.putString(key, v)
                    is Set<*> -> editor.putStringSet(key, v as Set<String>)
                }
            }
            editor.commit()
        }
        CalendarDnd.sync(context)
    }
}
