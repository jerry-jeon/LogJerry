package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.detector.DetectionStatus

@Composable
fun JsonDetectionView(
    modifier: Modifier = Modifier,
    detectionStatus: DetectionStatus,
    moveToPreviousOccurrence: (DetectionStatus) -> Unit,
    moveToNextOccurrence: (DetectionStatus) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp),
    ) {
        Row(
            modifier
                .width(220.dp)
                .fillMaxHeight()
                .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Json")
            JsonDetectionSelectionExist(detectionStatus, moveToPreviousOccurrence, moveToNextOccurrence)
        }
    }
}

@Composable
fun JsonDetectionSelectionExist(
    selection: DetectionStatus,
    moveToPreviousOccurrence: (DetectionStatus) -> Unit,
    moveToNextOccurrence: (DetectionStatus) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier) {
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
