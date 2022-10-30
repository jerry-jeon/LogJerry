package com.jerryjeon.logjerry.parse

interface ParserFactory {

    /**
     * If the parser can't handle the sample then return null
     */
    fun create(sample: String): LogParser?

}