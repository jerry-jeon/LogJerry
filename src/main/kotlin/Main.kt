// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import parse.DefaultParser
import java.io.File

@Composable
@Preview
fun App() {
    val parser = DefaultParser()
    MyTheme {
        var logs by remember { mutableStateOf(emptyList<Log>()) }
        val headerState = remember { mutableStateOf(Header.default) }
        val header by headerState

        Column {
            ColumnVisibility(headerState)
            Button(onClick = {
                val fileDialog = java.awt.FileDialog(ComposeWindow())
                fileDialog.isVisible = true
                fileDialog.file?.let {
                    val file = File(File(fileDialog.directory), it)
                    logs = parser.parse(file.readLines())
                }
            }) {
                Text("File Picker")
            }

            val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }

            LazyColumn(Modifier.padding(10.dp)) {
                item { HeaderRow(header, divider) }
                item { HeaderDivider() }
                logs.forEach {
                    item { LogRow(it, header, divider = divider) }
                }
            }
        }

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
    Window(
        state = WindowState(width = 1600.dp, height = 800.dp),
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
