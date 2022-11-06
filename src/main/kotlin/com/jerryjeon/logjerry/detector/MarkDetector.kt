package com.jerryjeon.logjerry.detector

import androidx.compose.ui.text.SpanStyle
import com.jerryjeon.logjerry.log.Log
import java.util.UUID

class MarkDetector(
    private val logIndices: Set<Int>
) : Detector<MarkDetection> {
    override val key = DetectorKey.Mark

    fun toggleMark(log: Log?): MarkDetector {
        return when {
            log == null -> return this
            log.index in logIndices -> MarkDetector(logIndices - log.index)
            else -> MarkDetector(logIndices + log.index)
        }
    }

    override fun detect(logStr: String, logIndex: Int): List<MarkDetection> {
        return if (logIndex in logIndices) {
            listOf(
                MarkDetection(
                    UUID.randomUUID().toString(),
                    logStr.indices,
                    logIndex,
                )
            )
        } else {
            emptyList()
        }
    }
}

class MarkDetection(
    override val id: String,
    override val range: IntRange, // TODO find cleaner way.. It doesn't need to exist
    override val logIndex: Int,
) : Detection {
    override val key = DetectorKey.Mark

    override val style: SpanStyle = detectedStyle

    companion object {
        // TODO find cleaner way.. It doesn't need to exist
        val detectedStyle = SpanStyle()
    }
}
