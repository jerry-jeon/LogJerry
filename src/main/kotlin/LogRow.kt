import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LogRow(
    log: Log
) {
    Row {
        Text(text = log.date, modifier = Modifier.wrapContentSize())
        Spacer(Modifier.padding(5.dp))
        Text(log.time)
        Spacer(Modifier.padding(5.dp))
        Text(log.pid.toString())
        Spacer(Modifier.padding(5.dp))
        Text(log.tid.toString())
        Spacer(Modifier.padding(5.dp))
        Text(log.packageName ?: "?")
        Spacer(Modifier.padding(5.dp))
        Text(log.priority)
        Spacer(Modifier.padding(5.dp))
        Text(log.tag)
        Spacer(Modifier.padding(5.dp))
        Text(log.log)
    }
}

@Preview
@Composable
fun LogRowPreview() {
    MyTheme {
        LogRow(SampleData.log)
    }
}
