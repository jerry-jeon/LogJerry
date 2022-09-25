package com.jerryjeon.logjerry.ui

import Priority
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.log.refine.PriorityFilter

@Composable
fun PriorityFilterView(
    priorityFilter: PriorityFilter,
    changePriorityFilter: (PriorityFilter) -> Unit
) {
    var value by remember(priorityFilter) { mutableStateOf(priorityFilter.priority.ordinal.toFloat()) }
    Column(Modifier.width(IntrinsicSize.Min).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).padding(8.dp)) {
        Text("Log level")
        Spacer(Modifier.height(8.dp))
        val priorities = Priority.values()

        Slider(
            modifier = Modifier.width(300.dp).height(50.dp),
            value = value,
            onValueChange = { value = it },
            steps = priorities.size - 2,
            valueRange = 0f..(priorities.size - 1).toFloat(),
            onValueChangeFinished = {
                val priority = priorities[value.toInt()]
                changePriorityFilter(PriorityFilter(priority))
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val middle = priorities.size / 2
            priorities.forEachIndexed { index, priority ->
                Text(
                    priority.fullText,
                    modifier = Modifier.width(40.dp),
                    style = LocalTextStyle.current.copy(fontSize = 11.sp),
                    textAlign = when {
                        index < middle -> TextAlign.Start
                        index == middle -> TextAlign.Center
                        else -> TextAlign.End
                    }
                )
            }
        }
    }
}
