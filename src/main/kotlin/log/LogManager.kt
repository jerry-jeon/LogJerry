package log

import Detection
import DetectionKey
import DetectionResult
import DetectionResultFocus
import Log
import Priority
import androidx.compose.ui.text.AnnotatedString
import detection.ExceptionDetection
import detection.JsonDetection
import detection.KeywordDetection
import detection.KeywordDetectionRequest
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import log.refine.LogFilter
import log.refine.PriorityFilter
import log.refine.RefinedLogs
import log.refine.TextFilter

class LogManager(
    val originalLogs: List<Log>
) {
    private val logScope = MainScope()

    private val defaultDetections = listOf<Detection>(ExceptionDetection(), JsonDetection())

    private val keywordDetectionEnabledStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val detectingKeywordFlow = MutableStateFlow("")
    val keywordDetectionRequestFlow = combine(keywordDetectionEnabledStateFlow, detectingKeywordFlow) { enabled, keyword ->
        if (enabled) {
            KeywordDetectionRequest.TurnedOn(keyword)
        } else {
            KeywordDetectionRequest.TurnedOff
        }
    }.stateIn(logScope, SharingStarted.Lazily, KeywordDetectionRequest.TurnedOff)

    val textFiltersFlow: MutableStateFlow<List<TextFilter>> = MutableStateFlow(emptyList())
    val priorityFilter: MutableStateFlow<PriorityFilter> = MutableStateFlow(PriorityFilter(Priority.Verbose))
    private val filtersFlow = combine(textFiltersFlow, priorityFilter) { textFilters, priorityFilter ->
        textFilters + listOf(priorityFilter)
    }
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
        val detectionResults = mutableMapOf<DetectionKey, MutableList<DetectionResult>>()
        val refined = if (transformers.filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> transformers.filters.all { it.filter(log) } }
        }
            .mapIndexed { logIndex, log ->
                transformers.detections.fold(log) { acc, detection ->
                    val (detectionResult, changedLog) = doDetection(detection, acc, logIndex)
                    if (detectionResult != null) {
                        val resultList = detectionResults.getOrPut(detection.key) { mutableListOf() }
                        resultList.add(detectionResult)
                    }
                    changedLog
                }
            }
        RefinedLogs(originalLogs, refined, detectionResults)
    }.stateIn(logScope, SharingStarted.Lazily, RefinedLogs(emptyList(), emptyList(), emptyMap()))

    val keywordDetectionResultFocus = MutableStateFlow<DetectionResultFocus?>(null)
    val exceptionDetectionResultFocus = MutableStateFlow<DetectionResultFocus?>(null)
    val jsonDetectionResultFocus = MutableStateFlow<DetectionResultFocus?>(null)

    private val focuses = mapOf(
        DetectionKey.Keyword to keywordDetectionResultFocus,
        DetectionKey.Exception to exceptionDetectionResultFocus,
        DetectionKey.Json to jsonDetectionResultFocus,
    )

    init {
        logScope.launch {
            refinedLogs.collect { refinedLogs ->
                val results = refinedLogs.detectionResults[DetectionKey.Keyword] ?: emptyList()
                keywordDetectionResultFocus.value = results.firstOrNull()?.let {
                    DetectionResultFocus(DetectionKey.Keyword, 0, null, results)
                }

                val results2 = refinedLogs.detectionResults[DetectionKey.Exception] ?: emptyList()
                exceptionDetectionResultFocus.value = results2.firstOrNull()?.let {
                    DetectionResultFocus(DetectionKey.Exception, 0, null, results2)
                }

                val results3 = refinedLogs.detectionResults[DetectionKey.Json] ?: emptyList()
                jsonDetectionResultFocus.value = results3.firstOrNull()?.let {
                    DetectionResultFocus(DetectionKey.Exception, 0, null, results3)
                }
            }
        }
    }

    val activeDetectionResultFocusFlowState =
        merge(keywordDetectionResultFocus, exceptionDetectionResultFocus, jsonDetectionResultFocus)
            .filter { it?.focusing != null }
            .stateIn(logScope, SharingStarted.Lazily, null)

    data class Transformers(
        val filters: List<LogFilter>,
        val detections: List<Detection>
    )

    private fun doDetection(detection: Detection, log: Log, logIndex: Int): Pair<DetectionResult?, Log> {
        val detectionResult = detection.detect(log, logIndex) ?: return (null to log)
        return detectionResult to log.copy(
            log = detectionResult.ranges.fold(AnnotatedString.Builder(log.originalLog)) { builder, range ->
                builder.apply {
                    addStyle(detection.detectedStyle, range.first, range.last)
                }
            }.toAnnotatedString()
        )
    }

    fun addFilter(textFilter: TextFilter) {
        textFiltersFlow.value = textFiltersFlow.value + textFilter
    }

    fun removeFilter(textFilter: TextFilter) {
        textFiltersFlow.value = textFiltersFlow.value - textFilter
    }

    fun find(keyword: String) {
        detectingKeywordFlow.value = keyword
    }

    fun setKeywordDetectionEnabled(enabled: Boolean) {
        keywordDetectionEnabledStateFlow.value = enabled
    }

    fun previousFindResult(key: DetectionKey, focus: DetectionResultFocus) {
        val previousIndex = if (focus.currentIndex <= 0) {
            focus.results.size - 1
        } else {
            focus.currentIndex - 1
        }

        focuses[key]?.value = focus.copy(currentIndex = previousIndex, focusing = focus.results[previousIndex])
    }

    fun nextFindResult(key: DetectionKey, focus: DetectionResultFocus) {
        val nextIndex = if (focus.currentIndex >= focus.results.size - 1) {
            0
        } else {
            focus.currentIndex + 1
        }

        focuses[key]?.value = focus.copy(currentIndex = nextIndex, focusing = focus.results[nextIndex])
    }

    fun setPriority(priorityFilter: PriorityFilter) {
        this.priorityFilter.value = priorityFilter
    }
}
