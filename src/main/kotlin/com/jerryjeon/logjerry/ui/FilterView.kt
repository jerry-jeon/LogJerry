package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.filter.FilterManager

@Composable
fun FilterView(
    filterManager: FilterManager,
) {
    val textFilters by filterManager.textFiltersFlow.collectAsState()
    val priorityFilters by filterManager.priorityFilterFlow.collectAsState()

    TextFilterView(textFilters, filterManager::addTextFilter, filterManager::removeTextFilter)
    Spacer(Modifier.width(16.dp))
    PriorityFilterView(priorityFilters, filterManager::setPriorityFilter)
    Spacer(Modifier.width(16.dp))
}