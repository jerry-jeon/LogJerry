package com.jerryjeon.logjerry.detector

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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
        }.stateIn(detectionScope, SharingStarted.Lazily, KeywordDetectionRequest.TurnedOff)
    val detectorsFlow = keywordDetectionRequestFlow.map {
        when (it) {
            is KeywordDetectionRequest.TurnedOn -> defaultDetectors + listOf(KeywordDetector(it.keyword))
            KeywordDetectionRequest.TurnedOff -> defaultDetectors
        }
    }

    fun findKeyword(keyword: String) {
        detectingKeywordFlow.value = keyword
    }

    fun setKeywordDetectionEnabled(enabled: Boolean) {
        keywordDetectorEnabledStateFlow.value = enabled
    }
}
