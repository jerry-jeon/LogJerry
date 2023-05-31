package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

class FilterManager {
    val textFiltersFlow: MutableStateFlow<List<TextFilter>> = MutableStateFlow(emptyList())
    val priorityFilterFlow: MutableStateFlow<PriorityFilter> = MutableStateFlow(PriorityFilter(Priority.Verbose))
    val hiddenLogIndicesFlow: MutableStateFlow<HiddenFilter> = MutableStateFlow(HiddenFilter(emptySet()))

    val filtersFlow = combine(
        textFiltersFlow,
        priorityFilterFlow,
        hiddenLogIndicesFlow
    ) { textFilters, priorityFilter, hiddenLogIndices ->
        textFilters + listOf(priorityFilter, hiddenLogIndices)
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
}
