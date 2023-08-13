package com.jerryjeon.logjerry.detector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.util.UUID

class JsonDetector : Detector<JsonDetection> {
    override val key: DetectorKey = DetectorKey.Json
    override val shownAsBlock: Boolean = true

    override fun detect(logStr: String, logIndex: Int): List<JsonDetection> {
        val bracketRanges = logStr.findBracketRanges('{', '}')

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
    val json: JsonObject,
    override val id: String = UUID.randomUUID().toString()
) : Detection {
    override val key: DetectorKey = DetectorKey.Json
    override val style: SpanStyle
        get() = detectedStyle

    companion object {
        val detectedStyle = SpanStyle(background = Color(0x40D3D3D3))
    }

    fun move(index: Int): JsonDetection {
        return JsonDetection(IntRange(range.first + index, range.last + index), logIndex, json, id)
    }
}
