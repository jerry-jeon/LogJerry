package com.jerryjeon.logjerry.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.jerryjeon.logjerry.filter.PriorityFilter
import com.jerryjeon.logjerry.ui.PriorityFilterView

@Composable
fun PriorityFilterPopup(
    showPopup: Boolean,
    anchor: Offset,
    priorityFilter: PriorityFilter,
    dismiss: () -> Unit,
    setPriorityFilter: (PriorityFilter) -> Unit
) {
    BasePopup(showPopup, anchor, dismiss) {
        PriorityFilterView(priorityFilter, setPriorityFilter)
    }
}
