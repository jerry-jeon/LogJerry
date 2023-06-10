package com.jerryjeon.logjerry.ui.popup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider

@Composable
fun BasePopup(
    showPopup: Boolean,
    anchor: Offset,
    dismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (showPopup) {
        Popup(
            onDismissRequest = dismiss,
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
                        anchor.x.toInt(),
                        (anchor.y + anchorBounds.height + additionalMargin).toInt()
                    )
                }
            }
        ) {
            Box(
                Modifier.border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colors.background)
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}
