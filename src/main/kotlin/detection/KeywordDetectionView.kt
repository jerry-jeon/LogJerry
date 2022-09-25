package detection

import DetectionResult
import DetectionResultFocus
import MyTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeywordDetectionView(
    keywordDetectionRequest: KeywordDetectionRequest,
    detectionResultFocus: DetectionResultFocus?,
    find: (String) -> Unit,
    setFindEnabled: (Boolean) -> Unit,
    moveToPreviousOccurrence: (DetectionResultFocus) -> Unit,
    moveToNextOccurrence: (DetectionResultFocus) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp),
    ) {
        when (keywordDetectionRequest) {
            is KeywordDetectionRequest.TurnedOn -> {
                KeywordDetectionRequestViewTurnedOn(
                    keywordDetectionRequest,
                    find,
                    setFindEnabled,
                    detectionResultFocus,
                    moveToPreviousOccurrence,
                    moveToNextOccurrence
                )
            }
            KeywordDetectionRequest.TurnedOff -> {}
        }
    }
}

@Composable
private fun KeywordDetectionRequestViewTurnedOn(
    keywordDetectionRequest: KeywordDetectionRequest.TurnedOn,
    find: (String) -> Unit,
    setFindEnabled: (Boolean) -> Unit,
    detectionResultFocus: DetectionResultFocus?,
    moveToPreviousOccurrence: (DetectionResultFocus) -> Unit,
    moveToNextOccurrence: (DetectionResultFocus) -> Unit
) {
    Box(Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = keywordDetectionRequest.keyword,
            onValueChange = { find(it) },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            trailingIcon = {
                Row {
                    detectionResultFocus?.let {
                        Text(
                            "${it.focusingResult.detectionIndexInView}/ ${detectionResultFocus.totalCount}",
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        IconButton(onClick = { moveToPreviousOccurrence(it) }) {
                            Icon(Icons.Default.KeyboardArrowUp, "Previous Occurrence")
                        }
                        IconButton(onClick = { moveToNextOccurrence(it) }) {
                            Icon(Icons.Default.KeyboardArrowDown, "Next Occurrence")
                        }
                    }
                    IconButton(onClick = { setFindEnabled(false) }) {
                        Icon(Icons.Default.Close, "Close find")
                    }
                }
            },
            singleLine = true
        )
    }
}

@Preview
@Composable
private fun KeywordDetectionViewPreview() {
    MyTheme {
        val results = listOf(
            DetectionResult("keyword", 0, 1),
            DetectionResult("keyword", 1, 1),
            DetectionResult("keyword", 2, 1),
            DetectionResult("keyword", 3, 1),
            DetectionResult("keyword", 4, 1),
        )
        KeywordDetectionView(
            KeywordDetectionRequest.TurnedOn("Searching keyword"),
            DetectionResultFocus(results[2], results),
            {},
            {},
            {},
            {}
        )
    }
}
