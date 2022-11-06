package com.jerryjeon.logjerry.detector

import com.jerryjeon.logjerry.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@OptIn(FlowPreview::class)
class DetectorManager {
    private val detectionScope = CoroutineScope(Dispatchers.Default)

    private val defaultDetectors = listOf(ExceptionDetector(), JsonDetector())
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
            .debounce(250)
            .stateIn(detectionScope, SharingStarted.Lazily, KeywordDetectionRequest.TurnedOff)

    private val toggleMarkLogRequestFlow = MutableStateFlow<Log?>(null)
    private val markDetectorFlow = toggleMarkLogRequestFlow.scan(MarkDetector(emptySet())) { detector, toggleRequestedLog ->
        detector.toggleMark(toggleRequestedLog)
    }
    val markedRowsFlow = markDetectorFlow.map { it.logs }
        .map { it.sortedBy { log -> log.index } } // TODO find cleaner way
        .stateIn(detectionScope, SharingStarted.Lazily, emptyList())

    val detectorsFlow = combine(keywordDetectionRequestFlow, markDetectorFlow) { keywordDetectionRequest, markDetector ->
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

    fun toggleMark(log: Log) {
        toggleMarkLogRequestFlow.value = log
    }
}
