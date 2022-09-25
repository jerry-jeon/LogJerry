package com.jerryjeon.logjerry.parse

import com.jerryjeon.logjerry.log.LogManager
import com.jerryjeon.logjerry.transformation.TransformationManager

sealed class ParseStatus {
    object NotStarted : ParseStatus()
    data class Proceeding(
        val percent: Int
    ) : ParseStatus()
    class Completed(
        val parseResult: ParseResult,
        val transformationManager: TransformationManager,
        val logManager: LogManager
    ) : ParseStatus()
}
