import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
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

// TODO Find cleaner way
fun RowScope.applyWidth(width: Int?, modifier: Modifier = Modifier): Modifier {
    return if (width == null) modifier.weight(1f) else modifier.width(width.dp)
}

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
            date = ColumnInfo(ColumnType.Date, 100, true),
            time = ColumnInfo(ColumnType.Time, 100, true),
            pid = ColumnInfo(ColumnType.PID, 40, true),
            tid = ColumnInfo(ColumnType.TID, 40, true),
            packageName = ColumnInfo(ColumnType.PackageName, 120, true),
            priority = ColumnInfo(ColumnType.Priority, 60, true),
            tag = ColumnInfo(ColumnType.Tag, 200, true),
            log = ColumnInfo(ColumnType.Log, null, true),
        )
    }
}

@Composable
fun RowScope.HeaderView(columnInfo: ColumnInfo, modifier: Modifier = Modifier) {
    if (columnInfo.visible) {
        Text(text = columnInfo.columnType.name, modifier = applyWidth(columnInfo.width, modifier))
    }
}

@Composable
fun HeaderRow(header: Header, divider: @Composable RowScope.() -> Unit) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        header.asColumnList.forEach {
            HeaderView(it)
            divider()
        }
    }
}

@Preview
@Composable
fun HeaderPreview() {
    MyTheme {
        HeaderRow(Header.default) { Divider() }
    }
}
