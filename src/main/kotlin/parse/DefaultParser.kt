package parse

import Log
import java.util.concurrent.atomic.AtomicInteger

class DefaultParser : LogParser {

    private val number = AtomicInteger(1)
    override fun canParse(raw: String): Boolean {
        // TODO check
        return true
    }

    override fun parse(rawLines: List<String>): ParseResult {
        val logs = mutableListOf<Log>()
        val invalidSentences = mutableListOf<Pair<Int, String>>()
        var lastLog: Log? = null
        rawLines.forEachIndexed { index, s ->
            lastLog = try {
                val log = parseSingleLineLog(s)
                lastLog?.let { logs.add(it) }
                log
            } catch (e: Exception) {
                val continuedLog = if (lastLog == null) {
                    invalidSentences.add(index to s)
                    return@forEachIndexed
                } else {
                    lastLog!!
                }
                continuedLog.copy(log = continuedLog.log + s)
            }
        }
        return ParseResult(logs, invalidSentences)
    }
    private fun parseSingleLineLog(raw: String): Log {
        val split = raw.split(" ")

        val date = split[0]
        val time = split[1]

        val thirdSegment = split[2].split("-", "/")
        val pid = thirdSegment[0].toLong()
        val tid = thirdSegment[1].toLong()
        val packageName = thirdSegment[2].takeIf { it != "?" }

        val fourthSegment = split[3].split("/")
        val priority = fourthSegment[0]
        val tag = fourthSegment[1].removeSuffix(":")

        val log = split.subList(4, split.size).joinToString(separator = " ")

        return Log(number.getAndIncrement(), date, time, pid, tid, packageName, priority, tag, log)
    }
}
