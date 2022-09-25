import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn

class LogManager(
    val originalLogs: List<Log>
) {
    private val logScope = MainScope()

    private val findEnabledStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val findKeywordFlow = MutableStateFlow("")
    val findRequestFlow = combine(findEnabledStateFlow, findKeywordFlow) { enabled, keyword ->
        if (enabled) {
            FindRequest.TurnedOn(keyword)
        } else {
            FindRequest.TurnedOff
        }
    }.stateIn(logScope, SharingStarted.Lazily, FindRequest.TurnedOff)

    val filtersFlow: MutableStateFlow<List<Filter>> = MutableStateFlow(emptyList())
    private val transformerFlow = combine(filtersFlow, findRequestFlow) { filters, findStatus ->
        Transformers(
            filters.map { filter ->
                { log ->
                    // TODO use filter.columnType
                    filter.text in log.log
                }
            },
            when (findStatus) {
                is FindRequest.TurnedOn -> listOf(KeywordDetection(findStatus.keyword))
                FindRequest.TurnedOff -> emptyList()
            }
        )
    }
    val refinedLogs: StateFlow<RefinedLogs> = transformerFlow.map { transformers ->
        val detectionResults = mutableMapOf<String, MutableList<DetectionResult>>()
        val refined = if (transformers.filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> transformers.filters.any { it(log) } }
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

    private val keywordDetections = refinedLogs.map {
        it.detectionResults["keyword"] ?: emptyList()
    }.stateIn(logScope, SharingStarted.Lazily, emptyList())

    private val refreshedFindResult = keywordDetections
        .map { results -> results.firstOrNull()?.let { FindResult(it, results) } }

    private val findResultChangeFromUser = MutableStateFlow<FindResult?>(null)

    val findResultFlowState = merge(refreshedFindResult, findResultChangeFromUser)
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

    fun findEnabled(enabled: Boolean) {
        findEnabledStateFlow.value = enabled
    }

    fun previousFindResult(findResult: FindResult) {
        val results = findResult.detectionResults
        val previousIndex = if (findResult.focusingResult.detectionIndex <= 0) {
            results.size - 1
        } else {
            findResult.focusingResult.detectionIndex - 1
        }

        findResultChangeFromUser.value = FindResult(results[previousIndex], results)
    }

    fun nextFindResult(findResult: FindResult) {
        val results = findResult.detectionResults
        val nextIndex = if (findResult.focusingResult.detectionIndex >= results.size - 1) {
            0
        } else {
            findResult.focusingResult.detectionIndex + 1
        }

        findResultChangeFromUser.value = FindResult(results[nextIndex], results)
    }
}
