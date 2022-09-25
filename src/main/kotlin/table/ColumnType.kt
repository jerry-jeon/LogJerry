@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package table

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut

enum class ColumnType(val text: String, val shortcut: KeyShortcut, val icon: ImageVector?, val showDivider: Boolean) {
    Number("#", KeyShortcut(Key.Zero, meta = true), null, true),
    Date("Date", KeyShortcut(Key.One, meta = true), Icons.Default.DateRange, true),
    Time("Time", KeyShortcut(Key.Two, meta = true), Icons.Default.Check, true),
    Pid("pid", KeyShortcut(Key.Three, meta = true), null, true),
    Tid("tid", KeyShortcut(Key.Four, meta = true), null, true),
    PackageName("PackageName", KeyShortcut(Key.Five, meta = true), null, true),
    Priority("level", KeyShortcut(Key.Six, meta = true), null, true),
    Tag("Tag", KeyShortcut(Key.Seven, meta = true), Icons.Default.Info, true),
    Button("B", KeyShortcut(Key.Nine, meta = true), null, false),
    Log("Log", KeyShortcut(Key.Eight, meta = true), Icons.Default.List, false);
}
