package com.jerryjeon.logjerry.parse

import com.jerryjeon.logjerry.log.Log
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.atomic.AtomicInteger

data class StudioLogcatAboveDolphinParser(
    // Log format configuration after AS Dolphin version
    val includeDate: Boolean,
    val includeTime: Boolean,
    val includePid: Boolean,
    val includeTid: Boolean,
    val includeTag: Boolean,
    val includePackageName: Boolean
) : LogParser {

    companion object : ParserFactory {

        private val packageNameRegex2 = Regex("^pid-\\d*$")
        private val packageNameRegex = Regex("^([A-Za-z][A-Za-z\\d_]*\\.)+[A-Za-z][A-Za-z\\d_]*$")
        private val priorityChars = setOf('V', 'D', 'I', 'W', 'E', 'A')

        private fun String.isPriority(): Boolean {
            return length == 1 && first() in priorityChars
        }

        override fun create(sample: String): LogParser? {
            try {
                val split = sample.split(" ").filter { it.isNotBlank() }
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

                val includePid: Boolean?
                val includeTid: Boolean?
                when {
                    currentToken.matches(Regex("\\d*")) -> { // only pid
                        includePid = true
                        includeTid = false
                        currentToken = iterator.next()
                    }
                    currentToken.matches(Regex("\\d*-\\d*")) -> { // pid-tid
                        includePid = true
                        includeTid = true
                        currentToken = iterator.next()
                    }
                    else -> {
                        includePid = false
                        includeTid = false
                    }
                }

                if (currentToken.isPriority()) {
                    return StudioLogcatAboveDolphinParser(includeDate, includeTime, includePid, includeTid, includeTag = false, includePackageName = false)
                }

                // Check package first, because for the tag there's no way to validate it

                // TODO Find more cleaner way
                if (currentToken.isPackageName()) {
                    currentToken = iterator.next()
                    return if (currentToken.isPriority() && iterator.hasNext()) {
                        StudioLogcatAboveDolphinParser(includeDate, includeTime, includePid, includeTid, includeTag = false, includePackageName = true)
                    } else {
                        null
                    }
                } else {
                    currentToken = iterator.next()
                    if (currentToken.isPriority()) {
                        return if (currentToken.isPriority() && iterator.hasNext()) {
                            StudioLogcatAboveDolphinParser(includeDate, includeTime, includePid, includeTid, includeTag = true, includePackageName = false)
                        } else {
                            null
                        }
                    } else if (currentToken.isPackageName()) {
                        currentToken = iterator.next()
                        return if (currentToken.isPriority() && iterator.hasNext()) {
                            StudioLogcatAboveDolphinParser(includeDate, includeTime, includePid, includeTid, includeTag = true, includePackageName = true)
                        } else {
                            null
                        }
                    }
                }

                return null
            } catch (e: Exception) {
                return null
            }
        }

        private fun String.isPackageName(): Boolean {
            return this == "system_process"
                || this.matches(packageNameRegex)
                || this.matches(packageNameRegex2)
        }
    }

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

    // The algorithm is inefficient. From my machine it's ok for 5000 lines. Improve later if there's an issue
    private fun parseSingleLineLog(raw: String): Log {
        val split = raw.split(" ").filter { it.isNotBlank() }

        var currentIndex = 0

        val date = if (includeDate) {
            split[currentIndex++]
        } else {
            null
        }

        val time = if(includeTime) {
            split[currentIndex++]
        } else {
            null
        }

        val pid: Long?
        val tid: Long?
        when {
            includePid && includeTid -> {
                val ids = split[currentIndex++].split("-")
                pid = ids[0].toLong()
                tid = ids[1].toLong()
            }
            includePid -> {
                pid = split[currentIndex++].toLong()
                tid = null
            }
            else -> {
                pid = null
                tid = null
            }
        }

        val tag = if (includeTag) {
            split[currentIndex++]
        } else {
            null
        }

        val packageName = if(includePackageName) {
            split[currentIndex++]
        } else {
            null
        }

        val priorityText = split[currentIndex++]

        val originalLog = split.drop(currentIndex).joinToString(separator = " ")

        return Log(number.getAndIncrement(), date, time, pid, tid, packageName, priorityText, tag, originalLog)
    }

    override fun toString(): String {
        return "StudioLogcatAboveDolphinParser(includeDate=$includeDate, includeTime=$includeTime, includePid=$includePid, includeTid=$includeTid, includeTag=$includeTag, includePackageName=$includePackageName, number=$number)"
    }
}

