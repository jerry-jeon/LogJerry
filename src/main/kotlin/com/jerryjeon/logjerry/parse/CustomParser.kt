package com.jerryjeon.logjerry.parse

class CustomParser : LogParser {
    override fun parse(rawLines: List<String>): ParseResult {
        TODO("Not yet implemented")
    }

    companion object : ParserFactory {
        override fun create(sample: String): LogParser? {
            return null
        }
    }
}
