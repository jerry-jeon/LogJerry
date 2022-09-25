package parse

interface LogParser {
    fun canParse(raw: String): Boolean
    fun parse(rawLines: List<String>): ParseResult
}
