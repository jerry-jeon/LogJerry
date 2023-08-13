@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.detector

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeywordDetectionView(
    modifier: Modifier = Modifier,
    keywordDetectionRequest: KeywordDetectionRequest,
    detectionStatus: DetectionStatus?,
    find: (String) -> Unit,
    setFindEnabled: (Boolean) -> Unit,
    moveToPreviousOccurrence: (DetectionStatus) -> Unit,
    moveToNextOccurrence: (DetectionStatus) -> Unit,
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
                    detectionStatus,
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
    detectionStatus: DetectionStatus?,
    moveToPreviousOccurrence: (DetectionStatus) -> Unit,
    moveToNextOccurrence: (DetectionStatus) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val textFieldValue by derivedStateOf {
        TextFieldValue(text = keywordDetectionRequest.keyword, selection = TextRange(keywordDetectionRequest.keyword.length))
    }

    Box(modifier.padding(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.focusRequester(focusRequester).onPreviewKeyEvent {
                when {
                    it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                        detectionStatus?.let { selection ->
                            if (it.isShiftPressed) {
                                moveToPreviousOccurrence(selection)
                            } else {
                                moveToNextOccurrence(selection)
                            }
                        }
                        true
                    }
                    it.key == Key.Escape && it.type == KeyEventType.KeyDown -> {
                        setFindEnabled(false)
                        true
                    }
                    else -> false
                }
            },
            value = textFieldValue,
            onValueChange = { find(it.text) },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            trailingIcon = {
                Row {
                    detectionStatus?.let {
                        if (it.selected == null) {
                            Text(
                                "${it.allDetections.size} results",
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        } else {
                            Text(
                                "${it.currentIndexInView} / ${detectionStatus.totalCount}",
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

    LaunchedEffect(keywordDetectionRequest::class) {
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
private fun KeywordDetectionViewPreview() {
}
