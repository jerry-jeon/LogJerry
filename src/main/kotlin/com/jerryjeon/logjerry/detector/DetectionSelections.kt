package com.jerryjeon.logjerry.detector

data class DetectionSelections(
    val selectionByKey: Map<DetectorKey, DetectionSelection?> = mapOf(
        DetectorKey.Keyword to null,
        DetectorKey.Exception to null,
        DetectorKey.Json to null,
        DetectorKey.Mark to null,
    ),
    val active: DetectionSelection? = null
) {

    fun updatedSelections(key: DetectorKey, selection: DetectionSelection): DetectionSelections {
        return this.copy(
            selectionByKey = this.selectionByKey + (key to selection),
            active = selection
        )
    }
}
