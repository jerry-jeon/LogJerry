package com.jerryjeon.logjerry.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.jerryjeon.logjerry.filter.PriorityFilter
import com.jerryjeon.logjerry.ui.PriorityFilterView

@Composable
fun PriorityFilterPopup(
    priorityFilter: PriorityFilter,
    anchor: Offset,
    showPopup: Boolean,
    dismissPopup: () -> Unit,
    setPriorityFilter: (PriorityFilter) -> Unit
) {
    if (showPopup) {
        Popup(
            onDismissRequest = dismissPopup,
            focusable = true,
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    return IntOffset(anchor.x.toInt(), (anchor.y + anchorBounds.height + 10).toInt())
                }
            }
        ) {
            PriorityFilterView(priorityFilter, setPriorityFilter)
        }
    }
}
