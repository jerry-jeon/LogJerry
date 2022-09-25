// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import parse.ParseResult
import java.awt.FileDialog
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
@Preview
fun App(headerState: MutableState<Header>, sourceManager: SourceManager) {
    val parseStatus = sourceManager.parseStatusFlow.collectAsState(ParseStatus.NotStarted)
    // Flow would be better

    when (val status = parseStatus.value) {
        ParseStatus.NotStarted -> GettingStartedView()
        is ParseStatus.Proceeding -> Text("Proceeding.... ${status.percent}")
        is ParseStatus.Completed -> {
            Column {
                val logManager = status.logManager
                val refinedLogs by logManager.refinedLogs.collectAsState()
                val findStatus = logManager.keywordFindRequestFlow.collectAsState()
                val findResult = logManager.dectectionResultFocusFlowState.collectAsState()
                val refinedLogsList = refinedLogs.refined
                val logs = logManager.originalLogs
                ParseCompletedView(
                    logManager,
                    findStatus.value,
                    findResult.value,
                    refinedLogsList,
                    logs,
                    headerState,
                    status.parseResult,
                )
            }
        }
    }
}

@Composable
fun ParseCompletedView(
    logManager: LogManager,
    keywordFindRequest: KeywordFindRequest,
    detectionResultFocus: DetectionResultFocus?,
    refinedLogsList: List<Log>,
    logs: List<Log>,
    headerState: MutableState<Header>,
    parseResult: ParseResult
) {
    InvalidSentences(parseResult)
    Row {
        FilterView(logManager.filtersFlow.value, logManager::addFilter, logManager::removeFilter)
        Spacer(Modifier.width(16.dp))
        KeywordFindView(
            keywordFindRequest,
            detectionResultFocus,
            logManager::find,
            logManager::setKeywordFindEnabled,
            logManager::previousFindResult,
            logManager::nextFindResult
        )
    }
    val filteredSize =
        (if (refinedLogsList.size != logs.size) "Filtered size : ${refinedLogsList.size}, " else "")
    Text(filteredSize + "Total : ${logs.size}", modifier = Modifier.padding(8.dp))
    LogsView(headerState.value, refinedLogsList, detectionResultFocus)
}

@Composable
private fun GettingStartedView() {
    Column(
        modifier = Modifier.padding(50.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Getting Started", modifier = Modifier.align(Alignment.CenterHorizontally), style = MaterialTheme.typography.h3)
        Spacer(Modifier.height(8.dp))
        Text("1. File - Open file (Cmd + O), and choose the android log file", modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(8.dp))
        Text("2. Copy the android log, and paste (Cmd + V) to this window", modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun InvalidSentences(parseResult: ParseResult) {
    var showInvalidSentence by remember { mutableStateOf(true) }
    val invalidSentences = parseResult.invalidSentences
    if (showInvalidSentence) {
        Column(Modifier.fillMaxWidth().background(Color.LightGray).padding(8.dp)) {
            Row {
                IconButton(
                    onClick = { showInvalidSentence = false },
                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Close, "Close showing invalid sentences")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "There were ${invalidSentences.size} invalid sentences",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(Modifier.height(8.dp))
            invalidSentences.forEach { (index, s) ->
                Text("L#${index + 1} : $s")
            }
        }
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
    Divider()
}

@Composable
fun MyTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

fun main() = application {
    val sourceManager = SourceManager()
    val headerState = remember { mutableStateOf(Header.default) }
    Window(
        state = WindowState(width = 1600.dp, height = 800.dp),
        onCloseRequest = ::exitApplication,
        onPreviewKeyEvent = { keyEvent ->
            if (keyEvent.isMetaPressed && keyEvent.key == Key.V && keyEvent.type == KeyEventType.KeyUp) {
                Toolkit.getDefaultToolkit()
                    .systemClipboard
                    .getData(DataFlavor.stringFlavor)
                    .takeIf { it is String }
                    ?.let { sourceManager.changeSource(Source.Text(it.toString())) }
            }
            if (keyEvent.isMetaPressed && keyEvent.key == Key.F && keyEvent.type == KeyEventType.KeyUp) {
                sourceManager.findShortcutPressed()
            }
            false
        }
    ) {
        MyTheme {
            MenuBar {
                Menu("File") {
                    Item("Open file", shortcut = KeyShortcut(Key.O, meta = true)) {
                        sourceManager.openFileDialog()
                    }
                }
                Menu("Columns") {
                    headerState.value.asColumnList.forEach { columnInfo ->
                        columnCheckboxItem(columnInfo, headerState)
                    }
                }
            }
            App(headerState, sourceManager)
        }
    }
}

private fun SourceManager.openFileDialog() {
    val fileDialog = FileDialog(ComposeWindow())
    fileDialog.isVisible = true
    fileDialog.file?.let {
        val file = File(File(fileDialog.directory), it)
        changeSource(Source.File(file))
    }
}
