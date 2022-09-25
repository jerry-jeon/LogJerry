package detection

import Detection
import DetectionKey
import DetectionResult
import Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

class JsonDetection : Detection {
    override val key = DetectionKey.Json
    override val detectedStyle: SpanStyle
        get() = SpanStyle(background = Color(0x40777777))

    override fun detect(log: Log, logIndex: Int): JsonDetectionResult? {
        // Find bracket pairs, { } and check this is json or not
        val stack = ArrayDeque<Pair<Int, Char>>()
        val bracketRanges = mutableListOf<IntRange>()
        log.originalLog.forEachIndexed { index, c ->
            if (c == '{') stack.addFirst(index to '{')
            else if (c == '}') {
                val lastOrNull = stack.lastOrNull() ?: return@forEachIndexed
                if (lastOrNull.second == '{') {
                    val (openIndex, _) = stack.removeLast()
                    if (stack.isEmpty()) { // It's most outside part of bracket
                        bracketRanges.add(openIndex..index)
                    }
                }
            }
        }

        if (bracketRanges.isEmpty()) return null

        val jsonList = bracketRanges.mapNotNull {
            val text = log.originalLog.substring(it)
            try {
                Json.parseToJsonElement(text).jsonObject
            } catch (_: Exception) {
                null
            }
        }
            .filter { it.isNotEmpty() } // Filter empty json

        if (jsonList.isEmpty()) return null

        return JsonDetectionResult(bracketRanges, log, logIndex, jsonList)
    }
}

class JsonDetectionResult(
    ranges: List<IntRange>,
    log: Log,
    logIndex: Int,
    val jsonList: List<JsonObject>
) : DetectionResult(ranges, log, logIndex)
