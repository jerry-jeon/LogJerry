package com.jerryjeon.logjerry.log
data class Log(
    val number: Int,
    val date: String,
    val time: String,
    val pid: Long,
    val tid: Long,
    val packageName: String?,
    val priorityText: String,
    val tag: String,
    val log: String
) {
    val priority = Priority.find(priorityText)
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
