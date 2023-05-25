@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.mark.LogMark
import com.jerryjeon.logjerry.ui.focus.KeyboardFocus
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed

@Composable
fun MarkDialog(
    showMarkDialog: MutableState<RefinedLog?>,
    setMark: (logMark: LogMark) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    val targetLog = showMarkDialog.value
    if (targetLog != null) {
        var note by remember { mutableStateOf(targetLog.mark?.note ?: "") }
        val colors = listOf(
            Color(0xFFFDF4F5),
            Color(0xFFE8A0BF),
            Color(0xFFBA90C6),
            Color(0xFFC0DBEA),
            Color(0xFFFFF2CC),
            Color(0xFFFFD966),
        )
        var selectedColorIndex by remember {
            mutableStateOf(colors.indexOfFirst { it == targetLog.mark?.color }.coerceAtLeast(0))
        }
        val cancelFunction = {
            note = ""
            showMarkDialog.value = null
        }
        val okFunction = {
            setMark(LogMark(targetLog.detectionFinishedLog.log, note, colors[selectedColorIndex]))
            note = ""
            showMarkDialog.value = null
        }
        Dialog(
            onCloseRequest = { showMarkDialog.value = null },
            title = "Mark a row",
            state = DialogState(width = 400.dp, height = 600.dp),
            onPreviewKeyEvent = { keyEvent ->
                when {
                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown -> {
                        cancelFunction()
                        true
                    }
                    keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown -> {
                        okFunction()
                        true
                    }
                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.DirectionRight && keyEvent.type == KeyEventType.KeyDown -> {
                        if(selectedColorIndex >= colors.size - 1) {
                            selectedColorIndex = 0
                        } else {
                            selectedColorIndex++
                        }
                        true
                    }
                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.DirectionLeft && keyEvent.type == KeyEventType.KeyDown -> {
                        if(selectedColorIndex <= 0) {
                            selectedColorIndex = colors.size - 1
                        } else {
                            selectedColorIndex--
                        }
                        true
                    }
                    else -> {
                        false
                    }
                }
            },
            resizable = false
        ) {
            Surface(color = MaterialTheme.colors.surface, contentColor = MaterialTheme.colors.onSurface) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    TextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") }
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        colors.forEachIndexed { index, color ->
                            val baseModifier =
                                Modifier.size(40.dp).background(color, shape = CircleShape)
                                    .onClick { selectedColorIndex = index }
                            val modifier = if (selectedColorIndex == index) {
                                baseModifier.border(2.dp, MaterialTheme.colors.onSurface, shape = CircleShape)
                            } else {
                                baseModifier
                            }
                            Box(modifier = modifier) {
                                if (selectedColorIndex == index) {
                                    Image(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Checked color",
                                        modifier = Modifier.size(20.dp).align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(onClick = cancelFunction) {
                            Text("Cancel")
                        }
                        Spacer(androidx.compose.ui.Modifier.width(12.dp))
                        Button(onClick = okFunction) {
                            Text("OK")
                        }
                    }
                }
            }

        }
        LaunchedEffect(targetLog) {
            focusRequester.requestFocus()
        }
    }
}