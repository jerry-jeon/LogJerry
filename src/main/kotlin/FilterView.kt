import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FilterView(
    filters: List<Filter>,
    addFilter: (Filter) -> Unit,
    removeFilter: (Filter) -> Unit
) {
    // Provide only useful column types
    var columnType by remember { mutableStateOf(ColumnType.Log) }
    val items = listOf(ColumnType.Log, ColumnType.PackageName)
    var text by remember { mutableStateOf("Text") }
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(8.dp)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(1.dp))
                    .align(Alignment.CenterVertically)
            ) {
                Row(modifier = Modifier.clickable(onClick = { expanded = true }).padding(8.dp)) {
                    Text(
                        columnType.name,
                        modifier = Modifier.width(80.dp).align(Alignment.CenterVertically),
                    )
                    Icon(Icons.Default.ArrowDropDown, "Column types")
                }
                DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    items.forEach {
                        DropdownMenuItem(onClick = {
                            columnType = it
                            expanded = false
                        }) {
                            Text(text = it.name)
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            TextField(
                value = text,
                onValueChange = {
                    text = it
                }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                addFilter(Filter(columnType, text))
                text = ""
            }) {
                Text("Add filter")
            }
        }

        Row {
            filters.forEach { filter ->

                Box(
                    Modifier
                        .height(50.dp)
                        .background(Color.LightGray, RoundedCornerShape(25.dp))
                ) {
                    Row(modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp)) {
                        if (filter.columnType.icon != null) {
                            Icon(
                                filter.columnType.icon,
                                contentDescription = "Remove a filter",
                                modifier = Modifier.size(ButtonDefaults.IconSize).align(Alignment.CenterVertically)
                            )
                        } else {
                            Text(
                                "${filter.columnType}",
                                modifier = Modifier.align(Alignment.CenterVertically),
                                style = TextStyle.Default.copy(fontSize = 12.sp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(filter.text, modifier = Modifier.align(Alignment.CenterVertically))
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .background(Color.Gray, CircleShape)
                                .clickable { removeFilter(filter) }
                                .padding(4.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove a filter",
                                modifier = Modifier.size(ButtonDefaults.IconSize).align(Alignment.Center)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}
