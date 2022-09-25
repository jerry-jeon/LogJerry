import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import detection.ExceptionDetection
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LogManager(
    val originalLogs: List<Log>
) {
    private val logScope = MainScope()

    val defaultDetections = listOf<Detection>(ExceptionDetection())

    private val keywordFindEnabledStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val findKeywordFlow = MutableStateFlow("")
    val keywordFindRequestFlow = combine(keywordFindEnabledStateFlow, findKeywordFlow) { enabled, keyword ->
        if (enabled) {
            KeywordFindRequest.TurnedOn(keyword)
        } else {
            KeywordFindRequest.TurnedOff
        }
    }.stateIn(logScope, SharingStarted.Lazily, KeywordFindRequest.TurnedOff)

    val filtersFlow: MutableStateFlow<List<Filter>> = MutableStateFlow(emptyList())
    private val transformerFlow = combine(filtersFlow, keywordFindRequestFlow) { filters, findStatus ->
        Transformers(
            filters.map { filter ->
                { log ->
                    when (filter.columnType) {
                        ColumnType.PackageName -> filter.text in (log.packageName ?: "")
                        ColumnType.Tag -> filter.text in log.tag
                        ColumnType.Log -> filter.text in log.log
                        else -> throw NotImplementedError("Not implemented filter : ${filter.columnType}")
                    }
                }
            },
            when (findStatus) {
                is KeywordFindRequest.TurnedOn -> defaultDetections + listOf(KeywordDetection(findStatus.keyword))
                KeywordFindRequest.TurnedOff -> defaultDetections
            }
        )
    }
    val refinedLogs: StateFlow<RefinedLogs> = transformerFlow.map { transformers ->
        val detectionResults = mutableMapOf<String, MutableList<DetectionResult>>()
        val refined = if (transformers.filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> transformers.filters.all { it(log) } }
        }
            .mapIndexed { logIndex, log ->
                transformers.detections.fold(log) { acc, detection ->
                    val (changed, changedLog) = doDetection(detection, acc)
                    if (changed) {
                        val resultList = detectionResults.getOrPut(detection.key) { mutableListOf() }
                        resultList
                            .add(DetectionResult(detection.key, resultList.size, logIndex))
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
                val results = it.detectionResults["keyword"] ?: emptyList()
                keywordDetectionResultFocus.value = results.firstOrNull()?.let { DetectionResultFocus(it, results) }

                val results2 = it.detectionResults["exception"] ?: emptyList()
                exceptionDetectionResultFocus.value = results2.firstOrNull()?.let { DetectionResultFocus(it, results2) }
            }
        }
    }

    val activeDetectionResultFocusFlowState =
        merge(keywordDetectionResultFocus, exceptionDetectionResultFocus, detectionResultFocusChangeFromUser)
            .stateIn(logScope, SharingStarted.Lazily, null)

    data class Transformers(
        val filters: List<(Log) -> Boolean>,
        val detections: List<Detection>
    )

    private fun doDetection(detection: Detection, log: Log): Pair<Boolean, Log> {
        val indexRanges = detection.detect(log)

        return if (indexRanges.isEmpty()) {
            false to log
        } else {
            true to log.copy(
                log = indexRanges.fold(AnnotatedString.Builder(log.originalLog)) { builder, range ->
                    builder.apply {
                        addStyle(detection.detectedStyle, range.first, range.last)
                    }
                }.toAnnotatedString()
            )
        }
    }

    class KeywordDetection(private val keyword: String) : Detection {
        override val key: String = "keyword"
        override val detectedStyle: SpanStyle = SpanStyle(background = Color.Yellow)
        override fun detect(log: Log): List<IntRange> {
            if (keyword.isBlank()) return emptyList()
            var startIndex = 0
            val indexRanges = mutableListOf<IntRange>()
            while (startIndex != -1) {
                startIndex = log.originalLog.indexOf(keyword, startIndex)
                if (startIndex != -1) {
                    indexRanges.add(startIndex..startIndex + keyword.length)
                    startIndex += keyword.length
                }
            }

            return indexRanges
        }
    }

    fun addFilter(filter: Filter) {
        filtersFlow.value = filtersFlow.value + filter
    }

    fun removeFilter(filter: Filter) {
        filtersFlow.value = filtersFlow.value - filter
    }

    fun find(keyword: String) {
        findKeywordFlow.value = keyword
    }

    fun setKeywordFindEnabled(enabled: Boolean) {
        keywordFindEnabledStateFlow.value = enabled
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
