package com.jerryjeon.logjerry.detector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import java.util.UUID

class DataClassDetector : Detector<DataClassDetection> {
    override val key: DetectorKey = DetectorKey.DataClass
    override val shownAsBlock: Boolean = true

    override fun detect(logStr: String, logIndex: Int): List<DataClassDetection> {
        val bracketRanges = extractDataClassRanges(logStr)

        val dataClasses = bracketRanges.mapNotNull { range ->
            val text = logStr.substring(range)
            try {
                range to (parseToStringRepresentation(text) ?: throw IllegalArgumentException("Invalid data class"))
            } catch (_: Exception) {
                null
            }
        }

        return dataClasses.map { (range, json) ->
            DataClassDetection(range, logIndex, json)
        }
    }
}

fun extractDataClassRanges(input: String): List<IntRange> {
    val results = mutableListOf<IntRange>()

    var startIndex = -1
    var openParensCount = 0
    var hasEquals = false  // To check if we found an argument with "="

    input.forEachIndexed { index, char ->
        when (char) {
            '(' -> {
                if (startIndex == -1) {
                    var potentialStart = index - 1
                    while (potentialStart >= 0 && !input[potentialStart].isLetter()) {
                        potentialStart--
                    }
                    while (potentialStart >= 0 && input[potentialStart].isLetter()) {
                        potentialStart--
                    }
                    if (potentialStart >= 0 && input[potentialStart + 1].isUpperCase()) {
                        startIndex = potentialStart + 1  // Adjust to start of the class name
                    }
                }
                openParensCount++
            }
            '=' -> if (openParensCount > 0) hasEquals = true
            ')' -> {
                openParensCount--
                if (openParensCount == 0 && startIndex != -1 && hasEquals) {
                    results.add(IntRange(startIndex, index))
                    startIndex = -1
                    hasEquals = false
                }
            }
        }
    }

    return results
}

fun parseToStringRepresentation(input: String): Map<String, Any?>? {
    val resultMap = mutableMapOf<String, Any?>()

    fun parseProperties(propertiesString: String): Map<String, Any?> {
        val propertyMap = mutableMapOf<String, Any?>()

        val propertyRegex = """(\w+)=((?:\w+)|(?:\w+\([\w\W]+?\)))""".toRegex()
        val properties = propertyRegex.findAll(propertiesString)

        for (propertyMatch in properties) {
            val propertyName = propertyMatch.groupValues[1]
            val propertyValue = propertyMatch.groupValues[2]

            if (propertyValue.matches("""\d+""".toRegex())) {
                propertyMap[propertyName] = propertyValue.toInt()
            } else if (propertyValue.contains("(")) {
                // Recursive call for nested data class
                val parsedValue = parseToStringRepresentation(propertyValue)
                if (parsedValue != null) {
                    propertyMap[propertyName] = parsedValue
                }
            } else {
                propertyMap[propertyName] = propertyValue
            }
        }

        return propertyMap
    }

    val regex = """(\w+)\(([\w\W]*?)\)""".toRegex()
    val matchResult = regex.find(input) ?: return null

    val className = matchResult.groupValues[1]
    val propertiesString = matchResult.groupValues[2]

    resultMap.putAll(parseProperties(propertiesString))
    // If the data class representation has no arguments, return null
    if (resultMap.isEmpty()) {
        return null
    }

    resultMap["class"] = className

    return resultMap
}

class DataClassDetection(
    override val range: IntRange,
    override val logIndex: Int,
    val map: Map<String, Any?>,
    override val id: String = UUID.randomUUID().toString()
) : Detection {
    override val key: DetectorKey = DetectorKey.DataClass
    override val style: SpanStyle
        get() = detectedStyle

    companion object {
        val detectedStyle = SpanStyle(background = Color(0x40D3D3D3))
    }
}
