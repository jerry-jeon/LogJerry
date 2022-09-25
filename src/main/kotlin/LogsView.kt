import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LogsView(header: Header, logs: List<Log>) {
    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    LazyColumn(Modifier.padding(10.dp)) {
        item { HeaderRow(header, divider) }
        item { HeaderDivider() }
        logs.forEach {
            item {
                Column {
                    LogRow(it, header, divider = divider)
                    Divider(color = Color(0xFFE5E7E9))
                }
            }
        }
    }
}
