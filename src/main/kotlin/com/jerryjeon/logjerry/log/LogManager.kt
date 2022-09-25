package com.jerryjeon.logjerry.log

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.jerryjeon.logjerry.detection.Detection
import com.jerryjeon.logjerry.detection.DetectionFocus
import com.jerryjeon.logjerry.detection.DetectionKey
import com.jerryjeon.logjerry.detection.DetectionResult
import com.jerryjeon.logjerry.detection.ExceptionDetection
import com.jerryjeon.logjerry.detection.JsonDetection
import com.jerryjeon.logjerry.detection.JsonDetectionResult
import com.jerryjeon.logjerry.detection.KeywordDetection
import com.jerryjeon.logjerry.detection.KeywordDetectionRequest
import com.jerryjeon.logjerry.log.refine.DetectionFinishedLog
import com.jerryjeon.logjerry.log.refine.DetectionResultView
import com.jerryjeon.logjerry.log.refine.InvestigationResult
import com.jerryjeon.logjerry.log.refine.InvestigationResultView
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

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
        InvestigationResult(originalLogs, refined, allDetectionResults, transformers.detections)
    }.stateIn(logScope, SharingStarted.Lazily, InvestigationResult(emptyList(), emptyList(), emptyMap(), emptyList()))

    private val detectionExpanded = MutableStateFlow<Map<DetectionResult, Boolean>>(emptyMap())

    val investigationResultView: StateFlow<InvestigationResultView> = combine(investigationResult, detectionExpanded) { result, expanded ->
        // Why should it be separated : make possible to change data of detectionResult
        // TODO don't want to repeat all annotate if just one log has changed. How can I achieve it
        val refinedLogs = result.detectionFinishedLogs.map {
            val logContents =
                separateAnnotationStrings(
                    it.log,
                    it.detectionResults.values.flatten().map { detectionResult ->
                        DetectionResultView(detectionResult, expanded[detectionResult] ?: false)
                    }
                )
            RefinedLog(it, annotate(it.log, logContents, result.detections))
        }
        val allDetectionLogs = result.allDetectionResults.mapValues { (_, v) ->
            v.map { DetectionResultView(it, false) }
        }
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
            investigationResult.collect { result ->
                val keywordDetections = result.allDetectionResults[DetectionKey.Keyword] ?: emptyList()
                keywordDetectionFocus.value = keywordDetections.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Keyword, 0, null, keywordDetections)
                }

                val exceptionDetections = result.allDetectionResults[DetectionKey.Exception] ?: emptyList()
                exceptionDetectionFocus.value = exceptionDetections.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Exception, 0, null, exceptionDetections)
                }

                val jsonDetections = result.allDetectionResults[DetectionKey.Json] ?: emptyList()
                jsonDetectionFocus.value = jsonDetections.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Exception, 0, null, jsonDetections)
                }

                detectionExpanded.value = result.allDetectionResults.values.flatten().associateWith {
                    it is JsonDetectionResult
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

    val json = Json { prettyPrint = true }
    private fun separateAnnotationStrings(log: Log, detectionResults: List<DetectionResultView>): List<LogContent> {
        val sorted = detectionResults.sortedBy { it.detectionResult.range.first }
        val originalLog = log.log

        var lastEnded = 0
        val logContents = mutableListOf<LogContent>()
        sorted.forEach {
            val newStart = it.detectionResult.range.first
            val newEnd = it.detectionResult.range.last
            // Assume that there are no overlapping areas.
            if (it.detectionResult is JsonDetectionResult && it.expanded) {
                if (lastEnded != newStart) {
                    logContents.add(LogContent.Simple(originalLog.substring(lastEnded, newStart)))
                }
                logContents.add(LogContent.Json(json.encodeToString(JsonObject.serializer(), it.detectionResult.json), it.detectionResult))
                lastEnded = newEnd + 1
            }
        }
        if (lastEnded < originalLog.length) {
            logContents.add(LogContent.Simple(originalLog.substring(lastEnded)))
        }
        return logContents
    }

    private fun annotate(log: Log, logContents: List<LogContent>, detections: List<Detection<*>>): List<LogContentView> {
        val result = logContents.map {
            when (it) {
                is LogContent.Json -> {
                    val simple = it
                    val initial = AnnotatedString.Builder(simple.text)
                    val results = detections.flatMap { detection ->
                        detection.detect(simple.text, log.number)
                    }

                    val builder = results.fold(initial) { acc, next ->
                        acc.apply {
                            addStyle(
                                SpanStyle(),
                                next.range.first,
                                next.range.last + 1
                            )
                        }
                    }
                    LogContentView.Json(builder.toAnnotatedString(), Color.LightGray, it.jdr)
                }
                is LogContent.Simple -> {
                    val simple = it
                    val initial = AnnotatedString.Builder(simple.text)
                    val results = detections.flatMap { detection ->
                        detection.detect(simple.text, log.number)
                    }

                    val builder = results.fold(initial) { acc, next ->
                        acc.apply {
                            addStyle(
                                next.style,
                                next.range.first,
                                next.range.last + 1
                            )
                        }
                    }
                    LogContentView.Simple(builder.toAnnotatedString())
                }
            }
        }

        // Detector 가 필요하네..
        return result
    }

    fun collapse(detectionResult: DetectionResult) {
        detectionExpanded.value = detectionExpanded.value + (detectionResult to false)
    }
}
