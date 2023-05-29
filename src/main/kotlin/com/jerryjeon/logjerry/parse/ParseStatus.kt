package com.jerryjeon.logjerry.parse

sealed class ParseStatus {
    object NotStarted : ParseStatus()
    data class Proceeding(
        val percent: Int
    ) : ParseStatus()
    class Completed(
        val parseResult: ParseResult,
    ) : ParseStatus()
}
