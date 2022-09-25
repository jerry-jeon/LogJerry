@file:OptIn(ExperimentalUnitApi::class)

package com.jerryjeon.logjerry.preferences

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.log.Priority
import com.jerryjeon.logjerry.serialization.ColorAsLongSerializer
import com.jerryjeon.logjerry.serialization.TextUnitAsFloatSerializer
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Preferences(
    @Serializable(with = TextUnitAsFloatSerializer::class) val fontSize: TextUnit,
    val colorByPriority: Map<Priority, @Serializable(with = ColorAsLongSerializer::class) Color>,
    val expandJsonWhenLoad: Boolean
) {
    companion object {
        val default = Preferences(
            14.sp,
            mutableMapOf(
                Priority.Verbose to Color(0xFFBBBBBB),
                Priority.Debug to Color(0xFFAAB895),
                Priority.Info to Color(0xFF3EDE66),
                Priority.Warning to Color(0xFFFF6B68),
                Priority.Error to Color(0xFFFF6B68),
            ),
            true
        )
        val file = File(System.getProperty("java.io.tmpdir"), "LogJerryPreferences.json")
    }
}
