package com.zaryif.minimalos.domain.weather

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class WeatherPlace(
    val name: String,
    val region: String?,
    val country: String?,
    val latitude: Double,
    val longitude: Double
)

data class HourForecast(
    /** Local time as "HH:mm". */
    val time: String,
    val temperature: Double,
    val code: Int,
    val precipitationChance: Int?
)

data class DayForecast(
    /** Local date as "yyyy-MM-dd". */
    val date: String,
    val code: Int,
    val max: Double,
    val min: Double,
    val precipitationChance: Int?,
    /** Local time as "HH:mm". */
    val sunrise: String?,
    val sunset: String?
)

data class WeatherSnapshot(
    val placeName: String,
    val temperature: Double,
    val feelsLike: Double,
    val code: Int,
    val isDay: Boolean,
    val windMph: Double,
    val humidity: Int,
    /** The next 24 hours, starting with the current hour. */
    val hourly: List<HourForecast>,
    /** Today first, then the following days. */
    val daily: List<DayForecast>,
    val fetchedAt: Long
)

/** Turns Open-Meteo responses into weather data. Kept free of Android calls so it can be unit tested. */
object WeatherParser {

    fun parseForecast(json: String, placeName: String, fetchedAt: Long): WeatherSnapshot {
        val root = JSONObject(json)
        val current = root.getJSONObject("current")
        val currentHour = current.getString("time").take(13) // "yyyy-MM-ddTHH"

        val hourlyJson = root.getJSONObject("hourly")
        val times = hourlyJson.getJSONArray("time")
        val temps = hourlyJson.getJSONArray("temperature_2m")
        val codes = hourlyJson.getJSONArray("weather_code")
        val rain = hourlyJson.optJSONArray("precipitation_probability")
        var start = 0
        while (start < times.length() && times.getString(start).take(13) < currentHour) start++
        val hourly = (start until minOf(start + 24, times.length())).map { i ->
            HourForecast(
                time = times.getString(i).substring(11, 16),
                temperature = temps.getDouble(i),
                code = codes.getInt(i),
                precipitationChance = rain?.takeUnless { it.isNull(i) }?.getInt(i)
            )
        }

        val dailyJson = root.getJSONObject("daily")
        val days = dailyJson.getJSONArray("time")
        val dailyCodes = dailyJson.getJSONArray("weather_code")
        val maxes = dailyJson.getJSONArray("temperature_2m_max")
        val mins = dailyJson.getJSONArray("temperature_2m_min")
        val dailyRain = dailyJson.optJSONArray("precipitation_probability_max")
        val sunrises = dailyJson.optJSONArray("sunrise")
        val sunsets = dailyJson.optJSONArray("sunset")
        val daily = (0 until days.length()).map { i ->
            DayForecast(
                date = days.getString(i),
                code = dailyCodes.getInt(i),
                max = maxes.getDouble(i),
                min = mins.getDouble(i),
                precipitationChance = dailyRain?.takeUnless { it.isNull(i) }?.getInt(i),
                sunrise = sunrises?.takeUnless { it.isNull(i) }?.getString(i)?.substring(11, 16),
                sunset = sunsets?.takeUnless { it.isNull(i) }?.getString(i)?.substring(11, 16)
            )
        }

        return WeatherSnapshot(
            placeName = placeName,
            temperature = current.getDouble("temperature_2m"),
            feelsLike = current.getDouble("apparent_temperature"),
            code = current.getInt("weather_code"),
            isDay = current.optInt("is_day", 1) == 1,
            windMph = current.optDouble("wind_speed_10m", 0.0),
            humidity = current.optInt("relative_humidity_2m", 0),
            hourly = hourly,
            daily = daily,
            fetchedAt = fetchedAt
        )
    }

    /**
     * Picks the best match for what was typed. "Batley" takes the top result;
     * "Batley, UK" or "Springfield, Illinois" prefers a result whose country or
     * region matches the part after the comma.
     */
    fun parseGeocode(json: String, query: String): WeatherPlace? {
        val results = JSONObject(json).optJSONArray("results") ?: return null
        if (results.length() == 0) return null
        val hint = query.substringAfter(',', "").trim().lowercase(Locale.ROOT)
        val places = (0 until results.length()).map { i ->
            val r = results.getJSONObject(i)
            WeatherPlace(
                name = r.getString("name"),
                region = r.optString("admin1").takeIf { it.isNotBlank() },
                country = r.optString("country").takeIf { it.isNotBlank() },
                latitude = r.getDouble("latitude"),
                longitude = r.getDouble("longitude")
            ) to r.optString("country_code").lowercase(Locale.ROOT)
        }
        if (hint.isNotEmpty()) {
            val ukAliases = setOf("uk", "united kingdom", "gb", "great britain", "england", "scotland", "wales", "northern ireland")
            places.firstOrNull { (place, code) ->
                val country = place.country?.lowercase(Locale.ROOT).orEmpty()
                val region = place.region?.lowercase(Locale.ROOT).orEmpty()
                country.contains(hint) || region.contains(hint) || code == hint ||
                    (hint in ukAliases && code == "gb")
            }?.let { return it.first }
        }
        return places.first().first
    }

    /** Short, lowercase description for a WMO weather code, as Open-Meteo reports them. */
    fun describe(code: Int, isDay: Boolean = true): String = when (code) {
        0 -> if (isDay) "clear" else "clear night"
        1 -> "mostly clear"
        2 -> "partly cloudy"
        3 -> "overcast"
        45, 48 -> "fog"
        51 -> "light drizzle"
        53 -> "drizzle"
        55 -> "heavy drizzle"
        56, 57 -> "freezing drizzle"
        61 -> "light rain"
        63 -> "rain"
        65 -> "heavy rain"
        66, 67 -> "freezing rain"
        71 -> "light snow"
        73 -> "snow"
        75 -> "heavy snow"
        77 -> "snow grains"
        80 -> "light showers"
        81 -> "showers"
        82 -> "heavy showers"
        85, 86 -> "snow showers"
        95 -> "thunderstorm"
        96, 99 -> "thunder & hail"
        else -> "—"
    }
}

/**
 * Fetches and caches the forecast for the town you set. The launcher only goes
 * online for this, and only while the weather card is on screen. Forecasts come
 * from Open-Meteo (open-meteo.com): free, no account or API key.
 */
object WeatherRepository {

    private const val PREFS = "weather"
    private const val KEY_NAME = "place_name"
    private const val KEY_LAT = "place_lat"
    private const val KEY_LON = "place_lon"
    private const val KEY_JSON = "last_forecast_json"
    private const val KEY_FETCHED = "last_fetched_at"

    /** Forecasts younger than this are reused rather than fetched again. */
    const val REFRESH_AFTER_MS = 30 * 60 * 1000L

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun placeName(context: Context): String? = prefs(context).getString(KEY_NAME, null)

    /** The saved town's coordinates, or null if no town is set. */
    fun location(context: Context): Pair<Double, Double>? {
        val p = prefs(context)
        val lat = p.getString(KEY_LAT, null)?.toDoubleOrNull() ?: return null
        val lon = p.getString(KEY_LON, null)?.toDoubleOrNull() ?: return null
        return lat to lon
    }

    fun cached(context: Context): WeatherSnapshot? {
        val p = prefs(context)
        val json = p.getString(KEY_JSON, null) ?: return null
        val name = p.getString(KEY_NAME, null) ?: return null
        return try {
            WeatherParser.parseForecast(json, name, p.getLong(KEY_FETCHED, 0L))
        } catch (_: Exception) {
            null
        }
    }

    /** Looks the town up and saves it. Returns null if it can't be found or the network fails. */
    suspend fun setTown(context: Context, query: String): WeatherPlace? = withContext(Dispatchers.IO) {
        val name = query.substringBefore(',').trim()
        if (name.isEmpty()) return@withContext null
        val url = "https://geocoding-api.open-meteo.com/v1/search?count=10&language=en&format=json&name=" +
            URLEncoder.encode(name, "UTF-8")
        val place = try {
            WeatherParser.parseGeocode(get(url), query)
        } catch (_: Exception) {
            null
        } ?: return@withContext null
        prefs(context).edit()
            .putString(KEY_NAME, place.name)
            .putString(KEY_LAT, place.latitude.toString())
            .putString(KEY_LON, place.longitude.toString())
            .remove(KEY_JSON)
            .remove(KEY_FETCHED)
            .apply()
        place
    }

    /**
     * Returns the forecast, fetching a new one if the saved one is older than
     * [REFRESH_AFTER_MS] (or [force] is set). Falls back to the saved forecast
     * when offline. Returns null if no town is set and nothing is saved.
     */
    suspend fun refresh(context: Context, force: Boolean = false): WeatherSnapshot? = withContext(Dispatchers.IO) {
        val p = prefs(context)
        val name = p.getString(KEY_NAME, null) ?: return@withContext null
        val lat = p.getString(KEY_LAT, null) ?: return@withContext null
        val lon = p.getString(KEY_LON, null) ?: return@withContext null
        val age = System.currentTimeMillis() - p.getLong(KEY_FETCHED, 0L)
        if (!force && age in 0 until REFRESH_AFTER_MS) cached(context)?.let { return@withContext it }

        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,is_day,wind_speed_10m" +
            "&hourly=temperature_2m,weather_code,precipitation_probability" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset" +
            "&timezone=auto&forecast_days=3&wind_speed_unit=mph"
        try {
            val json = get(url)
            val now = System.currentTimeMillis()
            val snapshot = WeatherParser.parseForecast(json, name, now)
            p.edit().putString(KEY_JSON, json).putLong(KEY_FETCHED, now).apply()
            snapshot
        } catch (_: Exception) {
            cached(context)
        }
    }

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "TildeLauncher")
            if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}")
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
