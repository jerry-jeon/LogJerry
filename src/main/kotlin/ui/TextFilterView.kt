import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import log.refine.TextFilter
import table.ColumnType

@Composable
fun TextFilterView(
    textFilters: List<TextFilter>,
    addFilter: (TextFilter) -> Unit,
    removeFilter: (TextFilter) -> Unit
) {
    Box(Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)).padding(8.dp)) {
        Column {
            Text("Filter")
            CompositionLocalProvider(
                LocalTextStyle provides LocalTextStyle.current.copy(fontSize = 12.sp)
            ) {
                Spacer(Modifier.height(8.dp))
                AddTextFilterView(addFilter)

                Spacer(Modifier.height(8.dp))
                // FlowRow must be better, but it works strangely..
                Column {
                    textFilters.chunked(2).forEach {
                        Row {
                            it.forEach { filter ->
                                AppliedTextFilter(filter, removeFilter)
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTextFilterView(
    addFilter: (TextFilter) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val columnTypeState = remember { mutableStateOf(ColumnType.Log) }
    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
        TextField(
            modifier = Modifier.height(60.dp),
            value = text,
            onValueChange = {
                text = it
            },
            singleLine = true,
            leadingIcon = {
                SelectColumnTypeView(columnTypeState)
            },
            trailingIcon = {
                IconButton(onClick = {
                    addFilter(TextFilter(columnTypeState.value, text))
                    text = ""
                }) {
                    Icon(Icons.Default.Add, "Add a filter")
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun SelectColumnTypeView(
    columnTypeState: MutableState<ColumnType>
) {
    val allowedColumnTypes = listOf(ColumnType.Log, ColumnType.Tag, ColumnType.PackageName)
    var expanded by remember { mutableStateOf(false) }
    var columnType by columnTypeState
    Box(Modifier) {
        Row(
            modifier = Modifier.fillMaxHeight()
                .clickable(onClick = { expanded = true })
                .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Text(
                columnType.name,
                modifier = Modifier.wrapContentWidth().align(Alignment.CenterVertically),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ArrowDropDown, "Column types", modifier = Modifier.align(Alignment.CenterVertically))
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            allowedColumnTypes.forEach {
                DropdownMenuItem(onClick = {
                    columnType = it
                    expanded = false
                }) {
                    Text(text = it.name)
                }
            }
        }
    }
}

@Composable
private fun AppliedTextFilter(textFilter: TextFilter, removeFilter: (TextFilter) -> Unit) {
    Box(
        Modifier
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
    ) {
        Row(modifier = Modifier.height(30.dp)) {
            Spacer(Modifier.width(8.dp))
            Text(
                textFilter.columnType.text,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            Spacer(Modifier.width(8.dp))
            Divider(Modifier.width(1.dp).fillMaxHeight())
            Spacer(Modifier.width(8.dp))
            Text(textFilter.text, modifier = Modifier.align(Alignment.CenterVertically))
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .clickable { removeFilter(textFilter) }
                    .align(Alignment.CenterVertically)
                    .fillMaxHeight()
                    .aspectRatio(1f)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove a filter",
                    modifier = Modifier.size(ButtonDefaults.IconSize).align(Alignment.Center)
                )
            }
        }
    }
}
