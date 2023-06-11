@file:OptIn(ExperimentalUnitApi::class)

package com.jerryjeon.logjerry.preferences

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.log.Priority
import com.jerryjeon.logjerry.serialization.ColorAsLongSerializer
import com.jerryjeon.logjerry.serialization.TextUnitAsFloatSerializer
import com.jerryjeon.logjerry.table.Header
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.decodeFromStream
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Preferences(
    @Serializable(with = TextUnitAsFloatSerializer::class) val fontSize: TextUnit = 14.sp,
    val colorTheme: ColorTheme = ColorTheme.System,
    val lightColorByPriority: Map<Priority, @Serializable(with = ColorAsLongSerializer::class) Color> = mutableMapOf(
        Priority.Verbose to Color(0xFFBBBBBB),
        Priority.Debug to Color(0xFFAAB895),
        Priority.Info to Color(0xFF3EDE66),
        Priority.Warning to Color(0xFFFF6B68),
        Priority.Error to Color(0xFFFF6B68),
    ),
    val lightBackgroundColor: @Serializable(with = ColorAsLongSerializer::class) Color = Color.White,
    val darkColorByPriority: Map<Priority, @Serializable(with = ColorAsLongSerializer::class) Color> = mutableMapOf(
        Priority.Verbose to Color(0xFFBBBBBB),
        Priority.Debug to Color(0xFFAAB895),
        Priority.Info to Color(0xFF3EDE66),
        Priority.Warning to Color(0xFFFF6B68),
        Priority.Error to Color(0xFFFF6B68),
    ),
    val darkBackgroundColor: @Serializable(with = ColorAsLongSerializer::class) Color = Color(0XFF121212),
    val showExceptionDetection: Boolean = true,
    val showInvalidSentences: Boolean = true,
    val jsonPreviewSize: Int = 20,
    val windowSizeWhenOpened: WindowSize? = null, // (width, height), null to maximize
) {

    // TODO should be optimized
    @Composable
    fun colorByPriority(): Map<Priority, Color> {
        return when (colorTheme) {
            ColorTheme.Light -> lightColorByPriority
            ColorTheme.Dark -> darkColorByPriority
            ColorTheme.System -> {
                if (isSystemInDarkTheme()) {
                    darkColorByPriority
                } else {
                    lightColorByPriority
                }
            }
        }
    }

    companion object {
        val default = Preferences()
        val file = File(System.getProperty("java.io.tmpdir"), "LogJerryPreferences.json")
    }

    // These are not need to be here, but I don't want to create another class for this
    @Transient
    val headerFlow = MutableStateFlow(
        try {
            Header.file.inputStream().use { PreferencesViewModel.json.decodeFromStream(it) }
        } catch (e: Exception) {
            Header()
        }
    )
}

enum class ColorTheme {
    Light, Dark, System
}
