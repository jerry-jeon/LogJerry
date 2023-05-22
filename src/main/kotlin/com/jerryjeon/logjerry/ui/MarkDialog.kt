@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.mark.LogMark
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed

@Composable
fun MarkDialog(
    showMarkDialog: MutableState<RefinedLog?>,
    setMark: (logMark: LogMark) -> Unit
) {
    var note by remember { mutableStateOf("") }
    val colors = listOf(
        Color(0xFFFDF4F5),
        Color(0xFFE8A0BF),
        Color(0xFFBA90C6),
        Color(0xFFC0DBEA),
        Color(0xFFFFF2CC),
        Color(0xFFFFD966),
    )
    var selectedColorIndex by remember { mutableStateOf(0) }

    val targetLog = showMarkDialog.value
    if (targetLog != null) {
        Dialog(
            onCloseRequest = { showMarkDialog.value = null },
            title = "Mark a row",
            state = DialogState(width = 400.dp, height = 600.dp),
            onPreviewKeyEvent = { keyEvent ->
                when {
                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown -> {
                        showMarkDialog.value = null
                        true
                    }

                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown -> {
                        setMark(LogMark(targetLog.detectionFinishedLog.log, note, colors[selectedColorIndex]))
                        showMarkDialog.value = null
                        true
                    }

                    else -> {
                        false
                    }
                }
            },
            resizable = false
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") }
                )

                Spacer(androidx.compose.ui.Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    colors.forEachIndexed { index, color ->
                        val baseModifier =
                            androidx.compose.ui.Modifier.size(40.dp).background(color, shape = CircleShape)
                                .onClick { selectedColorIndex = index }
                        if (selectedColorIndex == index) {
                            Box(modifier = baseModifier.border(2.dp, Color.Black, shape = CircleShape))
                        } else {
                            Box(modifier = baseModifier)
                        }
                    }
                }

                Spacer(androidx.compose.ui.Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        showMarkDialog.value = null
                    }) {
                        Text("Cancel")
                    }
                    Spacer(androidx.compose.ui.Modifier.width(12.dp))
                    Button(onClick = {
                        setMark(LogMark(targetLog.detectionFinishedLog.log, note, colors[selectedColorIndex]))
                        showMarkDialog.value = null
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}