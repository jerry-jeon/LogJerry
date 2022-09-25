package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Priority
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class FilterManager {
    private val transformationScope = MainScope()

    val textFiltersFlow: MutableStateFlow<List<TextFilter>> = MutableStateFlow(emptyList())
    val priorityFilterFlow: MutableStateFlow<PriorityFilter> = MutableStateFlow(PriorityFilter(Priority.Verbose))
    val filtersFlow = combine(textFiltersFlow, priorityFilterFlow) { textFilters, priorityFilter ->
        textFilters + listOf(priorityFilter)
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
}
