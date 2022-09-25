package parse

import Log
import java.io.File

interface LogParser {
    fun canParse(raw: String): Boolean
    fun parse(raw: String): Log

    fun parse(file: File): List<Log> {
        return file.readLines().map { parse(it) }
    }
}
