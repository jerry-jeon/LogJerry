package com.jerryjeon.logjerry.log

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class Log(
    val number: Int,
    val date: String?,
    val time: String?,
    val pid: Long?,
    val tid: Long?,
    val packageName: String?,
    val priorityText: String,
    val tag: String?,
    val log: String
) {
    val index = number - 1
    val priority = Priority.find(priorityText)

    val localDateTime: LocalDateTime?
        get() = try {
            if (date != null && time != null) {
                LocalDateTime.parse("$date $time", formatter)
            } else if (time != null) {
                // Assume date is today.
                LocalTime.parse(time, timeFormatter).atDate(LocalDate.now())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    fun durationBetween(other: Log): Duration? {
        val thisLocalDateTime = localDateTime ?: return null
        val otherLocalDateTime = other.localDateTime ?: return null
        return Duration.between(thisLocalDateTime, otherLocalDateTime)
    }

    companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    }
}

enum class Priority(val text: String, val fullText: String, val level: Int) {
    Verbose("V", "Verb", 1),
    Debug("D", "Debug", 2),
    Info("I", "Info", 3),
    Warning("W", "Warn", 4),
    Error("E", "Error", 5);

    companion object {
        fun find(text: String): Priority {
            return values().find { it.text == text }
                ?: throw IllegalArgumentException("Not defined priority: $text")
        }
    }
}
