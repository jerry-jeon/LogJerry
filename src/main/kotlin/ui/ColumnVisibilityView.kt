import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuScope
import table.ColumnInfo
import table.Header

@Composable
fun ColumnVisibility(headerState: MutableState<Header>) {
    val asColumnList = headerState.value.asColumnList

    Column {
        Text("Column visibility", style = MaterialTheme.typography.h4)
        asColumnList.chunked(3).forEach { chunked ->
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
    Row(modifier = Modifier.width(110.dp)) {
        Text(columnInfo.columnType.name, modifier = Modifier.weight(1f).align(Alignment.CenterVertically), textAlign = TextAlign.Center)
        Checkbox(columnInfo.visible, onCheckedChange = {
            header = header.copyOf(columnInfo.columnType, columnInfo.copy(visible = it))
        })
    }
}

@Composable
fun MenuScope.columnCheckboxItem(columnInfo: ColumnInfo, headerState: MutableState<Header>) {
    var header by headerState
    CheckboxItem(
        text = columnInfo.columnType.name,
        checked = columnInfo.visible,
        shortcut = columnInfo.columnType.shortcut
    ) {
        header = header.copyOf(columnInfo.columnType, columnInfo.copy(visible = it))
    }
}
