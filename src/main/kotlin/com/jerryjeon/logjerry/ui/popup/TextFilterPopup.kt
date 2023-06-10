package com.jerryjeon.logjerry.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.jerryjeon.logjerry.filter.TextFilter
import com.jerryjeon.logjerry.ui.TextFilterView

@Composable
fun TextFilterPopup(
    showTextFilterPopup: Boolean,
    textFilterAnchor: Offset,
    dismiss: () -> Unit,
    addTextFilter: (TextFilter) -> Unit
) {
    BasePopup(showTextFilterPopup, textFilterAnchor, dismiss) {
        TextFilterView(addFilter = addTextFilter, dismiss = dismiss)
    }
}
