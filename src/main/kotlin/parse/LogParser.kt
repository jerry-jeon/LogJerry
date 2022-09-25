package parse

import Log
import java.io.File

interface LogParser {
    fun canParse(raw: String): Boolean
    fun parse(rawLines: List<String>): List<Log>
}
