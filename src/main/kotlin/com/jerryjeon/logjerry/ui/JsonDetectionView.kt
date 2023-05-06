package com.jerryjeon.logjerry.ui

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
import com.jerryjeon.logjerry.detector.DetectionSelection
import com.jerryjeon.logjerry.detector.JsonDetection

@Composable
fun JsonDetectionView(
    modifier: Modifier,
    detectionSelection: DetectionSelection?,
    moveToPreviousOccurrence: (DetectionSelection) -> Unit,
    moveToNextOccurrence: (DetectionSelection) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp),
    ) {
        Box(modifier = modifier) {
            Column(Modifier.height(IntrinsicSize.Min).padding(8.dp)) {
                val title = buildAnnotatedString {
                    append("Json ")
                    withStyle(JsonDetection.detectedStyle) {
                        append("{ \"key\" : \"value\" }")
                    }
                }
                Text(title)
                if (detectionSelection == null) {
                    Spacer(Modifier.height(16.dp))
                    Text("No results", textAlign = TextAlign.Center)
                } else {
                    JsonDetectionSelectionExist(detectionSelection, moveToPreviousOccurrence, moveToNextOccurrence)
                }
            }
        }
    }
}

@Composable
fun JsonDetectionSelectionExist(
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

        // TODO cleanup ; don't cast
        /*
                                        (it.focusing as? JsonDetectionResult)?.let {
                                            Text(it.jsonList) // TODO show summary of json
                                        }
                        */
    }
}
