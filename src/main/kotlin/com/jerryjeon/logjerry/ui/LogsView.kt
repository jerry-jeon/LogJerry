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
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header

@Composable
fun LogsView(
    modifier: Modifier = Modifier,
    preferences: Preferences,
    header: Header,
    logs: List<RefinedLog>,
    detectionFocus: DetectionFocus?,
    logSelection: LogSelection?,
    collapseJsonDetection: (JsonDetection) -> Unit,
    expandJsonDetection: (annotation: String) -> Unit,
    toggleMark: (log: Log) -> Unit,
    selectLog: (RefinedLog) -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(detectionFocus) {
        detectionFocus?.focusing?.let {
            listState.scrollToItem(it.logIndex)
        }
    }
    LaunchedEffect(logSelection) {
        logSelection?.index?.let {
            // TODO Seems like inefficient... :(
            val headerCount = 2
            val currentPosition = it + headerCount
            if (currentPosition < listState.firstVisibleItemIndex) {
                listState.scrollToItem(currentPosition)
            } else {
                val viewportHeight = listState.layoutInfo.viewportSize.height
                val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if(currentPosition > lastVisibleItemIndex) {
                    listState.scrollToItem(currentPosition, scrollOffset = -viewportHeight + 200)
                }
            }
        }
    }

    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
            item { HeaderRow(header, divider) }
            item { HeaderDivider() }
            logs.forEach { refinedLog ->
                item {
                    Column {
                        LogRow(
                            refinedLog = refinedLog,
                            preferences = preferences,
                            header = header,
                            selected = refinedLog == logSelection?.refinedLog,
                            divider = divider,
                            collapseJsonDetection = collapseJsonDetection,
                            expandJsonDetection = expandJsonDetection,
                            toggleMark = toggleMark,
                            selectLog = selectLog
                        )
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
