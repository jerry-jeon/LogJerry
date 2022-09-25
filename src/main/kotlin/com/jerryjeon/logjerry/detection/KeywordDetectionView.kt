@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.detection

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val focusRequester = remember { FocusRequester() }

    Box(modifier.padding(8.dp)) {
        OutlinedTextField(
            modifier = Modifier.focusRequester(focusRequester).onPreviewKeyEvent {
                when {
                    it.key == Key.Enter && it.type == KeyEventType.KeyDown -> {
                        detectionFocus?.let { focus ->
                            if (it.isShiftPressed) {
                                moveToPreviousOccurrence(focus)
                            } else {
                                moveToNextOccurrence(focus)
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
            value = keywordDetectionRequest.keyword,
            onValueChange = { find(it) },
            leadingIcon = { Icon(Icons.Default.Search, "Search") },
            trailingIcon = {
                Row {
                    detectionFocus?.let {
                        if (it.focusing == null) {
                            Text(
                                "${it.allDetections.size} results",
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

    LaunchedEffect(keywordDetectionRequest::class) {
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
private fun KeywordDetectionViewPreview() {
}
