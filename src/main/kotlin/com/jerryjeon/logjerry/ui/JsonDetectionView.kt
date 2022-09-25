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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.detection.JsonDetectionResult

@Composable
fun JsonDetectionView(
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
                val title = buildAnnotatedString {
                    append("Json ")
                    withStyle(JsonDetectionResult.detectedStyle) {
                        append("{ \"key\" : \"value\" }")
                    }
                }
                Text(title)
                if (detectionFocus == null) {
                    Spacer(Modifier.height(16.dp))
                    Text("No results", textAlign = TextAlign.Center)
                } else {
                    JsonDetectionFocusExist(detectionFocus, moveToPreviousOccurrence, moveToNextOccurrence)
                }
            }
        }
    }
}

@Composable
fun JsonDetectionFocusExist(
    focus: DetectionFocus,
    moveToPreviousOccurrence: (DetectionFocus) -> Unit,
    moveToNextOccurrence: (DetectionFocus) -> Unit
) {
    Column {
        Row {
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

        // TODO cleanup ; don't cast
        /*
                                        (it.focusing as? JsonDetectionResult)?.let {
                                            Text(it.jsonList) // TODO show summary of json
                                        }
                        */
    }
}
