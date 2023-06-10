package com.jerryjeon.logjerry.ui.popup

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.filter.PackageFilter
import com.jerryjeon.logjerry.filter.PackageFilters

@Composable
fun PackageFilterPopup(
    showPackageFilterPopup: Boolean,
    packageFilterAnchor: Offset,
    packageFilters: PackageFilters,
    dismiss: () -> Unit,
    togglePackageFilter: (PackageFilter) -> Unit
) {
    BasePopup(showPackageFilterPopup, packageFilterAnchor, dismiss) {
        Column {
            packageFilters.filters.forEachIndexed { index, packageFilter ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = packageFilter.include,
                        onCheckedChange = {
                            togglePackageFilter(packageFilter)
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "${packageFilter.packageName ?: "?"} (${packageFilter.frequency})",
                        modifier = Modifier
                            .padding(8.dp)
                    )
                }
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
