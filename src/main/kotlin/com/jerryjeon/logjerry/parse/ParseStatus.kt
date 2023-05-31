package com.jerryjeon.logjerry.parse

import com.jerryjeon.logjerry.log.ParseCompleted

sealed class ParseStatus {
    object NotStarted : ParseStatus()
    data class Proceeding(
        val percent: Int
    ) : ParseStatus()
    class Completed(
        val parseResult: ParseResult,
        val parseCompleted: ParseCompleted,
    ) : ParseStatus()
}
