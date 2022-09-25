package ui

import DetectionKey
import DetectionResultFocus
import MyTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import detection.ExceptionDetectionResult
import log.SampleData

@Composable
fun ExceptionDetectionView(
    detectionResultFocus: DetectionResultFocus?,
    moveToPreviousOccurrence: (DetectionResultFocus) -> Unit,
    moveToNextOccurrence: (DetectionResultFocus) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp),
    ) {
        ExceptionDetectionRequestViewTurnedOn(
            detectionResultFocus,
            moveToPreviousOccurrence,
            moveToNextOccurrence
        )
    }
}

@Composable
private fun ExceptionDetectionRequestViewTurnedOn(
    detectionResultFocus: DetectionResultFocus?,
    moveToPreviousOccurrence: (DetectionResultFocus) -> Unit,
    moveToNextOccurrence: (DetectionResultFocus) -> Unit
) {
    Row(Modifier.padding(8.dp)) {
        Text("Exceptions")
        Icon(Icons.Default.Close, "Search", Modifier.align(Alignment.CenterVertically))
        detectionResultFocus?.let {
            Column {
                Row {
                    if (it.focusing == null) {
                        Text(
                            "${it.results.size} results",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    } else {
                        Text(
                            "${it.currentIndexInView} / ${detectionResultFocus.totalCount}",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                    IconButton(onClick = { moveToPreviousOccurrence(it) }) {
                        Icon(Icons.Default.KeyboardArrowUp, "Previous Occurrence")
                    }
                    IconButton(onClick = { moveToNextOccurrence(it) }) {
                        Icon(Icons.Default.KeyboardArrowDown, "Next Occurrence")
                    }
                }

                // TODO cleanup ; don't cast
                (it.focusing as? ExceptionDetectionResult)?.let {
                    Text(it.exception)
                }
            }
        }
    }
}

@Preview
@Composable
private fun ExceptionDetectionViewPreview() {
    MyTheme {
        val results = listOf(
            ExceptionDetectionResult(DetectionKey.Exception, SpanStyle(), 0..1, SampleData.log, 0, "")
        )
        ExceptionDetectionView(
            DetectionResultFocus(DetectionKey.Exception, 1, results[2], results),
            {},
            {},
        )
    }
}
