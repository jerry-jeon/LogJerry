import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FindView(
    findStatus: FindStatus,
    findResult: List<DetectionResult>,
    find: (String) -> Unit,
    setFindEnabled: (Boolean) -> Unit
) {
    when (findStatus) {
        is FindStatus.TurnedOn -> {
            Row {
                TextField(
                    value = findStatus.keyword,
                    onValueChange = {
                        find(it)
                    }
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    setFindEnabled(false)
                }) {
                    Icon(Icons.Default.Close, "Close find")
                }

                Text("0 / ${findResult.size}")
            }
        }
        FindStatus.TurnedOff -> {}
    }
}
