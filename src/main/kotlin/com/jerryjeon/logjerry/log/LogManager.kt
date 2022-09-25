package com.jerryjeon.logjerry.log

import androidx.compose.ui.text.AnnotatedString
import com.jerryjeon.logjerry.detection.Detection
import com.jerryjeon.logjerry.detection.DetectionFocus
import com.jerryjeon.logjerry.detection.DetectionKey
import com.jerryjeon.logjerry.detection.DetectionResult
import com.jerryjeon.logjerry.detection.ExceptionDetection
import com.jerryjeon.logjerry.detection.JsonDetection
import com.jerryjeon.logjerry.detection.KeywordDetection
import com.jerryjeon.logjerry.detection.KeywordDetectionRequest
import com.jerryjeon.logjerry.log.refine.DetectionFinishedLog
import com.jerryjeon.logjerry.log.refine.InvestigationResult
import com.jerryjeon.logjerry.log.refine.InvestigationResultView
import com.jerryjeon.logjerry.log.refine.LogContent
import com.jerryjeon.logjerry.log.refine.LogFilter
import com.jerryjeon.logjerry.log.refine.PriorityFilter
import com.jerryjeon.logjerry.log.refine.RefinedLog
import com.jerryjeon.logjerry.log.refine.TextFilter
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

class LogManager(
    val originalLogs: List<Log>
) {
    private val logScope = MainScope()

    private val defaultDetections = listOf(ExceptionDetection(), JsonDetection())

    private val keywordDetectionEnabledStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val detectingKeywordFlow = MutableStateFlow("")
    val keywordDetectionRequestFlow =
        combine(keywordDetectionEnabledStateFlow, detectingKeywordFlow) { enabled, keyword ->
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
    private val investigationResult: StateFlow<InvestigationResult> = transformerFlow.map { transformers ->
        val refined = if (transformers.filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> transformers.filters.all { it.filter(log) } }
        }
            .mapIndexed { logIndex, log ->
                val detectionResults = transformers.detections.map { it.detect(log.log, logIndex) }
                    .flatten()
                DetectionFinishedLog(log, detectionResults.groupBy { it.key })
            }

        val allDetectionResults = mutableMapOf<DetectionKey, List<DetectionResult>>()
        refined.forEach {
            it.detectionResults.forEach { (key, value) ->
                allDetectionResults[key] = (allDetectionResults[key] ?: emptyList()) + value
            }
        }
        InvestigationResult(originalLogs, refined, allDetectionResults)
    }.stateIn(logScope, SharingStarted.Lazily, InvestigationResult(emptyList(), emptyList(), emptyMap()))

    val investigationResultView: StateFlow<InvestigationResultView> = investigationResult.map { investigationResult ->
        // Why should it be separated : make possible to change data of detectionResult
        // TODO don't want to repeat all annotate if just one log has changed. How can I achieve it
        val refinedLogs = investigationResult.detectionFinishedLogs.map {
            RefinedLog(it, annotate(it.log, it.detectionResults.values.flatten()))
        }
        val allDetectionLogs = investigationResult.allDetectionResults
        InvestigationResultView(refinedLogs, allDetectionLogs)
    }.stateIn(logScope, SharingStarted.Lazily, InvestigationResultView(emptyList(), emptyMap()))

    val keywordDetectionFocus = MutableStateFlow<DetectionFocus?>(null)
    val exceptionDetectionFocus = MutableStateFlow<DetectionFocus?>(null)
    val jsonDetectionFocus = MutableStateFlow<DetectionFocus?>(null)

    private val focuses = mapOf(
        DetectionKey.Keyword to keywordDetectionFocus,
        DetectionKey.Exception to exceptionDetectionFocus,
        DetectionKey.Json to jsonDetectionFocus,
    )

    init {
        logScope.launch {
            investigationResult.collect { refinedLogs ->
                val results = refinedLogs.allDetectionResults[DetectionKey.Keyword] ?: emptyList()
                keywordDetectionFocus.value = results.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Keyword, 0, null, results)
                }

                val results2 = refinedLogs.allDetectionResults[DetectionKey.Exception] ?: emptyList()
                exceptionDetectionFocus.value = results2.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Exception, 0, null, results2)
                }

                val results3 = refinedLogs.allDetectionResults[DetectionKey.Json] ?: emptyList()
                jsonDetectionFocus.value = results3.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Exception, 0, null, results3)
                }
            }
        }
    }

    val activeDetectionResultFocusFlowState =
        merge(keywordDetectionFocus, exceptionDetectionFocus, jsonDetectionFocus)
            .filter { it?.focusing != null }
            .stateIn(logScope, SharingStarted.Lazily, null)

    data class Transformers(
        val filters: List<LogFilter>,
        val detections: List<Detection<*>>
    )

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

    fun previousFindResult(key: DetectionKey, focus: DetectionFocus) {
        val previousIndex = if (focus.currentIndex <= 0) {
            focus.results.size - 1
        } else {
            focus.currentIndex - 1
        }

        focuses[key]?.value = focus.copy(currentIndex = previousIndex, focusing = focus.results[previousIndex])
    }

    fun nextFindResult(key: DetectionKey, focus: DetectionFocus) {
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

    private fun annotate(log: Log, detectionResults: List<DetectionResult>): List<LogContent> {
        // Assume that there are no overlapping areas.
        // TODO: Support overlapping detection
        val initial = DetectionResult.AnnotationResult(AnnotatedString.Builder(log.log), 0)
        val annotationResult = detectionResults.fold(initial) { acc, next -> next.annotate(acc) }
        return listOf(LogContent.Simple(annotationResult.builder.toAnnotatedString()))
    }
}
