import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class LogManager(
    val originalLogs: List<Log>
) {
    private val logScope = MainScope()

    private val findEnabledStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val findKeywordFlow = MutableStateFlow("")
    val findStatusFlow = combine(findEnabledStateFlow, findKeywordFlow) { enabled, keyword ->
        if (enabled) {
            FindStatus.TurnedOn(keyword, 0, 0)
        } else {
            FindStatus.TurnedOff
        }
    }.stateIn(logScope, SharingStarted.Lazily, FindStatus.TurnedOff)

    val filtersFlow: MutableStateFlow<List<Filter>> = MutableStateFlow(emptyList())
    private val transformerFlow = combine(filtersFlow, findStatusFlow) { filters, findStatus ->
        Transformers(
            filters.map { filter ->
                { log ->
                    filter.text in log.log
                }
            },
            when (findStatus) {
                is FindStatus.TurnedOn -> listOf(KeywordDetection(findStatus.keyword))
                FindStatus.TurnedOff -> emptyList()
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
            .mapIndexed { index, log ->
                transformers.detections.fold(log) { acc, detection ->
                    val changedLog = doDetection(detection, acc)
                    (detectionResults.getOrPut(detection.key) { mutableListOf() })
                        .add(DetectionResult(detection.key, index))
                    changedLog
                }
            }
        RefinedLogs(originalLogs, refined, detectionResults)
    }.stateIn(logScope, SharingStarted.Lazily, RefinedLogs(emptyList(), emptyList(), emptyMap()))

    data class Transformers(
        val filters: List<(Log) -> Boolean>,
        val detections: List<Detection>
    )

    private fun doDetection(detection: Detection, log: Log): Log {
        val indexRanges = detection.detect(log)

        return log.copy(
            log = indexRanges.fold(AnnotatedString.Builder(log.originalLog)) { builder, range ->
                builder.apply {
                    addStyle(detection.detectedStyle, range.first, range.last)
                }
            }.toAnnotatedString()
        )
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
}
