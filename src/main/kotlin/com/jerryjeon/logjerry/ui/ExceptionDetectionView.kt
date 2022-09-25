package com.jerryjeon.logjerry.ui

import DetectionFocus
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.detection.ExceptionDetectionResult

@Composable
fun ExceptionDetectionView(
    modifier: Modifier,
    detectionFocus: DetectionFocus?,
    moveToPreviousOccurrence: (DetectionFocus) -> Unit,
    moveToNextOccurrence: (DetectionFocus) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp),
    ) {
        Box(modifier = modifier) {
            Column(Modifier.height(IntrinsicSize.Min).padding(8.dp)) {
                Text(AnnotatedString("Exception", spanStyle = ExceptionDetectionResult.detectedStyle))
                if (detectionFocus == null) {
                    Spacer(Modifier.height(16.dp))
                    Text("No results", textAlign = TextAlign.Center)
                } else {
                    ExceptionDetectionFocusExist(detectionFocus, moveToPreviousOccurrence, moveToNextOccurrence)
                }
            }
        }
    }
}

@Composable
fun ExceptionDetectionFocusExist(
    focus: DetectionFocus,
    moveToPreviousOccurrence: (DetectionFocus) -> Unit,
    moveToNextOccurrence: (DetectionFocus) -> Unit
) {
    Row {
/* TODO not sure it's helpful... remove it because it looks bad
                        Row(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            // TODO cleanup ; don't cast
                            (it.focusing as? ExceptionDetectionResult)?.let {
                                Text(it.exception, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
*/
        Row(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (focus.focusing == null) {
                Text(
                    "${focus.results.size} results",
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
