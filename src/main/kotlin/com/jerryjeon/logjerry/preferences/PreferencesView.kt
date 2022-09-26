@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.preferences

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import com.jerryjeon.logjerry.log.Priority

@Composable
fun PreferencesView(
    isOpen: MutableState<Boolean>,
    viewModel: PreferencesViewModel,
) {
    val colorStrings by viewModel.colorStrings.collectAsState()
    val validColorsByPriority by viewModel.validColorsByPriority.collectAsState()
    val saveEnabled by viewModel.saveEnabled.collectAsState()
    val expandJsonWhenLoad by viewModel.expandJsonWhenLoadFlow.collectAsState()

    if (isOpen.value) {
        Window(
            onCloseRequest = { isOpen.value = false },
            onPreviewKeyEvent = { keyEvent ->
                if (keyEvent.isCtrlPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown) {
                    isOpen.value = false
                }
                false
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row {
                    Column(
                        modifier = Modifier.border(1.dp, Color.Black).padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Log colors", style = MaterialTheme.typography.h4)
                        Spacer(Modifier.height(16.dp))
                        Priority.values().forEach { priority ->
                            val color = validColorsByPriority.getValue(priority)
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
                                        value = colorStrings.getValue(priority),
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
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Expand all json when load the logs")
                        Spacer(Modifier.width(4.dp))
                        Checkbox(expandJsonWhenLoad, onCheckedChange = viewModel::changeExpandJsonWhenLoad)
                    }
                }
                Row(modifier = Modifier.align(Alignment.BottomEnd)) {
                    Button(onClick = { viewModel.restoreToDefault() }) {
                        Text("Restore to default")
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(enabled = saveEnabled, onClick = {
                        viewModel.save()
                        isOpen.value = false
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
