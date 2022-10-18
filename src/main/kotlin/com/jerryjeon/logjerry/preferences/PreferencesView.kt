@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.preferences

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
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
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import com.jerryjeon.logjerry.DarkTheme
import com.jerryjeon.logjerry.LightTheme
import com.jerryjeon.logjerry.log.Priority
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed

@Composable
fun PreferencesView(
    isOpen: MutableState<Boolean>,
    viewModel: PreferencesViewModel,
) {
    val saveEnabled by viewModel.saveEnabled.collectAsState()
    val colorTheme by viewModel.colorThemeFlow.collectAsState()
    val expandJsonWhenLoad by viewModel.expandJsonWhenLoadFlow.collectAsState()

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
                    Column {
                        Text("General", style = MaterialTheme.typography.h5)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Expand all json when load the logs")
                            Spacer(Modifier.width(4.dp))
                            Checkbox(expandJsonWhenLoad, onCheckedChange = viewModel::changeExpandJsonWhenLoad)
                        }

                        Spacer(Modifier.height(8.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))

                        Text("Colors", style = MaterialTheme.typography.h5)
                        Spacer(Modifier.height(16.dp))
                        ThemeSelector(colorTheme, viewModel::changeColorTheme)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            WhiteThemeView(viewModel)
                            DarkThemeView(viewModel)
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
}

@Composable
fun WhiteThemeView(viewModel: PreferencesViewModel) {
    val lightColorStrings by viewModel.whiteColorStrings.collectAsState()
    val lightValidColorsByPriority by viewModel.whiteValidColorsByPriority.collectAsState()
    val lightBackgroundColor by viewModel.whiteBackgroundValidColor.collectAsState()
    val lightBackgroundColorString by viewModel.whiteBackgroundColorString.collectAsState()
    val defaultBackgroundColor = viewModel.preferencesFlow.value.lightBackgroundColor
    LightTheme(viewModel.preferencesFlow.value) {
        Surface(color = lightBackgroundColor ?: defaultBackgroundColor, contentColor = MaterialTheme.colors.onSurface) {
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .border(1.dp, MaterialTheme.colors.onSurface)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("White theme", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(16.dp))
                Priority.values().forEach { priority ->
                    ColorChanger(
                        title = priority.name,
                        color = lightValidColorsByPriority.getValue(priority),
                        colorString = lightColorStrings.getValue(priority)
                    ) {
                        viewModel.changeWhiteColorString(priority, it)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                ColorChanger(
                    title = "Background",
                    color = MaterialTheme.colors.onSurface,
                    colorString = lightBackgroundColorString,
                    onColorChanged = viewModel::changeWhiteBackgroundColor
                )
            }
        }
    }
}

@Composable
fun DarkThemeView(viewModel: PreferencesViewModel) {
    val darkColorStrings by viewModel.darkColorStrings.collectAsState()
    val darkValidColorsByPriority by viewModel.darkValidColorsByPriority.collectAsState()
    val darkBackgroundColor by viewModel.darkBackgroundValidColor.collectAsState()
    val darkBackgroundColorString by viewModel.darkBackgroundColorString.collectAsState()
    val defaultBackgroundColor = viewModel.preferencesFlow.value.darkBackgroundColor
    DarkTheme(preferences = viewModel.preferencesFlow.value) {
        Surface(color = darkBackgroundColor ?: defaultBackgroundColor, contentColor = MaterialTheme.colors.onSurface) {
            Column(
                modifier = Modifier
                    .width(240.dp)
                    .border(1.dp, MaterialTheme.colors.onSurface)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Dark theme", style = MaterialTheme.typography.h6)
                Spacer(Modifier.height(16.dp))
                Priority.values().forEach { priority ->
                    ColorChanger(
                        title = priority.name,
                        color = darkValidColorsByPriority.getValue(priority),
                        colorString = darkColorStrings.getValue(priority)
                    ) {
                        viewModel.changeDarkColorString(priority, it)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                ColorChanger(
                    title = "Background",
                    color = MaterialTheme.colors.onSurface,
                    colorString = darkBackgroundColorString,
                    onColorChanged = viewModel::changeDarkBackgroundColor
                )
            }
        }
    }
}

@Composable
fun ColumnScope.ColorChanger(title: String, color: Color?, colorString: String, onColorChanged: (String) -> Unit) {
    val isError = (color == null)
    val defaultStyle = MaterialTheme.typography.body1
    val style = color?.let { defaultStyle.copy(color = it) } ?: defaultStyle
    Row {
        Text(
            text = title,
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
            style = style,
        )
        Column {
            TextField(
                value = colorString,
                onValueChange = onColorChanged,
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
}

@Composable
fun ThemeSelector(colorTheme: ColorTheme, onThemeSelected: (ColorTheme) -> Unit) {
    val radioOptions = ColorTheme.values().map { it.name }
    val selectedOption = colorTheme.name
    Row(Modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onThemeSelected(ColorTheme.valueOf(text)) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.body1.merge(),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}