package com.jerryjeon.logjerry.detector

import com.jerryjeon.logjerry.mark.LogMark
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class DetectorManager(preferences: Preferences) {
    private val detectionScope = CoroutineScope(Dispatchers.Default)

    private val defaultDetectors =
        listOf(JsonDetector()) + (if (preferences.showExceptionDetection) listOf(ExceptionDetector()) else emptyList())
    private val keywordDetectorEnabledStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val detectingKeywordFlow = MutableStateFlow("")
    val keywordDetectionRequestFlow =
        combine(keywordDetectorEnabledStateFlow, detectingKeywordFlow) { enabled, keyword ->
            if (enabled) {
                KeywordDetectionRequest.TurnedOn(keyword)
            } else {
                KeywordDetectionRequest.TurnedOff
            }
        }
            .stateIn(detectionScope, SharingStarted.Lazily, KeywordDetectionRequest.TurnedOff)

    private val toggleMarkLogRequestFlow = MutableStateFlow<MarkRequest?>(null)
    private val markDetectorFlow = toggleMarkLogRequestFlow.scan(MarkDetector(emptyMap())) { detector, markRequest ->
        when (markRequest) {
            is MarkRequest.Mark -> detector.setMark(markRequest.logMark)
            is MarkRequest.Delete -> detector.deleteMark(markRequest.logIndex)
            null -> detector
        }
    }
    val markedRowsFlow = markDetectorFlow.map { it.logMarks }
        .map { it.values.map { it.log }.sortedBy { log -> log.index } } // TODO find cleaner way
        .stateIn(detectionScope, SharingStarted.Lazily, emptyList())

    val detectorsFlow = combine(
        keywordDetectionRequestFlow.debounce(100L), markDetectorFlow
    ) { keywordDetectionRequest, markDetector ->
        when (keywordDetectionRequest) {
            is KeywordDetectionRequest.TurnedOn -> defaultDetectors + listOf(KeywordDetector(keywordDetectionRequest.keyword)) + markDetector
            KeywordDetectionRequest.TurnedOff -> defaultDetectors + markDetector
        }
    }

    fun findKeyword(keyword: String) {
        detectingKeywordFlow.value = keyword
    }

    fun setKeywordDetectionEnabled(enabled: Boolean) {
        keywordDetectorEnabledStateFlow.value = enabled
    }

    fun setMark(logMark: LogMark) {
        toggleMarkLogRequestFlow.value = MarkRequest.Mark(logMark)
    }
    fun deleteMark(logIndex: Int) {
        toggleMarkLogRequestFlow.value = MarkRequest.Delete(logIndex)
    }

    sealed class MarkRequest {
        data class Mark(val logMark: LogMark) : MarkRequest()
        data class Delete(val logIndex: Int) : MarkRequest()
    }
}
