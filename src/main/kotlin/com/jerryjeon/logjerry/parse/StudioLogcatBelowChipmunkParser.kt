package com.jerryjeon.logjerry.parse

import com.jerryjeon.logjerry.log.Log
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicInteger

data class StudioLogcatBelowChipmunkParser(
    // Log format configuration before AS Chipmunk version
    val includeDateTime: Boolean,
    val includePidTid: Boolean,
    val includePackageName: Boolean,
    val includeTag: Boolean
) : LogParser {

    private val number = AtomicInteger(1)
    override fun parse(rawLines: List<String>): ParseResult {
        val logs = mutableListOf<Log>()
        val invalidSentences = mutableListOf<Pair<Int, String>>()
        var lastLog: Log? = null
        rawLines.forEachIndexed { index, s ->
            lastLog = try {
                val log = parseSingleLineLog(s)

                // Custom continuation
                if (log.log.startsWith("Cont(")) {
                    lastLog?.let {
                        it.copy(log = "${it.log}${log.log.substringAfter(") ")}")
                    } ?: log
                } else {
                    lastLog?.let { logs.add(it) }
                    log
                }
            } catch (e: Exception) {
                val continuedLog = if (lastLog == null) {
                    invalidSentences.add(index to s)
                    return@forEachIndexed
                } else {
                    lastLog!!
                }
                continuedLog.copy(log = "${continuedLog.log}\n$s")
            }
        }
        lastLog?.let { logs.add(it) }
        return ParseResult(logs, invalidSentences)
    }
    private fun parseSingleLineLog(raw: String): Log {
        var segmentCount = 5
        if (!includeDateTime) segmentCount -= 2
        if (!includePidTid && !includePackageName) segmentCount--

        val split = raw.split(" ", limit = segmentCount)

        var currentIndex = 0

        val date: String?
        val time: String?
        if (includeDateTime) {
            date = split[currentIndex++]
            time = split[currentIndex++]
        } else {
            date = null
            time = null
        }

        val pid: Long?
        val tid: Long?
        val packageName: String?
        when {
            includePidTid && includePackageName -> {
                val thirdSegment = split[currentIndex++].split("-", "/")
                pid = thirdSegment[0].toLong()
                tid = thirdSegment[1].toLong()
                packageName = thirdSegment[2].takeIf { it != "?" }
            }
            includePidTid -> {
                val thirdSegment = split[currentIndex++].split("-")
                pid = thirdSegment[0].toLong()
                tid = thirdSegment[1].toLong()
                packageName = null
            }
            includePackageName -> {
                pid = null
                tid = null
                packageName = split[currentIndex++]
            }
            else -> {
                pid = null
                tid = null
                packageName = null
            }
        }

        val priorityText: String
        val tag: String?
        if (includeTag) {
            val fourthSegment = split[currentIndex++].split("/")
            priorityText = fourthSegment[0]
            tag = fourthSegment[1].removeSuffix(":")
        } else {
            priorityText = split[currentIndex++].removeSuffix(":")
            tag = null
        }

        val originalLog = split[currentIndex]

        return Log(number.getAndIncrement(), date, time, pid, tid, packageName, priorityText, tag, originalLog)
    }

    override fun toString(): String {
        return "DefaultParser(includeDateTime=$includeDateTime, includePidTid=$includePidTid, includePackageName=$includePackageName, includeTag=$includeTag)"
    }
    companion object : ParserFactory {

        private val priorityChars = setOf('V', 'D', 'I', 'W', 'E', 'A')

        private fun String.isPriority(): Boolean {
            return length == 1 && first() in priorityChars
        }
        override fun create(sample: String): LogParser? {
            try {
                val split = sample.split(" ", limit = 5)
                val iterator = split.listIterator()

                var currentToken = iterator.next()

                val includeDate = try {
                    LocalDate.parse(currentToken)
                    currentToken = iterator.next()
                    true
                } catch (e: Exception) {
                    false
                }

                val includeTime = try {
                    LocalTime.parse(currentToken)
                    currentToken = iterator.next()
                    true
                } catch (e: Exception) {
                    false
                }

                // Only supports both exist or not exist at all
                if (includeDate xor includeTime) return null

                val pidTidRegex = Regex("\\d*[-/]\\d*")
                val packageNameRegex = Regex("^([A-Za-z][A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*$")

                // pit-tid/packageName
                var includePidTid: Boolean = false
                var includePackageName: Boolean = false
                if (currentToken.contains("/")) {
                    // both exist
                    val tokens = currentToken.split("/")
                    if (tokens[0].matches(pidTidRegex) && (tokens[1] == "?" || tokens[1].matches(packageNameRegex))) {
                        includePidTid = true
                        includePackageName = true
                        currentToken = iterator.next()
                    }
                } else {
                    if (currentToken.matches(pidTidRegex)) {
                        includePidTid = true
                        includePackageName = false
                        currentToken = iterator.next()
                    } else if (currentToken == "?" || currentToken.matches(packageNameRegex)) {
                        includePidTid = false
                        includePackageName = true
                        currentToken = iterator.next()
                    }
                }

                var includeTag = false
                if (currentToken.contains("/")) {
                    // both exist
                    val tokens = currentToken.split("/")
                    // Check what's faster: list and regex
                    if (tokens[0].isPriority()) {
                        includeTag = true
                    } else {
                        // invalid
                        return null
                    }
                } else if (currentToken.isPriority()) {
                    includeTag = false
                }

                if (currentToken.last() != ':') {
                    return null
                }

                if (!iterator.hasNext()) {
                    return null
                }

                return StudioLogcatBelowChipmunkParser(includeDate, includePidTid, includePackageName, includeTag)
            } catch (e: Exception) {
                return null
            }
        }
    }
}
