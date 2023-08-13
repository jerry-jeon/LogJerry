package com.jerryjeon.logjerry.ui.popup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.filter.*

@Composable
fun PackageFilterPopup(
    showPackageFilterPopup: Boolean,
    packageFilterAnchor: Offset,
    packageFilters: PackageFilters,
    packageFilterSortOption: Pair<FilterSortOption, SortOrder>,
    dismiss: () -> Unit,
    togglePackageFilter: (PackageFilter) -> Unit,
    includeAll: () -> Unit,
    excludeAll: () -> Unit,
    setPackageFilterSortOption: (FilterSortOption, SortOrder) -> Unit,
) {
    var showSortOptionDialog by remember { mutableStateOf(false) }
    if (showSortOptionDialog) {
        SortOptionDialog(packageFilterSortOption, setPackageFilterSortOption, closeDialog = { showSortOptionDialog = false })
    }

    BasePopup(showPackageFilterPopup, packageFilterAnchor, dismiss) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = includeAll) {
                    Text(
                        text = "Check All",
                        modifier = Modifier,
                        style = MaterialTheme.typography.body2
                    )
                }
                Spacer(Modifier.width(4.dp))
                OutlinedButton(onClick = excludeAll,) {
                    Text(
                        text = "Uncheck All",
                        modifier = Modifier,
                        style = MaterialTheme.typography.body2
                    )
                }
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = {
                    showSortOptionDialog = true
                }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                    )
                }
            }
            packageFilters.filters.forEachIndexed { index, packageFilter ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = packageFilter.include,
                        onCheckedChange = {
                            togglePackageFilter(packageFilter)
                        },
                    )
                    Text(
                        text = "${packageFilter.packageName ?: "?"} (${packageFilter.frequency})",
                        modifier = Modifier,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}
