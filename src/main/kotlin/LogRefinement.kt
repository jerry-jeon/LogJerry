import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class LogRefinement(
    private val originalLogs: List<Log>
) {
    val filtersFlow = MutableStateFlow(emptyList<Filter>())
    val refinedLogs = filtersFlow.map { filters ->
        originalLogs.filter { log ->
            filters.none { it.text !in log.log }
        }
    }

    fun addFilter(filter: Filter) {
        filtersFlow.value = filtersFlow.value + filter
    }

    fun removeFilter(filter: Filter) {
        filtersFlow.value = filtersFlow.value - filter
    }
}
