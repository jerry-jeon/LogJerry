// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this com.jerryjeon.logjerry.source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalSerializationApi::class)

package com.jerryjeon.logjerry

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.parse.ParseStatus
import com.jerryjeon.logjerry.preferences.ColorTheme
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.preferences.PreferencesView
import com.jerryjeon.logjerry.preferences.PreferencesViewModel
import com.jerryjeon.logjerry.source.Source
import com.jerryjeon.logjerry.tab.Tab
import com.jerryjeon.logjerry.tab.TabManager
import com.jerryjeon.logjerry.tab.Tabs
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.InvalidSentencesDialog
import com.jerryjeon.logjerry.ui.ParseCompletedView
import com.jerryjeon.logjerry.ui.ShortcutDialog
import com.jerryjeon.logjerry.ui.columnCheckboxItem
import com.jerryjeon.logjerry.util.KeyShortcuts
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import java.awt.FileDialog
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
@Preview
fun ActiveTabView(
    preferences: Preferences,
    header: Header,
    activeTab: Tab,
    openNewTab: (StateFlow<List<Log>>) -> Unit,
) {
    val parseStatus by activeTab.sourceManager.parseStatusFlow.collectAsState(ParseStatus.NotStarted)
    // Flow would be better
    when (val status = parseStatus) {
        is ParseStatus.NotStarted -> GettingStartedView(status, activeTab.sourceManager::changeSource)
        is ParseStatus.Proceeding -> Text("Proceeding.... ${status.percent}")
        is ParseStatus.Completed -> {
            if (status.parseResult.logs.isNotEmpty()) {
                Column {
                    if (preferences.showInvalidSentences) {
                        InvalidSentencesDialog(status.parseResult.invalidSentences)
                    }
                    ParseCompletedView(
                        preferences,
                        header,
                        status.parseCompleted,
                        openNewTab
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        """
                          It's not possible to parse the log. 
                          Currently LogJerry only support the log format of the Android Studio  
                        """.trimIndent(),
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier.fillMaxWidth().align(Alignment.Center),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun GettingStartedView(notStarted: ParseStatus.NotStarted, changeSource: (Source) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier.fillMaxSize().padding(50.dp)
            .onPreviewKeyEvent { keyEvent ->
                when {
                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.V && keyEvent.type == KeyEventType.KeyDown -> {
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
            .focusRequester(focusRequester)
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
            "OR",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "2. Copy the android log, and paste (Cmd + V) to this window",
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
    LaunchedEffect(notStarted) {
        focusRequester.requestFocus()
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
fun MyTheme(
    preferences: Preferences,
    content: @Composable () -> Unit
) {
    when (preferences.colorTheme) {
        ColorTheme.Light -> LightTheme(preferences, content)
        ColorTheme.Dark -> DarkTheme(preferences, content)
        ColorTheme.System -> {
            if (isSystemInDarkTheme()) {
                DarkTheme(preferences, content)
            } else {
                LightTheme(preferences, content)
            }
        }
    }
}

@Composable
fun DarkTheme(preferences: Preferences, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = darkColors(
            primary = Color(0xFFCE93D8),
            secondary = Color(0xFF81C784),
            background = preferences.darkBackgroundColor,
            surface = preferences.darkBackgroundColor
        ),
        content = {
            CompositionLocalProvider(
                LocalScrollbarStyle provides LocalScrollbarStyle.current.copy(
                    hoverColor = Color(0xFF999999),
                    unhoverColor = Color(0xFF666666)
                )
            ) {
                content()
            }
        }
    )
}

@Composable
fun LightTheme(preferences: Preferences, content: @Composable () -> Unit) {
    MaterialTheme(
        colors = lightColors(
            primary = Color(0xFFCE93D8),
            secondary = Color(0xFF81C784),
            background = preferences.lightBackgroundColor,
            surface = preferences.lightBackgroundColor
        ),
        content = content
    )
}

fun main() = application {
    val preferencesViewModel = PreferencesViewModel()
    val preferences by preferencesViewModel.preferencesFlow.collectAsState()
    val header by preferences.headerFlow.collectAsState()
    val preferenceOpen = remember { mutableStateOf(false) }
    val shortcutDialogOpened = remember { mutableStateOf(false) }
    val tabManager = TabManager(preferences)
    val tabsState = tabManager.tabs.collectAsState()
    Window(
        title = "LogJerry",
        icon = painterResource("LogJerry.png"),
        state = WindowState(width = Dp.Unspecified, height = Dp.Unspecified),
        onCloseRequest = ::exitApplication,
        onPreviewKeyEvent = { keyEvent ->
            if (keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.F && keyEvent.type == KeyEventType.KeyDown) {
                tabManager.findShortcutPressed()
                true
            } else {
                false
            }
        }
    ) {
        MyTheme(preferences = preferences) {
            MenuBar {
                Menu("File") {
                    Item("New Tab", shortcut = KeyShortcuts.newTab) {
                        tabManager.newTab()
                    }
                    Item("Open file", shortcut = KeyShortcuts.openFile) {
                        openFileDialog {
                            tabManager.onNewFileSelected(it)
                        }
                    }
                    Item("Previous Tab", shortcut = KeyShortcuts.previousTab) {
                        tabManager.moveToPreviousTab()
                    }
                    Item("Next Tab", shortcut = KeyShortcuts.nextTab) {
                        tabManager.moveToNextTab()
                    }
                    Item("Close Tab", shortcut = KeyShortcuts.closeTab) {
                        tabManager.closeActiveTab()
                    }
                }
                Menu("Columns") {
                    header.asColumnList.forEach { columnInfo ->
                        columnCheckboxItem(columnInfo, preferencesViewModel::setColumnInfoVisibility)
                    }
                }
                Menu("Preferences") {
                    Item("preferences.Preferences", shortcut = KeyShortcuts.preferences) {
                        preferenceOpen.value = true
                    }
                }
                Menu("Help") {
                    Item("Shortcuts") {
                        shortcutDialogOpened.value = true
                    }
                }
            }
            Surface(
                color = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.onSurface
            ) {
                Column {
                    TabView(tabsState.value, tabManager::activate, tabManager::close)
                    Divider(modifier = Modifier.fillMaxWidth().height(1.dp))
                    ActiveTabView(
                        preferences,
                        header,
                        tabsState.value.active,
                        openNewTab = { logsFlow: StateFlow<List<Log>> ->
                            tabManager.newTab("Marked rows", Source.LogsFlow(logsFlow))
                        }
                    )
                    PreferencesView(preferenceOpen, preferencesViewModel)
                    ShortcutDialog(shortcutDialogOpened)
                }
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
            Column(modifier = Modifier.width(IntrinsicSize.Max).clickable { activate(tab) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                ) {
                    Text(
                        tab.name,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.body2,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        modifier = Modifier.size(16.dp).align(Alignment.CenterVertically),
                        onClick = { close(tab) }
                    ) {
                        Icon(Icons.Default.Close, "Close tab")
                    }
                }
                if (tab === activated) {
                    Box(modifier = Modifier.fillMaxWidth().height(7.dp))
                    Divider(modifier = Modifier.fillMaxWidth().height(5.dp), color = MaterialTheme.colors.primary)
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(12.dp))
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
