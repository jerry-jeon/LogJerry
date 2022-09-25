package ui

import Priority
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import log.refine.PriorityFilter

@Composable
fun PriorityFilterView(
    priorityFilter: PriorityFilter,
    changePriorityFilter: (PriorityFilter) -> Unit
) {
    var value by remember { mutableStateOf(priorityFilter.priority.ordinal.toFloat()) }
    Slider(
        modifier = Modifier.width(300.dp).height(50.dp),
        value = value,
        onValueChange = { value = it },
        steps = Priority.values().size - 2,
        valueRange = 0f..(Priority.values().size - 1).toFloat(),
        onValueChangeFinished = {
            val priority = Priority.values()[value.toInt()]
            changePriorityFilter(PriorityFilter(priority))
        },
    )
}
