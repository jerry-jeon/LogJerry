@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed

@Composable
fun InvalidSentencesDialog(invalidSentences: List<Pair<Int, String>>) {
    var showInvalidSentence by remember { mutableStateOf(invalidSentences.isNotEmpty()) }
    if (showInvalidSentence) {
        Dialog(
            onCloseRequest = { showInvalidSentence = false },
            title = "Invalid sentences",
            state = DialogState(width = 800.dp, height = 600.dp),
            onPreviewKeyEvent = { keyEvent ->
                if (keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown) {
                    showInvalidSentence = false
                }
                false
            }
        ) {
            Surface(color = MaterialTheme.colors.surface, contentColor = MaterialTheme.colors.onSurface) {
                InvalidSentences(invalidSentences)
            }
        }
    }
}

@Composable
private fun InvalidSentences(invalidSentences: List<Pair<Int, String>>) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "There were ${invalidSentences.size} invalid sentences",
            style = MaterialTheme.typography.body1
        )
        Text(
            "Supported format : \"date time pid-tid/packageName priority/tag: log\"",
            style = MaterialTheme.typography.body1
        )
        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(16.dp))
        invalidSentences.forEach { (index, s) ->
            Row(Modifier.height(IntrinsicSize.Min)) {
                Text("Line ${index + 1}")
                Spacer(Modifier.width(4.dp))
                Divider(Modifier.width(1.dp).fillMaxHeight())
                Spacer(Modifier.width(4.dp))
                Text(s, color = MaterialTheme.colors.primary)
            }
        }
    }
}
