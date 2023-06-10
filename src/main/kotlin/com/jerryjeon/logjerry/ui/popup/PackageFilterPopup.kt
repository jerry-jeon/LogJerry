package com.jerryjeon.logjerry.ui.popup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
