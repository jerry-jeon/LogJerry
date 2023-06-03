package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.filter.TextFilter

@Composable
fun AppliedTextFilter(textFilter: TextFilter, removeFilter: (TextFilter) -> Unit) {
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
