package com.zaryif.minimalos.domain.parser

import java.util.Calendar
import java.util.Locale

data class ParsedDateTime(
    val cleanTitle: String,
    val calendar: Calendar,
    val startMillis: Long,
    val hour: Int,
    val minute: Int,
    val hasExplicitDate: Boolean,
    val hasExplicitTime: Boolean
)

/**
 * Intelligent Natural Language Date and Time parser.
 * Handles relative days (today, tomorrow, day after tomorrow),
 * calendar dates (25th Oct, October 25, 25/10),
 * weekdays (Monday, Friday, this Friday, next Tuesday),
 * times with dot or colon ("7.30 am", "7: 30 am", "8.30pm", "7 am"),
 * and relative offsets ("in 2 hours", "in 30 mins").
 */
object NaturalDateTimeParser {

    private val MONTH_MAP = mapOf(
        "jan" to 0, "january" to 0,
        "feb" to 1, "february" to 1,
        "mar" to 2, "march" to 2,
        "apr" to 3, "april" to 3,
        "may" to 4,
        "jun" to 5, "june" to 5,
        "jul" to 6, "july" to 6,
        "aug" to 7, "august" to 7,
        "sep" to 8, "september" to 8,
        "oct" to 9, "october" to 9,
        "nov" to 10, "november" to 10,
        "dec" to 11, "december" to 11
    )

    private val WEEKDAY_MAP = mapOf(
        "sun" to Calendar.SUNDAY, "sunday" to Calendar.SUNDAY,
        "mon" to Calendar.MONDAY, "monday" to Calendar.MONDAY,
        "tue" to Calendar.TUESDAY, "tuesday" to Calendar.TUESDAY,
        "wed" to Calendar.WEDNESDAY, "wednesday" to Calendar.WEDNESDAY,
        "thu" to Calendar.THURSDAY, "thursday" to Calendar.THURSDAY,
        "fri" to Calendar.FRIDAY, "friday" to Calendar.FRIDAY,
        "sat" to Calendar.SATURDAY, "saturday" to Calendar.SATURDAY
    )

    fun parse(rawInput: String, defaultTitle: String = "Event"): ParsedDateTime {
        val cal = Calendar.getInstance()
        var workingText = rawInput.trim()
        var hasExplicitDate = false
        var hasExplicitTime = false

        // 1. Relative offset expressions like "in 2 hours", "in 30 mins"
        val inOffsetRegex = Regex("(?i)\\bin\\s+(\\d+)\\s*(hours?|hrs?|minutes?|mins?|days?)\\b")
        val inOffsetMatch = inOffsetRegex.find(workingText)
        if (inOffsetMatch != null) {
            val amount = inOffsetMatch.groupValues[1].toIntOrNull() ?: 1
            val unit = inOffsetMatch.groupValues[2].lowercase(Locale.getDefault())
            when {
                unit.startsWith("h") -> cal.add(Calendar.HOUR_OF_DAY, amount)
                unit.startsWith("m") -> cal.add(Calendar.MINUTE, amount)
                unit.startsWith("d") -> cal.add(Calendar.DAY_OF_YEAR, amount)
            }
            workingText = workingText.removeRange(inOffsetMatch.range).trim()
            hasExplicitDate = true
            hasExplicitTime = true
        }

        // 2. Relative day phrases: "day after tomorrow", "tomorrow", "tmrw", "today", "tonight"
        if (!hasExplicitDate) {
            val dayAfterTomorrowRegex = Regex("(?i)\\b(?:the\\s+)?day\\s+after\\s+tomorrow\\b")
            if (dayAfterTomorrowRegex.containsMatchIn(workingText)) {
                cal.add(Calendar.DAY_OF_YEAR, 2)
                workingText = workingText.replace(dayAfterTomorrowRegex, "").trim()
                hasExplicitDate = true
            } else {
                val tomorrowRegex = Regex("(?i)\\b(tomorrow|tmrw|tom)\\b")
                if (tomorrowRegex.containsMatchIn(workingText)) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                    workingText = workingText.replace(tomorrowRegex, "").trim()
                    hasExplicitDate = true
                } else {
                    val todayRegex = Regex("(?i)\\b(today)\\b")
                    if (todayRegex.containsMatchIn(workingText)) {
                        workingText = workingText.replace(todayRegex, "").trim()
                        hasExplicitDate = true
                    }
                }
            }
        }

        // 3. Weekday matching: "this friday", "next monday", "on wednesday"
        if (!hasExplicitDate) {
            val weekdayRegex = Regex("(?i)\\b(?:on\\s+|this\\s+|next\\s+)?(monday|tuesday|wednesday|thursday|friday|saturday|sunday|mon|tue|wed|thu|fri|sat|sun)\\b")
            val weekdayMatch = weekdayRegex.find(workingText)
            if (weekdayMatch != null) {
                val dayName = weekdayMatch.groupValues[1].lowercase(Locale.getDefault())
                val targetDayOfWeek = WEEKDAY_MAP[dayName]
                if (targetDayOfWeek != null) {
                    val currentDay = cal.get(Calendar.DAY_OF_WEEK)
                    var daysToAdd = (targetDayOfWeek - currentDay + 7) % 7
                    if (daysToAdd == 0) daysToAdd = 7
                    cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    workingText = workingText.removeRange(weekdayMatch.range).trim()
                    hasExplicitDate = true
                }
            }
        }

        // 4. Explicit calendar date: "25th oct", "october 25th", "25 october", "oct 25"
        if (!hasExplicitDate) {
            // "25th october" or "25 oct"
            val dayMonthRegex = Regex("(?i)\\b(\\d{1,2})(?:st|nd|rd|th)?\\s+(?:of\\s+)?(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\\b")
            val dmMatch = dayMonthRegex.find(workingText)
            if (dmMatch != null) {
                val day = dmMatch.groupValues[1].toIntOrNull()
                val monthName = dmMatch.groupValues[2].lowercase(Locale.getDefault())
                val month = MONTH_MAP[monthName]
                if (day != null && month != null && day in 1..31) {
                    cal.set(Calendar.MONTH, month)
                    cal.set(Calendar.DAY_OF_MONTH, day)
                    val now = Calendar.getInstance()
                    val isPastDate = (cal.get(Calendar.MONTH) < now.get(Calendar.MONTH)) ||
                        (cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.DAY_OF_MONTH) < now.get(Calendar.DAY_OF_MONTH))
                    if (isPastDate) {
                        cal.add(Calendar.YEAR, 1)
                    }
                    workingText = workingText.removeRange(dmMatch.range).trim()
                    hasExplicitDate = true
                }
            } else {
                // "october 25th" or "oct 25"
                val monthDayRegex = Regex("(?i)\\b(jan(?:uary)?|feb(?:ruary)?|mar(?:ch)?|apr(?:il)?|may|jun(?:e)?|jul(?:y)?|aug(?:ust)?|sep(?:tember)?|oct(?:ober)?|nov(?:ember)?|dec(?:ember)?)\\s+(\\d{1,2})(?:st|nd|rd|th)?\\b")
                val mdMatch = monthDayRegex.find(workingText)
                if (mdMatch != null) {
                    val monthName = mdMatch.groupValues[1].lowercase(Locale.getDefault())
                    val day = mdMatch.groupValues[2].toIntOrNull()
                    val month = MONTH_MAP[monthName]
                    if (day != null && month != null && day in 1..31) {
                        cal.set(Calendar.MONTH, month)
                        cal.set(Calendar.DAY_OF_MONTH, day)
                        val now = Calendar.getInstance()
                        val isPastDate = (cal.get(Calendar.MONTH) < now.get(Calendar.MONTH)) ||
                            (cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.DAY_OF_MONTH) < now.get(Calendar.DAY_OF_MONTH))
                        if (isPastDate) {
                            cal.add(Calendar.YEAR, 1)
                        }
                        workingText = workingText.removeRange(mdMatch.range).trim()
                        hasExplicitDate = true
                    }
                }
            }
        }

        // 5. Time parsing: "at 7.30 am", "7: 30 am", "7.30 am", "7 am", "at 7pm", "19:30", "7:30"
        var hour = cal.get(Calendar.HOUR_OF_DAY) + 1
        var minute = 0

        // Explicit time patterns (avoids bare numbers like "2" in "Buy 2 laptops"):
        // 1) "at 7", "at 7:30", "at 7am", "at 7:30 pm"
        // 2) "7:30", "7.30", "7:30am", "7:30 pm", "19:30" (with explicit colon/dot minutes)
        // 3) "7am", "7 pm" (with explicit am/pm)
        val timeRegex = Regex("(?i)\\b(?:at\\s+(\\d{1,2})(?:\\s*[:.]\\s*(\\d{2}))?\\s*(am|pm)?|(\\d{1,2})\\s*[:.]\\s*(\\d{2})\\s*(am|pm)?|(\\d{1,2})\\s*(am|pm))\\b")
        val timeMatch = timeRegex.find(workingText)
        if (timeMatch != null) {
            val hStr = timeMatch.groups[1]?.value ?: timeMatch.groups[4]?.value ?: timeMatch.groups[7]?.value ?: ""
            val mStr = timeMatch.groups[2]?.value ?: timeMatch.groups[5]?.value ?: ""
            val ampm = (timeMatch.groups[3]?.value ?: timeMatch.groups[6]?.value ?: timeMatch.groups[8]?.value ?: "").lowercase(Locale.getDefault())

            val rawHour = hStr.toIntOrNull()
            val rawMin = mStr.toIntOrNull() ?: 0

            if (rawHour != null && rawHour in 0..23) {
                hour = rawHour
                minute = rawMin
                if (ampm == "pm" && hour < 12) hour += 12
                else if (ampm == "am" && hour == 12) hour = 0
                else if (ampm.isBlank() && !defaultTitle.equals("alarm", ignoreCase = true) && hour in 1..7 && !workingText.contains("morning", ignoreCase = true)) {
                    hour += 12 // Assume 2-7 means PM for afternoon/evening events, but not for alarms
                }
                workingText = workingText.removeRange(timeMatch.range).trim()
                hasExplicitTime = true
            }
        } else {
            // Named times of day
            val tonightRegex = Regex("(?i)\\btonight\\b")
            if (tonightRegex.containsMatchIn(workingText)) {
                hour = 20
                minute = 0
                workingText = workingText.replace(tonightRegex, "").trim()
                hasExplicitTime = true
            }
            val noonRegex = Regex("(?i)\\bnoon\\b")
            if (noonRegex.containsMatchIn(workingText)) {
                hour = 12
                minute = 0
                workingText = workingText.replace(noonRegex, "").trim()
                hasExplicitTime = true
            }
            val morningRegex = Regex("(?i)\\b(?:in\\s+the\\s+)?morning\\b")
            if (morningRegex.containsMatchIn(workingText)) {
                hour = 9
                minute = 0
                workingText = workingText.replace(morningRegex, "").trim()
                hasExplicitTime = true
            }
            val eveningRegex = Regex("(?i)\\b(?:in\\s+the\\s+)?evening\\b")
            if (eveningRegex.containsMatchIn(workingText)) {
                hour = 18
                minute = 0
                workingText = workingText.replace(eveningRegex, "").trim()
                hasExplicitTime = true
            }
        }

        // Clean up remaining dangling prepositions & conjunctions
        workingText = workingText.replace(Regex("(?i)\\b(at|on|for|in|this|next|of)\\s*$"), "").trim()
        workingText = workingText.replace(Regex("(?i)^\\b(at|on|for|in|this|next|of)\\s+"), "").trim()
        workingText = workingText.replace(Regex("\\s{2,}"), " ").trim()

        if (workingText.isBlank()) {
            workingText = defaultTitle
        }

        cal.set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
        cal.set(Calendar.MINUTE, minute.coerceIn(0, 59))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val cleanTitle = workingText.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

        return ParsedDateTime(
            cleanTitle = cleanTitle,
            calendar = cal,
            startMillis = cal.timeInMillis,
            hour = hour.coerceIn(0, 23),
            minute = minute.coerceIn(0, 59),
            hasExplicitDate = hasExplicitDate,
            hasExplicitTime = hasExplicitTime
        )
    }
}
