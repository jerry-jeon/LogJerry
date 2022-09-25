@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.table

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut

enum class ColumnType(val text: String, val shortcut: KeyShortcut, val showDivider: Boolean) {
    Number("#", KeyShortcut(Key.One, meta = true), true),
    Date("Date", KeyShortcut(Key.Two, meta = true), true),
    Time("Time", KeyShortcut(Key.Three, meta = true), true),
    Pid("pid", KeyShortcut(Key.Four, meta = true), true),
    Tid("tid", KeyShortcut(Key.Five, meta = true), true),
    PackageName("PackageName", KeyShortcut(Key.Six, meta = true), true),
    Priority("Lev", KeyShortcut(Key.Seven, meta = true), true),
    Tag("Tag", KeyShortcut(Key.Eight, meta = true), true),
    Log("Log", KeyShortcut(Key.Nine, meta = true), false),
    Detection("", KeyShortcut(Key.Zero, meta = true), false);
}
