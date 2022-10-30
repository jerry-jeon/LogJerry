package com.jerryjeon.logjerry.parse

interface LogParser {
    fun parse(rawLines: List<String>): ParseResult
}
