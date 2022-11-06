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
import com.jerryjeon.logjerry.detector.DetectionFocus

@Composable
fun MarkDetectionView(
    modifier: Modifier,
    detectionFocus: DetectionFocus?,
    moveToPreviousOccurrence: (DetectionFocus) -> Unit,
    moveToNextOccurrence: (DetectionFocus) -> Unit,
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
                if (detectionFocus == null) {
                    Spacer(Modifier.height(16.dp))
                    Text("No results", textAlign = TextAlign.Center)
                } else {
                    MarkDetectionFocusExist(detectionFocus, moveToPreviousOccurrence, moveToNextOccurrence)
                }
            }
        }
    }
}

@Composable
fun MarkDetectionFocusExist(
    focus: DetectionFocus,
    moveToPreviousOccurrence: (DetectionFocus) -> Unit,
    moveToNextOccurrence: (DetectionFocus) -> Unit
) {
    Column {
        Row {
            Row(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (focus.focusing == null) {
                    Text(
                        "${focus.allDetections.size} results",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                } else {
                    Text(
                        "${focus.currentIndexInView} / ${focus.totalCount}",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
            IconButton(onClick = { moveToPreviousOccurrence(focus) }) {
                Icon(Icons.Default.KeyboardArrowUp, "Previous Occurrence")
            }
            IconButton(onClick = { moveToNextOccurrence(focus) }) {
                Icon(Icons.Default.KeyboardArrowDown, "Next Occurrence")
            }
        }
    }
}
