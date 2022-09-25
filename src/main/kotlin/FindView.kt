import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FindView(
    findRequest: FindRequest,
    findResult: FindResult?,
    find: (String) -> Unit,
    setFindEnabled: (Boolean) -> Unit,
    moveToPreviousOccurrence: (FindResult) -> Unit,
    moveToNextOccurrence: (FindResult) -> Unit,
) {
    when (findRequest) {
        is FindRequest.TurnedOn -> {
            Row {
                TextField(
                    value = findRequest.keyword,
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

                findResult?.let {
                    Text("${it.focusingResult.detectionIndexInView}/ ${findResult.totalCount}", modifier = Modifier.align(Alignment.CenterVertically))
                    IconButton(onClick = {
                        moveToPreviousOccurrence(it)
                    }) {
                        Icon(Icons.Default.KeyboardArrowUp, "Previous Occurrence")
                    }
                    IconButton(onClick = {
                        moveToNextOccurrence(it)
                    }) {
                        Icon(Icons.Default.KeyboardArrowDown, "Next Occurrence")
                    }
                }
            }
        }
        FindRequest.TurnedOff -> {}
    }
}
