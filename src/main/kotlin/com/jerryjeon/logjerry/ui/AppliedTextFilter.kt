package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.filter.TextFilter
import com.jerryjeon.logjerry.filter.TextFilterType

@Composable
fun AppliedTextFilter(textFilter: TextFilter, removeFilter: (TextFilter) -> Unit) {
    val borderColor = when (textFilter.textFilterType) {
        TextFilterType.Include -> MaterialTheme.colors.secondary
        else -> MaterialTheme.colors.error
    }
    Row(
        modifier = Modifier.height(IntrinsicSize.Min)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))
        Text(
            textFilter.columnType.text,
            modifier = Modifier.padding(vertical = 8.dp),
            style = MaterialTheme.typography.caption
        )
        Spacer(Modifier.width(8.dp))
        Divider(Modifier.width(1.dp).fillMaxHeight())
        Spacer(Modifier.width(8.dp))
        Text(textFilter.text, modifier = Modifier, style = MaterialTheme.typography.caption)
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = { removeFilter(textFilter) },
            modifier = Modifier.size(18.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove a filter",
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
