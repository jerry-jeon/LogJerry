@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.jerryjeon.logjerry.log.Priority

@Composable
fun PreferencesView(isOpen: MutableState<Boolean>, preferencesState: MutableState<Preferences>) {
    val viewModel = PreferencesViewModel(preferencesState)
    val colorStrings = viewModel.colorStrings.collectAsState()
    val validColorsByPriority = viewModel.validColorsByPriority.collectAsState()
    val saveEnabled = viewModel.saveEnabled.collectAsState()

    if (isOpen.value) {
        Window(
            onCloseRequest = { isOpen.value = false },
            onPreviewKeyEvent = { keyEvent ->
                if (keyEvent.isMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown) {
                    isOpen.value = false
                }
                false
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Column {
                    Text("Log colors")
                    Spacer(Modifier.height(16.dp))
                    Priority.values().forEach { priority ->
                        val color = validColorsByPriority.value.getValue(priority)
                        val isError = (color == null)
                        val defaultStyle = MaterialTheme.typography.body1
                        val style = color?.let { defaultStyle.copy(color = it) } ?: defaultStyle
                        Row {
                            Text(
                                priority.name,
                                modifier = Modifier.width(80.dp).align(Alignment.CenterVertically),
                                style = style
                            )
                            Column {
                                TextField(
                                    value = colorStrings.value.getValue(priority),
                                    onValueChange = { viewModel.changeColorString(priority, it) },
                                    modifier = Modifier.width(120.dp),
                                    isError = isError,
                                    singleLine = true
                                )
                            }
                        }
                        Text(
                            text = "Invalid color",
                            color = MaterialTheme.colors.error,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(start = 16.dp).alpha(if (isError) 1f else 0f)
                                .align(Alignment.End),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                Row {
                    Button(enabled = saveEnabled.value, onClick = {
                        viewModel.save()
                        isOpen.value = false
                    }) {
                        Text("Save")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(onClick = { viewModel.restoreToDefault() }) {
                        Text("Restore to default")
                    }
                }
            }
        }
    }
}
