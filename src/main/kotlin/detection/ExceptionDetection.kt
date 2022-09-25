package detection

import Detection
import Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

class ExceptionDetection : Detection {
    override val key: String = "exception"
    override val detectedStyle: SpanStyle
        get() = SpanStyle(background = Color(0x40EEEEA5))

    override fun detect(log: Log): List<IntRange> {
        val lines = log.originalLog.split("\n")
        val isException = lines.any {
            isStackTrace(it)
        }
        return if (isException) {
            listOf(0 until log.originalLog.length)
        } else {
            emptyList()
        }
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
