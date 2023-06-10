package com.jerryjeon.logjerry.ui.popup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.filter.TagFilter
import com.jerryjeon.logjerry.filter.TagFilters

@Composable
fun TagFilterPopup(
    showTagFilterPopup: Boolean,
    tagFilterAnchor: Offset,
    tagFilters: TagFilters,
    dismiss: () -> Unit,
    toggleTagFilter: (TagFilter) -> Unit
) {
    val scrollState = rememberScrollState()
    BasePopup(showTagFilterPopup, tagFilterAnchor, dismiss) {
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            tagFilters.filters.forEachIndexed { index, tagFilter ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = tagFilter.include,
                        onCheckedChange = {
                            toggleTagFilter(tagFilter)
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        text = "${tagFilter.tag ?: "?"} (${tagFilter.frequency})",
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