// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import parse.DefaultParser
import java.awt.FileDialog
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
@Preview
fun App(sourceState: MutableState<Source>) {
    val parser = DefaultParser()
    var logs by remember { mutableStateOf(emptyList<Log>()) }

    var logRefinement: LogRefinement by remember { mutableStateOf(LogRefinement(emptyList())) }
    val refinedLogs by logRefinement.refinedLogs.collectAsState(emptyList())
    // Flow would be better
    val headerState = remember { mutableStateOf(Header.default) }

    LaunchedEffect(sourceState.value) {
        when (val source = sourceState.value) {
            is Source.File -> {
                logs = parser.parse(source.file.readLines())
                logRefinement = LogRefinement(logs)
            }

            is Source.Text -> {
                logs = parser.parse(source.text.split("\n"))
                logRefinement = LogRefinement(logs)
            }

            Source.None -> {}
        }
    }

    Column {
        ColumnVisibility(headerState)
        ChooseFileButton(sourceState)
        FilterView(logRefinement)
        val filteredSize = (if (refinedLogs.size != logs.size) "Filtered size : ${refinedLogs.size}, " else "")
        Text(filteredSize + "Total : ${logs.size}")
        LogsView(headerState.value, refinedLogs)
    }
}

@Composable
private fun FilterView(logRefinement: LogRefinement) {
    val filters by logRefinement.filtersFlow.collectAsState(emptyList())
    // Provide only useful column types
    var columnType by remember { mutableStateOf(ColumnType.Log) }
    val items = listOf(ColumnType.Log, ColumnType.PackageName)
    var text by remember { mutableStateOf("Text") }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
        Text(
            columnType.name,
            modifier = Modifier.width(300.dp).clickable(onClick = { expanded = true }).background(
                Color.Gray
            )
        )
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            items.forEach {
                DropdownMenuItem(onClick = {
                    columnType = it
                    expanded = false
                }) {
                    Text(text = it.name)
                }
            }
        }
    }
    TextField(
        value = text,
        onValueChange = {
            text = it
        }
    )
    Button(onClick = {
        logRefinement.addFilter(Filter(columnType, text))
        text = ""
    }) {
        Text("Add filter")
    }
    filters.forEach { filter ->
        Box(Modifier.background(color = Color.Cyan)) {
            Row {
                Button(onClick = {
                    logRefinement.removeFilter(filter)
                }) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
                Text(filter.columnType.toString())
                Text(filter.text)
            }
        }
    }
}

@Composable
private fun LogsView(header: Header, logs: List<Log>) {
    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    LazyColumn(Modifier.padding(10.dp)) {
        item { HeaderRow(header, divider) }
        item { HeaderDivider() }
        logs.forEach {
            item { LogRow(it, header, divider = divider) }
        }
    }
}

@Composable
fun ChooseFileButton(sourceState: MutableState<Source>) {
    Button(onClick = {
        val fileDialog = FileDialog(ComposeWindow())
        fileDialog.isVisible = true
        fileDialog.file?.let {
            val file = File(File(fileDialog.directory), it)
            sourceState.value = Source.File(file)
        }
    }) {
        Text("File Picker")
    }
}

@Composable
fun ColumnVisibility(headerState: MutableState<Header>) {
    val asColumnList = headerState.value.asColumnList

    Column {
        asColumnList.chunked(4).forEach { chunked ->
            Row {
                chunked.forEach { columnInfo ->
                    ColumnCheckBox(columnInfo, headerState)
                }
            }
        }
    }
}

@Composable
private fun ColumnCheckBox(columnInfo: ColumnInfo, headerState: MutableState<Header>) {
    var header by headerState
    Row {
        Text(columnInfo.columnType.name)
        Checkbox(columnInfo.visible, onCheckedChange = {
            header = header.copyOf(columnInfo.columnType, columnInfo.copy(visible = it))
        })
    }
}

@Composable
fun ColumnDivider() {
    Box(Modifier.padding(horizontal = 5.dp)) {
        Divider(Modifier.fillMaxHeight().width(1.dp))
    }
}

@Composable
fun HeaderDivider() {
    Spacer(Modifier.height(3.dp))
    Divider()
    Spacer(Modifier.height(3.dp))
}

@Composable
fun MyTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

fun main() = application {
    val sourceState: MutableState<Source> = remember { mutableStateOf(Source.None) }
    Window(
        state = WindowState(width = 1600.dp, height = 800.dp),
        onCloseRequest = ::exitApplication,
        onPreviewKeyEvent = { keyEvent ->
            if (keyEvent.isMetaPressed && keyEvent.key == Key.V && keyEvent.type == KeyEventType.KeyUp) {
                Toolkit.getDefaultToolkit()
                    .systemClipboard
                    .getData(DataFlavor.stringFlavor)
                    .takeIf { it is String }
                    ?.let { sourceState.value = Source.Text(it.toString()) }
            }
            false
        }
    ) {
        MyTheme {
            App(sourceState)
        }
    }
}
