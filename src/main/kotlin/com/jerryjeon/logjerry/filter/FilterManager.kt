package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class FilterManager(
    originalLogsFlow: StateFlow<List<Log>>,
) {
    private val filterScope = CoroutineScope(Dispatchers.Default)

    val textFiltersFlow: MutableStateFlow<List<TextFilter>> = MutableStateFlow(emptyList())
    val priorityFilterFlow: MutableStateFlow<PriorityFilter> = MutableStateFlow(PriorityFilter(Priority.Verbose))
    val hiddenLogIndicesFlow: MutableStateFlow<HiddenFilter> = MutableStateFlow(HiddenFilter(emptySet()))
    val packageFiltersFlow = MutableStateFlow(PackageFilters(emptyList()))
    val tagFiltersFlow = MutableStateFlow(TagFilters(emptyList()))

    val filtersFlow = combine(
        textFiltersFlow,
        priorityFilterFlow,
        hiddenLogIndicesFlow,
        packageFiltersFlow,
        tagFiltersFlow,
    ) { textFilters, priorityFilter, hiddenLogIndices, packageFilters, tagFilters ->
        textFilters + listOf(priorityFilter, hiddenLogIndices) + packageFilters + tagFilters
    }

    init {
        originalLogsFlow.onEach { originalLogs ->
            val packageFilters = originalLogs.groupingBy { it.packageName }.eachCount()
                .map { (packageName, frequency) ->
                    PackageFilter(packageName, frequency, true)
                }
                .sortedByDescending { it.frequency }
                .let { PackageFilters(it) }
            packageFiltersFlow.value = packageFilters
        }
            .launchIn(filterScope)

        originalLogsFlow.onEach { originalLogs ->
            val tagFilters = originalLogs.groupingBy { it.tag }.eachCount()
                .map { (tag, frequency) ->
                    TagFilter(tag, frequency, true)
                }
                .sortedByDescending { it.frequency }
                .let { TagFilters(it) }
            tagFiltersFlow.value = tagFilters
        }
            .launchIn(filterScope)
    }

    fun addTextFilter(textFilter: TextFilter) {
        textFiltersFlow.value = textFiltersFlow.value + textFilter
    }

    fun removeTextFilter(textFilter: TextFilter) {
        textFiltersFlow.value = textFiltersFlow.value - textFilter
    }

    fun setPriorityFilter(priorityFilter: PriorityFilter) {
        this.priorityFilterFlow.value = priorityFilter
    }

    fun hide(logIndex: Int) {
        hiddenLogIndicesFlow.update { it.copy(hiddenLogIndices = it.hiddenLogIndices + logIndex) }
    }

    fun unhide(logIndex: Int) {
        hiddenLogIndicesFlow.update { it.copy(hiddenLogIndices = it.hiddenLogIndices - logIndex) }
    }

    fun togglePackageFilter(packageFilter: PackageFilter) {
        packageFiltersFlow.update { packageFilters ->
            packageFilters.copy(
                filters = packageFilters.filters.map {
                    if (it.packageName == packageFilter.packageName) {
                        it.copy(include = !it.include)
                    } else {
                        it
                    }
                }
            )
        }
    }

    fun toggleTagFilter(tagFilter: TagFilter) {
        tagFiltersFlow.update { tagFilters ->
            tagFilters.copy(
                filters = tagFilters.filters.map {
                    if (it.tag == tagFilter.tag) {
                        it.copy(include = !it.include)
                    } else {
                        it
                    }
                }
            )
        }
    }
}
