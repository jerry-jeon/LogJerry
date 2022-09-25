package com.jerryjeon.logjerry.detection

import Detection
import DetectionKey
import DetectionResult
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

class ExceptionDetection : Detection<ExceptionDetectionResult> {
    override val key = DetectionKey.Exception
    override val detectedStyle: SpanStyle
        get() = SpanStyle(background = Color(0x40EEEEA5))

    override fun detect(logStr: String, logIndex: Int): List<ExceptionDetectionResult> {
        val lines = logStr.split("\n")
        val stackStartLine = lines.indexOfFirst { isStackTrace(it) }
            .takeIf { it != -1 } ?: return emptyList()

        val exceptionLines = lines.subList(0, (stackStartLine - 1).coerceAtLeast(0))
        val words = exceptionLines.joinToString(separator = " ").split(',', '.', ' ', '\n', '$', ':', ';')
        val exception = words.firstOrNull { it.contains("exception", ignoreCase = true) || it.contains("error", ignoreCase = true) }

        val result = ExceptionDetectionResult(key, detectedStyle, logStr.indices, logIndex, exception ?: "")
        return listOf(result)
    }

    /**
     * Check pattern is
     * `at methodCall (FileName:Line)`
     */
    private fun isStackTrace(str: String): Boolean {
        val trimmed = str.trim()
        if (!trimmed.startsWith("at ")) return false
        if (trimmed.lastOrNull() != ')') return false

        val locationStart = trimmed.lastIndexOf("(")
            .takeIf { it != -1 } ?: return false

        val location = trimmed.substring(locationStart + 1, trimmed.length - 1) // skip the parentheses

        val split = location.split(":")
        if (split.size != 2) return false
        val fileName = split[0]
        if (!fileName.endsWith("java") && !fileName.endsWith("kt")) return false
        val lineNumber = split[1]
        if (lineNumber.toIntOrNull() == null) return false

        return true
    }
}

class ExceptionDetectionResult(
    key: DetectionKey,
    style: SpanStyle,
    range: IntRange,
    logIndex: Int,
    val exception: String,
) : DetectionResult(key, style, range, logIndex)
