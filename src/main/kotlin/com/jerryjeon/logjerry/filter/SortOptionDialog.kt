package com.jerryjeon.logjerry.filter
// Dialog that changes the sort order

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState

@Composable
fun SortOptionDialog(
    tagFilterSortOption: Pair<FilterSortOption, SortOrder>,
    setTagFilterSortOption: (FilterSortOption, SortOrder) -> Unit,
    closeDialog: () -> Unit,
) {
    Dialog(
        title = "Sort by",
        state = DialogState(width = 300.dp, height = 300.dp),
        onCloseRequest = closeDialog,
    ) {
        var sortOption by remember { mutableStateOf(tagFilterSortOption.first) }
        var sortOrder by remember { mutableStateOf(tagFilterSortOption.second) }

        Surface(color = MaterialTheme.colors.surface, contentColor = MaterialTheme.colors.onSurface) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(text = "Sort by", modifier = Modifier, style = MaterialTheme.typography.body2)
                Row {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sortOption == FilterSortOption.Frequency,
                            onClick = { sortOption = FilterSortOption.Frequency }
                        )
                        Text(
                            text = "Frequency",
                            modifier = Modifier,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sortOption == FilterSortOption.Name,
                            onClick = { sortOption = FilterSortOption.Name }
                        )
                        Text(
                            text = "Name",
                            modifier = Modifier,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }

                Text(text = "Sort order", modifier = Modifier, style = MaterialTheme.typography.body2)
                Row {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sortOrder == SortOrder.Ascending,
                            onClick = { sortOrder = SortOrder.Ascending }
                        )
                        Text(
                            text = "Ascending",
                            modifier = Modifier,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = sortOrder == SortOrder.Descending,
                            onClick = { sortOrder = SortOrder.Descending }
                        )
                        Text(
                            text = "Descending",
                            modifier = Modifier,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        setTagFilterSortOption(sortOption, sortOrder)
                        closeDialog()
                    }) {
                        Text(
                            text = "Ok",
                            modifier = Modifier,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = closeDialog) {
                        Text(
                            text = "Cancel",
                            modifier = Modifier,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }
}
