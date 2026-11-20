package com.zaryif.minimalos.domain.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherParserTest {

    // 72 hourly entries from midnight on 20 Nov, like forecast_days=3, with "now" at 13:15.
    private fun forecastJson(currentTime: String = "2026-11-20T13:15"): String {
        val times = (0 until 72).map { h ->
            val day = 20 + h / 24
            "\"2026-11-%02dT%02d:00\"".format(day, h % 24)
        }
        val temps = (0 until 72).map { (it % 24).toDouble() / 2 }
        val codes = (0 until 72).map { if (it % 24 < 12) 3 else 61 }
        val rain = (0 until 72).map { if (it == 15) "null" else ((it * 7) % 100).toString() }
        return """
            {
              "latitude": 53.7, "longitude": -1.63, "timezone": "Europe/London",
              "current": {
                "time": "$currentTime", "interval": 900,
                "temperature_2m": 9.6, "apparent_temperature": 6.2, "relative_humidity_2m": 81,
                "weather_code": 2, "is_day": 1, "wind_speed_10m": 12.4
              },
              "hourly": {
                "time": [${times.joinToString(",")}],
                "temperature_2m": [${temps.joinToString(",")}],
                "weather_code": [${codes.joinToString(",")}],
                "precipitation_probability": [${rain.joinToString(",")}]
              },
              "daily": {
                "time": ["2026-11-20", "2026-11-21", "2026-11-22"],
                "weather_code": [61, 3, 0],
                "temperature_2m_max": [11.2, 9.8, 7.5],
                "temperature_2m_min": [4.1, 2.9, -0.6],
                "precipitation_probability_max": [80, 20, 5],
                "sunrise": ["2026-11-20T07:32", "2026-11-21T07:34", "2026-11-22T07:35"],
                "sunset": ["2026-11-20T16:05", "2026-11-21T16:04", "2026-11-22T16:03"]
              }
            }
        """.trimIndent()
    }

    @Test
    fun parsesCurrentConditions() {
        val w = WeatherParser.parseForecast(forecastJson(), "Batley", 1234L)
        assertEquals("Batley", w.placeName)
        assertEquals(9.6, w.temperature, 0.001)
        assertEquals(6.2, w.feelsLike, 0.001)
        assertEquals(2, w.code)
        assertTrue(w.isDay)
        assertEquals(12.4, w.windMph, 0.001)
        assertEquals(81, w.humidity)
        assertEquals(1234L, w.fetchedAt)
    }

    @Test
    fun hourlyStartsAtTheCurrentHourAndCoversADay() {
        val w = WeatherParser.parseForecast(forecastJson(), "Batley", 0L)
        assertEquals(24, w.hourly.size)
        assertEquals("13:00", w.hourly.first().time)
        assertEquals("12:00", w.hourly.last().time)
        assertEquals(6.5, w.hourly.first().temperature, 0.001)
        assertEquals(61, w.hourly.first().code)
        assertNull(w.hourly[2].precipitationChance) // 15:00 was null in the data
    }

    @Test
    fun lateEveningStillGetsTwentyFourHours() {
        val w = WeatherParser.parseForecast(forecastJson("2026-11-20T23:45"), "Batley", 0L)
        assertEquals(24, w.hourly.size)
        assertEquals("23:00", w.hourly.first().time)
    }

    @Test
    fun parsesDailyForecastAndSunTimes() {
        val w = WeatherParser.parseForecast(forecastJson(), "Batley", 0L)
        assertEquals(3, w.daily.size)
        val today = w.daily.first()
        assertEquals("2026-11-20", today.date)
        assertEquals(11.2, today.max, 0.001)
        assertEquals(4.1, today.min, 0.001)
        assertEquals(80, today.precipitationChance)
        assertEquals("07:32", today.sunrise)
        assertEquals("16:05", today.sunset)
        assertEquals(-0.6, w.daily[2].min, 0.001)
    }

    private val geocodeJson = """
        {"results": [
          {"id": 1, "name": "Batley", "latitude": 53.70, "longitude": -1.63, "country_code": "GB",
           "country": "United Kingdom", "admin1": "England"},
          {"id": 2, "name": "Batley", "latitude": -27.1, "longitude": 152.2, "country_code": "AU",
           "country": "Australia", "admin1": "Queensland"}
        ]}
    """.trimIndent()

    @Test
    fun geocodeTakesTopResultByDefault() {
        val place = WeatherParser.parseGeocode(geocodeJson, "Batley")!!
        assertEquals("Batley", place.name)
        assertEquals("United Kingdom", place.country)
        assertEquals(53.70, place.latitude, 0.001)
    }

    @Test
    fun geocodeUsesTheHintAfterAComma() {
        assertEquals("Australia", WeatherParser.parseGeocode(geocodeJson, "Batley, Australia")!!.country)
        assertEquals("Australia", WeatherParser.parseGeocode(geocodeJson, "Batley, queensland")!!.country)
        assertEquals("United Kingdom", WeatherParser.parseGeocode(geocodeJson, "Batley, UK")!!.country)
    }

    @Test
    fun geocodeReturnsNullWhenNothingFound() {
        assertNull(WeatherParser.parseGeocode("""{"generationtime_ms": 0.5}""", "Nowhereville"))
        assertNull(WeatherParser.parseGeocode("""{"results": []}""", "Nowhereville"))
    }

    @Test
    fun describesWeatherCodes() {
        assertEquals("clear", WeatherParser.describe(0, isDay = true))
        assertEquals("clear night", WeatherParser.describe(0, isDay = false))
        assertEquals("light rain", WeatherParser.describe(61))
        assertEquals("thunder & hail", WeatherParser.describe(99))
        assertFalse(WeatherParser.describe(42).isBlank())
    }
}
