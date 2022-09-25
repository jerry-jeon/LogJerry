package com.jerryjeon.logjerry.parse

interface LogParser {
    fun canParse(raw: String): Boolean
    fun parse(rawLines: List<String>): ParseResult
}
