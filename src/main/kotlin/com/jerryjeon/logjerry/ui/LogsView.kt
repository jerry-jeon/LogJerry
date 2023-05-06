package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.ColumnDivider
import com.jerryjeon.logjerry.HeaderDivider
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.detector.MarkDetection
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.mark.LogMark
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
    setMark: (logMark: LogMark) -> Unit,
    deleteMark: (logIndex: Int) -> Unit,
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
                                setMark = setMark,
                                deleteMark = deleteMark,
                                selectLog = selectLog
                            )
                        }
                        val mark = refinedLog.detectionFinishedLog.detections[DetectorKey.Mark]?.firstOrNull() as? MarkDetection
                        if(mark != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight().background(mark.color)
                                    .padding(start = 6.dp, top = 0.dp, end = 6.dp, bottom = 6.dp)
                            ) {
                                Text(
                                    text = mark.note,
                                    modifier = Modifier.wrapContentHeight().align(Alignment.CenterHorizontally).padding(8.dp),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.h5,
                                )
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
