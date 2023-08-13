package com.jerryjeon.logjerry.detector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import com.jerryjeon.logjerry.mark.LogMark
import java.util.UUID

class MarkDetector(
    val logMarks: Map<Int, LogMark>,
) : Detector<MarkDetection> {
    override val key = DetectorKey.Mark
    override val shownAsBlock: Boolean = false

    fun setMark(logMark: LogMark): MarkDetector {
        return MarkDetector(logMarks + (logMark.log.index to logMark))
    }

    fun deleteMark(logIndex: Int): MarkDetector {
        return MarkDetector(logMarks = logMarks - logIndex)
    }

    override fun detect(logStr: String, logIndex: Int): List<MarkDetection> {
        val logMark = logMarks[logIndex]
        return if (logMark != null) {
            listOf(
                MarkDetection(
                    UUID.randomUUID().toString(),
                    logStr.indices,
                    logIndex,
                    logMark.note,
                    logMark.color
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
    val note: String,
    val color: Color,
) : Detection {
    override val key = DetectorKey.Mark

    override val style: SpanStyle = detectedStyle

    companion object {
        // TODO find cleaner way.. It doesn't need to exist
        val detectedStyle = SpanStyle()
    }
}
