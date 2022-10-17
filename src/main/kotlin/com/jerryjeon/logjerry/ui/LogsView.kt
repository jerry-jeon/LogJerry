package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jerryjeon.logjerry.ColumnDivider
import com.jerryjeon.logjerry.HeaderDivider
import com.jerryjeon.logjerry.detector.DetectionFocus
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header

@Composable
fun LogsView(
    preferences: Preferences,
    header: Header,
    logs: List<RefinedLog>,
    detectionFocus: DetectionFocus?,
    collapseJsonDetection: (JsonDetection) -> Unit,
    expandJsonDetection: (annotation: String) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(detectionFocus) {
        detectionFocus?.focusing?.let {
            listState.scrollToItem(it.logIndex)
        }
    }

    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
            item { HeaderRow(header, divider) }
            item { HeaderDivider() }
            logs.forEach {
                item {
                    Column {
                        LogRow(it, preferences, header, divider = divider, collapseJsonDetection = collapseJsonDetection, expandJsonDetection = expandJsonDetection)
                        Divider()
                    }
                }
            }
        }
        val adapter = rememberScrollbarAdapter(listState)
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = adapter
        )
    }
}
