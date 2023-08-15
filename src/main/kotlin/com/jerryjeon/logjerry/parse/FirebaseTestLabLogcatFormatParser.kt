package com.jerryjeon.logjerry.parse

import com.jerryjeon.logjerry.log.Log
import java.util.concurrent.atomic.AtomicInteger

class FirebaseTestLabLogcatFormatParser : LogParser {

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

    fun parseSingleLineLog(raw: String): Log {
        val timeAndMessage = raw.split(": ", limit = 2)

        val date = timeAndMessage[0].split(" ")[0]
        val time = timeAndMessage[0].split(" ")[1]

        val metadataParts = timeAndMessage[1].substringBefore(":")
        val priorityText = metadataParts.substringBefore("/")

        val tagWithPid = metadataParts.substringAfter("/")
        val tag = tagWithPid.substringBefore("(")
        val pid = tagWithPid.substringAfter("(").substringBefore(")").toLong()

        val originalLog = timeAndMessage[1].substringAfter(": ")

        return Log(number.getAndIncrement(), date, time, pid, null, null, priorityText, tag, originalLog)
    }

    companion object : ParserFactory {
        private val logRegex = """\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}: [EWIDV]/[a-zA-Z0-9_\-]+\( *\d+ *\): .+""".toRegex()

        override fun create(sample: String): LogParser? {
            val matches = logRegex.matches(sample)
            return if (matches) {
                FirebaseTestLabLogcatFormatParser()
            } else {
                null
            }
        }
    }
}
