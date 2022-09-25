import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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
                is FindStatus.TurnedOn -> listOfNotNull(annotateWithSearchKeyword(findStatus.keyword))
                FindStatus.TurnedOff -> emptyList()
            }
        )
    }
    val refinedLogs = transformerFlow.map { transformers ->
        if (transformers.filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> transformers.filters.any { it(log) } }
        }
            .map { log ->
                transformers.formatters.fold(log) { acc, formatter -> formatter(acc) }
            }
    }

    data class Transformers(
        val filters: List<(Log) -> Boolean>,
        val formatters: List<(Log) -> Log>
    )

    private fun annotateWithSearchKeyword(keyword: String): (Log) -> Log {
        return if (keyword.isBlank()) { log: Log -> log }
        else { log: Log ->
            var startIndex = 0
            val indexRanges = mutableListOf<IntRange>()
            while (startIndex != -1) {
                startIndex = log.originalLog.indexOf(keyword, startIndex)
                if (startIndex != -1) {
                    indexRanges.add(startIndex..startIndex + keyword.length)
                    startIndex += keyword.length
                }
            }

            log.copy(
                log = indexRanges.fold(AnnotatedString.Builder(log.originalLog)) { builder, range ->
                    builder.apply {
                        addStyle(SpanStyle(background = Color.Red), range.first, range.last)
                    }
                }.toAnnotatedString()
            )
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
