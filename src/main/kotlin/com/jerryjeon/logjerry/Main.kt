// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this com.jerryjeon.logjerry.source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalSerializationApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.jerryjeon.logjerry.detection.KeywordDetectionRequest
import com.jerryjeon.logjerry.detection.KeywordDetectionView
import com.jerryjeon.logjerry.log.LogManager
import com.jerryjeon.logjerry.log.refine.RefinedLog
import com.jerryjeon.logjerry.parse.ParseResult
import com.jerryjeon.logjerry.parse.ParseStatus
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.preferences.PreferencesView
import com.jerryjeon.logjerry.source.Source
import com.jerryjeon.logjerry.tab.Tab
import com.jerryjeon.logjerry.tab.TabManager
import com.jerryjeon.logjerry.tab.Tabs
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.ExceptionDetectionView
import com.jerryjeon.logjerry.ui.JsonDetectionView
import com.jerryjeon.logjerry.ui.PriorityFilterView
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.awt.FileDialog
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
@Preview
fun ActiveTabView(
    preferences: Preferences,
    headerState: MutableState<Header>,
    activeTab: Tab,
) {
    val parseStatus by activeTab.sourceManager.parseStatusFlow.collectAsState(ParseStatus.NotStarted)
    // Flow would be better
    when (val status = parseStatus) {
        is ParseStatus.NotStarted -> GettingStartedView(status, activeTab.sourceManager::changeSource)
        is ParseStatus.Proceeding -> Text("Proceeding.... ${status.percent}")
        is ParseStatus.Completed -> {
            Column {
                val logManager = status.logManager
                val refinedLogs by logManager.refineResult.collectAsState()
                val findStatus = logManager.keywordDetectionRequestFlow.collectAsState()
                val keywordDetectionResultFocus = logManager.keywordDetectionFocus.collectAsState()
                val exceptionDetectionResultFocus = logManager.exceptionDetectionFocus.collectAsState()
                val jsonDetectionResultFocus = logManager.jsonDetectionFocus.collectAsState()
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
                    preferences,
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
    detectionFocus: DetectionFocus?,
    keywordDetectionFocus: DetectionFocus?,
    exceptionDetectionFocus: DetectionFocus?,
    jsonDetectionFocus: DetectionFocus?,
    refinedLogsList: List<RefinedLog>,
    logs: List<Log>,
    preferences: Preferences,
    headerState: MutableState<Header>,
    parseResult: ParseResult
) {
    InvalidSentences(parseResult)
    Row(modifier = Modifier.padding(16.dp)) {
        TextFilterView(logManager.textFiltersFlow.value, logManager::addFilter, logManager::removeFilter)
        Spacer(Modifier.width(16.dp))
        PriorityFilterView(logManager.priorityFilter.value, logManager::setPriority)
        Spacer(Modifier.width(16.dp))
        Box(modifier = Modifier.weight(0.5f).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))) {
            Column {
                Text("Auto-detection", modifier = Modifier.padding(8.dp))
                Divider()
                Row {
                    ExceptionDetectionView(
                        Modifier.width(200.dp).wrapContentHeight(),
                        exceptionDetectionFocus,
                        { logManager.previousFindResult(DetectionKey.Exception, it) },
                        { logManager.nextFindResult(DetectionKey.Exception, it) },
                    )
                    Spacer(Modifier.width(8.dp))
                    Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                    Spacer(Modifier.width(8.dp))
                    JsonDetectionView(
                        Modifier.width(200.dp).wrapContentHeight(),
                        jsonDetectionFocus,
                        { logManager.previousFindResult(DetectionKey.Json, it) },
                        { logManager.nextFindResult(DetectionKey.Json, it) },
                    )
                    Spacer(Modifier.width(8.dp))
                    Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
    val filteredSize =
        (if (refinedLogsList.size != logs.size) "Filtered size : ${refinedLogsList.size}, " else "")
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(filteredSize + "Total : ${logs.size}", modifier = Modifier.padding(8.dp))
        KeywordDetectionView(
            Modifier.align(Alignment.BottomEnd),
            keywordDetectionRequest,
            keywordDetectionFocus,
            logManager::find,
            logManager::setKeywordDetectionEnabled,
            { logManager.previousFindResult(DetectionKey.Keyword, it) },
            { logManager.nextFindResult(DetectionKey.Keyword, it) },
        )
    }
    Divider(color = Color.Black)
    LogsView(preferences, headerState.value, refinedLogsList, detectionFocus)
}

@Composable
private fun GettingStartedView(notStarted: ParseStatus.NotStarted, changeSource: (Source) -> Unit) {
    val requester = remember { FocusRequester() }
    Column(
        modifier = Modifier.fillMaxSize().padding(50.dp)
            .onPreviewKeyEvent { keyEvent ->
                when {
                    keyEvent.isMetaPressed && keyEvent.key == Key.V && keyEvent.type == KeyEventType.KeyDown -> {
                        Toolkit.getDefaultToolkit()
                            .systemClipboard
                            .getData(DataFlavor.stringFlavor)
                            .takeIf { it is String }
                            ?.let { changeSource(Source.Text(it.toString())) }
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
            .focusRequester(requester)
            .focusable(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Getting Started",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.h3
        )
        Spacer(Modifier.height(24.dp))
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
    LaunchedEffect(notStarted) {
        requester.requestFocus()
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
            state = DialogState(width = 800.dp, height = 600.dp),
            onPreviewKeyEvent = { keyEvent ->
                if (keyEvent.isMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown) {
                    showInvalidSentence = false
                }
                false
            }
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
    val headerState = remember { mutableStateOf(Header.default) }
    val preferenceOpen = remember { mutableStateOf(false) }
    val preferences = try {
        Preferences.file.inputStream().use { Json.decodeFromStream(it) }
    } catch (e: Exception) {
        Preferences.default
    }
    val preferencesState = remember { mutableStateOf(preferences) }
    val tabManager = TabManager()
    val tabsState = tabManager.tabs.collectAsState()
    Window(
        title = "LogJerry",
        icon = painterResource("hamster.svg"),
        state = WindowState(width = Dp.Unspecified, height = Dp.Unspecified),
        onCloseRequest = ::exitApplication,
        onPreviewKeyEvent = { keyEvent ->
            if (keyEvent.isMetaPressed && keyEvent.key == Key.F && keyEvent.type == KeyEventType.KeyDown) {
                tabManager.findShortcutPressed()
                true
            } else {
                false
            }
        }
    ) {
        MyTheme {
            MenuBar {
                Menu("File") {
                    Item("New Tab", shortcut = KeyShortcut(Key.N, meta = true)) {
                        tabManager.newTab()
                    }
                    Item("Open file", shortcut = KeyShortcut(Key.O, meta = true)) {
                        openFileDialog {
                            tabManager.onNewFileSelected(it)
                        }
                    }
                    Item("Previous Tab", shortcut = KeyShortcut(Key.LeftBracket, meta = true, shift = true)) {
                        tabManager.moveToPreviousTab()
                    }
                    Item("Next Tab", shortcut = KeyShortcut(Key.RightBracket, meta = true, shift = true)) {
                        tabManager.moveToNextTab()
                    }
                    Item("Close Tab", shortcut = KeyShortcut(Key.W, meta = true)) {
                        tabManager.closeActiveTab()
                    }
                }
                Menu("Columns") {
                    headerState.value.asColumnList.forEach { columnInfo ->
                        columnCheckboxItem(columnInfo, headerState)
                    }
                }
                Menu("Preferences") {
                    Item("preferences.Preferences", shortcut = KeyShortcut(Key.Comma, meta = true)) {
                        preferenceOpen.value = true
                    }
                }
            }
            Column {
                TabView(tabsState.value, tabManager::activate, tabManager::close)
                Divider(modifier = Modifier.fillMaxWidth().height(1.dp))
                ActiveTabView(preferencesState.value, headerState, tabsState.value.active)
                PreferencesView(preferenceOpen, preferencesState)
            }
        }
    }
}

@Composable
private fun TabView(tabs: Tabs, activate: (Tab) -> Unit, close: (Tab) -> Unit) {
    val (tabList, activated) = tabs
    val scrollState = rememberScrollState()
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).horizontalScroll(scrollState)) {
        tabList.forEach { tab ->
            Row(
                modifier = Modifier
                    .background(if (tab === activated) Color.LightGray else Color.Transparent)
                    .clickable { activate(tab) }
                    .padding(8.dp)
            ) {
                Text(tab.name, modifier = Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.body2)
                Spacer(Modifier.width(8.dp))
                IconButton(modifier = Modifier.size(16.dp).align(Alignment.CenterVertically), onClick = { close(tab) }) {
                    Icon(Icons.Default.Close, "Close tab")
                }
            }
            Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
        }
    }
}

private fun openFileDialog(onFileSelected: (File) -> Unit) {
    val fileDialog = FileDialog(ComposeWindow())
    fileDialog.isVisible = true
    fileDialog.file?.let {
        onFileSelected(File(File(fileDialog.directory), it))
    }
}