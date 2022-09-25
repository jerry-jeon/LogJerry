package log

import Detection
import DetectionKey
import DetectionResult
import DetectionResultFocus
import IndexedDetectionResult
import Log
import androidx.compose.ui.text.AnnotatedString
import detection.ExceptionDetection
import detection.KeywordDetection
import detection.KeywordDetectionRequest
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import log.refine.LogFilter
import log.refine.RefinedLogs

class LogManager(
    val originalLogs: List<Log>
) {
    private val logScope = MainScope()

    private val defaultDetections = listOf<Detection>(ExceptionDetection())

    private val keywordDetectionEnabledStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val detectingKeywordFlow = MutableStateFlow("")
    val keywordDetectionRequestFlow = combine(keywordDetectionEnabledStateFlow, detectingKeywordFlow) { enabled, keyword ->
        if (enabled) {
            KeywordDetectionRequest.TurnedOn(keyword)
        } else {
            KeywordDetectionRequest.TurnedOff
        }
    }.stateIn(logScope, SharingStarted.Lazily, KeywordDetectionRequest.TurnedOff)

    val filtersFlow: MutableStateFlow<List<LogFilter>> = MutableStateFlow(emptyList())
    private val transformerFlow = combine(filtersFlow, keywordDetectionRequestFlow) { filters, findStatus ->
        Transformers(
            filters,
            when (findStatus) {
                is KeywordDetectionRequest.TurnedOn -> defaultDetections + listOf(KeywordDetection(findStatus.keyword))
                KeywordDetectionRequest.TurnedOff -> defaultDetections
            }
        )
    }
    val refinedLogs: StateFlow<RefinedLogs> = transformerFlow.map { transformers ->
        val detectionResults = mutableMapOf<DetectionKey, MutableList<IndexedDetectionResult>>()
        val refined = if (transformers.filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> transformers.filters.all { it.filter(log) } }
        }
            .mapIndexed { logIndex, log ->
                transformers.detections.fold(log) { acc, detection ->
                    val (detectionResult, changedLog) = doDetection(detection, acc)
                    if (detectionResult != null) {
                        val resultList = detectionResults.getOrPut(detection.key) { mutableListOf() }
                        resultList
                            .add(IndexedDetectionResult(detection.key, detectionResult, resultList.size, logIndex))
                    }
                    changedLog
                }
            }
        RefinedLogs(originalLogs, refined, detectionResults)
    }.stateIn(logScope, SharingStarted.Lazily, RefinedLogs(emptyList(), emptyList(), emptyMap()))

    val keywordDetectionResultFocus = MutableStateFlow<DetectionResultFocus?>(null)
    val exceptionDetectionResultFocus = MutableStateFlow<DetectionResultFocus?>(null)

    // When user press the next
    private val detectionResultFocusChangeFromUser = MutableStateFlow<DetectionResultFocus?>(null)

    init {
        logScope.launch {
            refinedLogs.collect {
                val results = it.detectionResults[DetectionKey.Keyword] ?: emptyList()
                keywordDetectionResultFocus.value = results.firstOrNull()?.let { DetectionResultFocus(it, results) }

                val results2 = it.detectionResults[DetectionKey.Exception] ?: emptyList()
                exceptionDetectionResultFocus.value = results2.firstOrNull()?.let { DetectionResultFocus(it, results2) }
            }
        }
    }

    val activeDetectionResultFocusFlowState =
        merge(keywordDetectionResultFocus, exceptionDetectionResultFocus, detectionResultFocusChangeFromUser)
            .stateIn(logScope, SharingStarted.Lazily, null)

    data class Transformers(
        val filters: List<LogFilter>,
        val detections: List<Detection>
    )

    private fun doDetection(detection: Detection, log: Log): Pair<DetectionResult?, Log> {
        val detectionResult = detection.detect(log) ?: return (null to log)
        return detectionResult to log.copy(
            log = detectionResult.ranges.fold(AnnotatedString.Builder(log.originalLog)) { builder, range ->
                builder.apply {
                    addStyle(detection.detectedStyle, range.first, range.last)
                }
            }.toAnnotatedString()
        )
    }

    fun addFilter(logFilter: LogFilter) {
        filtersFlow.value = filtersFlow.value + logFilter
    }

    fun removeFilter(logFilter: LogFilter) {
        filtersFlow.value = filtersFlow.value - logFilter
    }

    fun find(keyword: String) {
        detectingKeywordFlow.value = keyword
    }

    fun setKeywordDetectionEnabled(enabled: Boolean) {
        keywordDetectionEnabledStateFlow.value = enabled
    }

    fun previousFindResult(keyword: Boolean, detectionResultFocus: DetectionResultFocus) {
        val results = detectionResultFocus.detectionResults
        val previousIndex = if (detectionResultFocus.focusingResult.detectionIndex <= 0) {
            results.size - 1
        } else {
            detectionResultFocus.focusingResult.detectionIndex - 1
        }

        if (keyword) {
            keywordDetectionResultFocus.value = DetectionResultFocus(results[previousIndex], results)
        } else {
            exceptionDetectionResultFocus.value = DetectionResultFocus(results[previousIndex], results)
        }
    }

    fun nextFindResult(keyword: Boolean, detectionResultFocus: DetectionResultFocus) {
        val results = detectionResultFocus.detectionResults
        val nextIndex = if (detectionResultFocus.focusingResult.detectionIndex >= results.size - 1) {
            0
        } else {
            detectionResultFocus.focusingResult.detectionIndex + 1
        }

        if (keyword) {
            keywordDetectionResultFocus.value = DetectionResultFocus(results[nextIndex], results)
        } else {
            exceptionDetectionResultFocus.value = DetectionResultFocus(results[nextIndex], results)
        }
    }
}
