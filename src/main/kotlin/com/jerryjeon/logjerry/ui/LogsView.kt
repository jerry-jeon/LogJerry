package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.ColumnDivider
import com.jerryjeon.logjerry.HeaderDivider
import com.jerryjeon.logjerry.detector.DetectorKey
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
                        val logRow: @Composable () -> Unit = {
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
                        }
                        val marked = DetectorKey.Mark in refinedLog.detectionFinishedLog.detections.keys
                        if(marked) {
                            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight().background(Color.Cyan).padding(start = 4.dp, top = 30.dp, end = 4.dp, bottom = 4.dp)) {
                                Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                                    logRow()
                                }
                            }
                        } else {
                            logRow()
                        }
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
