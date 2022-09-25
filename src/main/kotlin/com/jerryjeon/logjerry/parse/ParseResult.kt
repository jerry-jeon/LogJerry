package com.jerryjeon.logjerry.parse

import Log

data class ParseResult(
    val logs: List<Log>,
    val invalidSentences: List<Pair<Int, String>>
)
