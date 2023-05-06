package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jerryjeon.logjerry.ColumnDivider
import com.jerryjeon.logjerry.HeaderDivider
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
    logSelection: LogSelection?,
    listState: LazyListState,
    collapseJsonDetection: (JsonDetection) -> Unit,
    expandJsonDetection: (annotation: String) -> Unit,
    toggleMark: (log: Log) -> Unit,
    selectLog: (RefinedLog) -> Unit,
) {
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
