package com.jerryjeon.logjerry.detector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.util.UUID

class JsonDetector() : Detector<JsonDetection> {
    override val key: DetectionKey = DetectionKey.Json

    override fun detect(logStr: String, logIndex: Int): List<JsonDetection> {
        // Find bracket pairs, { } and check this is json or not
        val stack = ArrayDeque<Pair<Int, Char>>()
        val bracketRanges = mutableListOf<IntRange>()
        logStr.forEachIndexed { index, c ->
            if (c == '{') stack.addLast(index to '{')
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

        if (bracketRanges.isEmpty()) return emptyList()

        val jsonList = bracketRanges.mapNotNull { range ->
            val text = logStr.substring(range)
            try {
                range to Json.parseToJsonElement(text).jsonObject
            } catch (_: Exception) {
                null
            }
        }
            .filter { (_, json) -> json.isNotEmpty() } // Filter empty json

        return jsonList.map { (range, json) ->
            JsonDetection(range, logIndex, json)
        }
    }
}

class JsonDetection(
    override val range: IntRange,
    override val logIndex: Int,
    val json: JsonObject
) : Detection {
    override val id: String = UUID.randomUUID().toString()
    override val key: DetectionKey = DetectionKey.Json
    override val style: SpanStyle
        get() = detectedStyle

    companion object {
        val detectedStyle = SpanStyle(background = Color(0x40777777))
    }
}
