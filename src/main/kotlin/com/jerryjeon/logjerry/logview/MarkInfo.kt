package com.jerryjeon.logjerry.logview

sealed class MarkInfo {
    class Marked(
        val markedLog: RefinedLog,
    ) : MarkInfo()

    class StatBetweenMarks(
        val logCount: Int,
        val duration: String?, // ex) 1h 2m 3s, and null if it's not able to calculate
    ) : MarkInfo()
}
