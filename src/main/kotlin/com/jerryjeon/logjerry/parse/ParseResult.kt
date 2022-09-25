package com.jerryjeon.logjerry.parse

import com.jerryjeon.logjerry.log.Log

data class ParseResult(
    val logs: List<Log>,
    val invalidSentences: List<Pair<Int, String>>
)
