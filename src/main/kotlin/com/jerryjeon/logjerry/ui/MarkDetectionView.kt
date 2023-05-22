@file:OptIn(ExperimentalFoundationApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.detector.DetectionSelection

@Composable
fun MarkDetectionView(
    modifier: Modifier,
    detectionSelection: DetectionSelection?,
    moveToPreviousOccurrence: (DetectionSelection) -> Unit,
    moveToNextOccurrence: (DetectionSelection) -> Unit,
    openMarkedRowsTab: () -> Unit
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp),
    ) {
        Box(modifier = modifier) {
            Column(Modifier.height(IntrinsicSize.Min).padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 0.dp)) {
                Row {
                    Text("Marked rows")
                    Spacer(Modifier.width(16.dp))
                    Text("Open as a tab", modifier = Modifier.onClick { openMarkedRowsTab() }, color = MaterialTheme.colors.secondary)
                }
                if (detectionSelection == null) {
                    Spacer(Modifier.height(16.dp))
                    Text("No results", textAlign = TextAlign.Center)
                } else {
                    MarkDetectionSelectionExist(detectionSelection, moveToPreviousOccurrence, moveToNextOccurrence)
                }
            }
        }
    }
}

@Composable
fun MarkDetectionSelectionExist(
    selection: DetectionSelection,
    moveToPreviousOccurrence: (DetectionSelection) -> Unit,
    moveToNextOccurrence: (DetectionSelection) -> Unit
) {
    Column {
        Row {
            Row(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (selection.selected == null) {
                    Text(
                        "${selection.allDetections.size} results",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                } else {
                    Text(
                        "${selection.currentIndexInView} / ${selection.totalCount}",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            IconButton(onClick = { moveToPreviousOccurrence(selection) }) {
                Icon(Icons.Default.KeyboardArrowUp, "Previous Occurrence")
            }
            IconButton(onClick = { moveToNextOccurrence(selection) }) {
                Icon(Icons.Default.KeyboardArrowDown, "Next Occurrence")
            }
        }
    }
}
