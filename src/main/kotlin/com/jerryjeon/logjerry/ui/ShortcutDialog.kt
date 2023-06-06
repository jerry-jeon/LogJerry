@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed

@Composable
fun ShortcutDialog(
    isOpen: MutableState<Boolean>,
) {
    if (isOpen.value) {
        Window(
            state = WindowState(width = 1200.dp, height = 1200.dp),
            onCloseRequest = { isOpen.value = false },
            onPreviewKeyEvent = { keyEvent ->
                if (keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown) {
                    isOpen.value = false
                }
                false
            }
        ) {
            Surface(color = MaterialTheme.colors.surface, contentColor = MaterialTheme.colors.onSurface) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text(
                        """
                               ⌘ + O - Open File 
                               ⌘ + N - New Tab
                               ⌘ + V - (When new tab has opened) Paste from clipboard
                               
                               ⌘ + M - Mark a selected log
                               ← - Hide a selected log
                               ⌘ + [ - Move to previous mark
                               ⌘ + ] - Move to next mark
                               ⌘ + F - Find..
                               ⌘ + C - Copy the content of the selected log
                               
                               Navigation
                               ↑ - Move to previous log
                               ↓ - Move to next log
                               PgUp - Move to previous page
                               PgDown - Move to next page
                               Home - Move to the top
                               End - Move to the bottom
                        """.trimIndent()
                    )
                }
            }
        }
    }
}
