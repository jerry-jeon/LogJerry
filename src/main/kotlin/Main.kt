// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import detection.KeywordDetectionRequest
import detection.KeywordDetectionView
import log.LogManager
import log.refine.RefinedLog
import parse.ParseResult
import parse.ParseStatus
import source.Source
import source.SourceManager
import table.Header
import ui.ExceptionDetectionView
import ui.JsonDetectionView
import ui.PriorityFilterView
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
                val refinedLogs by logManager.refineResult.collectAsState()
                val findStatus = logManager.keywordDetectionRequestFlow.collectAsState()
                val keywordDetectionResultFocus = logManager.keywordDetectionResultFocus.collectAsState()
                val exceptionDetectionResultFocus = logManager.exceptionDetectionResultFocus.collectAsState()
                val jsonDetectionResultFocus = logManager.jsonDetectionResultFocus.collectAsState()
                val findResult = logManager.activeDetectionResultFocusFlowState.collectAsState()
                val refinedLogsList = refinedLogs.refined
                val logs = logManager.originalLogs
                ParseCompletedView(
                    logManager,
                    findStatus.value,
                    findResult.value,
                    keywordDetectionResultFocus.value,
                    exceptionDetectionResultFocus.value,
                    jsonDetectionResultFocus.value,
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
    keywordDetectionRequest: KeywordDetectionRequest,
    detectionResultFocus: DetectionResultFocus?,
    keywordDetectionResultFocus: DetectionResultFocus?,
    exceptionDetectionResultFocus: DetectionResultFocus?,
    jsonDetectionResultFocus: DetectionResultFocus?,
    refinedLogsList: List<RefinedLog>,
    logs: List<Log>,
    headerState: MutableState<Header>,
    parseResult: ParseResult
) {
    InvalidSentences(parseResult)
    Row {
        TextFilterView(logManager.textFiltersFlow.value, logManager::addFilter, logManager::removeFilter)
        PriorityFilterView(logManager.priorityFilter.value, logManager::setPriority)
        Spacer(Modifier.width(16.dp))
        KeywordDetectionView(
            keywordDetectionRequest,
            keywordDetectionResultFocus,
            logManager::find,
            logManager::setKeywordDetectionEnabled,
            { logManager.previousFindResult(DetectionKey.Keyword, it) },
            { logManager.nextFindResult(DetectionKey.Keyword, it) },
        )
        ExceptionDetectionView(
            exceptionDetectionResultFocus,
            { logManager.previousFindResult(DetectionKey.Exception, it) },
            { logManager.nextFindResult(DetectionKey.Exception, it) },
        )
        JsonDetectionView(
            jsonDetectionResultFocus,
            { logManager.previousFindResult(DetectionKey.Json, it) },
            { logManager.nextFindResult(DetectionKey.Json, it) },
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
        Text(
            "Getting Started",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.h3
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "1. File - Open file (Cmd + O), and choose the android log file",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "2. Copy the android log, and paste (Cmd + V) to this window",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun InvalidSentences(parseResult: ParseResult) {
    val invalidSentences = parseResult.invalidSentences
    var showInvalidSentence by remember { mutableStateOf(invalidSentences.isNotEmpty()) }
    if (showInvalidSentence) {
        Dialog(
            onCloseRequest = { showInvalidSentence = false },
            title = "Invalid sentences",
            state = DialogState(width = 800.dp, height = 600.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "There were ${invalidSentences.size} invalid sentences",
                    color = Color.Red,
                )
                Text(
                    "Supported format : \"date time pid-tid/packageName priority/tag: log\"",
                    color = Color.Black,
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
                        Text(s, color = Color.Red)
                    }
                }
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
