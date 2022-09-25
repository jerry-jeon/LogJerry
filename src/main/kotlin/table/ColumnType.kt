@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package table

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut

enum class ColumnType(val shortcut: KeyShortcut, val icon: ImageVector?) {
    Number(KeyShortcut(Key.Zero, meta = true), Icons.Default.AccountBox),
    Date(KeyShortcut(Key.One, meta = true), Icons.Default.DateRange),
    Time(KeyShortcut(Key.Two, meta = true), Icons.Default.Check),
    PID(KeyShortcut(Key.Three, meta = true), null),
    TID(KeyShortcut(Key.Four, meta = true), null),
    PackageName(KeyShortcut(Key.Five, meta = true), null),
    Priority(KeyShortcut(Key.Six, meta = true), null),
    Tag(KeyShortcut(Key.Seven, meta = true), Icons.Default.Info),
    Log(KeyShortcut(Key.Eight, meta = true), Icons.Default.List),
    B(KeyShortcut(Key.Nine, meta = true), null); // button
}
