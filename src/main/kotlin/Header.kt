import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ColumnType {
    Date, Time, PID, TID, PackageName, Priority, Tag, Log;
}

data class ColumnInfo(
    val columnType: ColumnType,
    val width: Int?, // null means new weight 1f
    val visible: Boolean
)

data class Header(
    val date: ColumnInfo,
    val time: ColumnInfo,
    val pid: ColumnInfo,
    val tid: ColumnInfo,
    val packageName: ColumnInfo,
    val priority: ColumnInfo,
    val tag: ColumnInfo,
    val log: ColumnInfo,
) {

    val asColumnList: List<ColumnInfo> = listOf(date, time, pid, tid, packageName, priority, tag, log)

    companion object {
        val default = Header(
            date = ColumnInfo(ColumnType.Date, 60, true),
            time = ColumnInfo(ColumnType.Date, 60, true),
            pid = ColumnInfo(ColumnType.Date, 60, true),
            tid = ColumnInfo(ColumnType.Date, 60, true),
            packageName = ColumnInfo(ColumnType.Date, 60, true),
            priority = ColumnInfo(ColumnType.Date, 60, true),
            tag = ColumnInfo(ColumnType.Date, 60, true),
            log = ColumnInfo(ColumnType.Date, 60, true),
        )
    }
}

@Composable
fun RowScope.HeaderView(columnInfo: ColumnInfo, modifier: Modifier = Modifier) {
    if (columnInfo.visible) {
        val newModifier: Modifier =
            if (columnInfo.width == null) modifier.weight(1f) else modifier.width(columnInfo.width.dp)
        Text(text = columnInfo.columnType.name, modifier = newModifier)
    }
}

@Composable
fun HeaderRow(header: Header) {
    Row {
        header.asColumnList.forEach {
            HeaderView(it)
            Spacer(Modifier.padding(5.dp))
        }
    }
}

@Preview
@Composable
fun HeaderPreview() {
    MyTheme {
        HeaderRow(Header.default)
    }
}
