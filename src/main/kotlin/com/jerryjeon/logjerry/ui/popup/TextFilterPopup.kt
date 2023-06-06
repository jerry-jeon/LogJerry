package com.jerryjeon.logjerry.ui.popup

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.jerryjeon.logjerry.filter.TextFilter
import com.jerryjeon.logjerry.ui.TextFilterView

@Composable
fun TextFilterPopup(
    showTextFilterPopup: Boolean,
    textFilterAnchor: Offset,
    dismissPopup: () -> Unit,
    addTextFilter: (TextFilter) -> Unit
) {
    if (showTextFilterPopup) {
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
                    val additionalMargin = 10
                    return IntOffset(
                        textFilterAnchor.x.toInt(),
                        (textFilterAnchor.y + anchorBounds.height + additionalMargin).toInt()
                    )
                }
            }
        ) {
            TextFilterView(addFilter = addTextFilter, dismiss = dismissPopup)
        }
    }
}
