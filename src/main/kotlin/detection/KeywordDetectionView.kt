package detection

import DetectionFocus
import DetectionKey
import DetectionResult
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import log.SampleData

@Composable
fun KeywordDetectionView(
    modifier: Modifier = Modifier,
    keywordDetectionRequest: KeywordDetectionRequest,
    detectionFocus: DetectionFocus?,
    find: (String) -> Unit,
    setFindEnabled: (Boolean) -> Unit,
    moveToPreviousOccurrence: (DetectionFocus) -> Unit,
    moveToNextOccurrence: (DetectionFocus) -> Unit,
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp),
    ) {
        when (keywordDetectionRequest) {
            is KeywordDetectionRequest.TurnedOn -> {
                KeywordDetectionRequestViewTurnedOn(
                    modifier,
                    keywordDetectionRequest,
                    find,
                    setFindEnabled,
                    detectionFocus,
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
    modifier: Modifier,
    keywordDetectionRequest: KeywordDetectionRequest.TurnedOn,
    find: (String) -> Unit,
    setFindEnabled: (Boolean) -> Unit,
    detectionFocus: DetectionFocus?,
    moveToPreviousOccurrence: (DetectionFocus) -> Unit,
    moveToNextOccurrence: (DetectionFocus) -> Unit
) {
    Box(modifier.padding(8.dp)) {
        OutlinedTextField(
            value = keywordDetectionRequest.keyword,
            onValueChange = { find(it) },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            trailingIcon = {
                Row {
                    detectionFocus?.let {
                        if (it.focusing == null) {
                            Text(
                                "${it.results.size} results",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        } else {
                            Text(
                                "${it.currentIndexInView} / ${detectionFocus.totalCount}",
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
        val results = listOf(DetectionResult(DetectionKey.Keyword, SpanStyle(), listOf(0..3), SampleData.log, 0))
        KeywordDetectionView(
            Modifier,
            KeywordDetectionRequest.TurnedOn("Searching keyword"),
            DetectionFocus(DetectionKey.Keyword, 0, results[0], results),
            {},
            {},
            {},
            {}
        )
    }
}
