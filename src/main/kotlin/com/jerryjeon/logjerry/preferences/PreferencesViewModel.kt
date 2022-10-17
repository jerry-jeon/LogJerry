@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSerializationApi::class)

package com.jerryjeon.logjerry.preferences

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jerryjeon.logjerry.log.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.zip
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

class PreferencesViewModel {
    private val preferenceScope = CoroutineScope(Dispatchers.Default)

    // TODO not to read on the main thread
    val preferencesFlow = MutableStateFlow(
        try {
            Preferences.file.inputStream().use { Json.decodeFromStream(it) }
        } catch (e: Exception) {
            Preferences.default
        }
    )

    val colorThemeFlow = MutableStateFlow(preferencesFlow.value.colorTheme)
    fun changeColorTheme(colorTheme: ColorTheme) {
        colorThemeFlow.value = colorTheme
    }

    //region white theme
    val whiteColorStrings = MutableStateFlow(preferencesFlow.value.lightColorByPriority.toColorStrings())
    val whiteValidColorsByPriority = whiteColorStrings.map {
        it.mapValues { (_, color) ->
            try {
                Color(parseColor(color))
            } catch (e: Exception) {
                null
            }
        }
    }
        .stateIn(preferenceScope, SharingStarted.Lazily, Priority.values().associateWith { Color.Black })
    val whiteBackgroundColorString = MutableStateFlow(preferencesFlow.value.lightBackgroundColor.toColorString())
    val whiteBackgroundValidColor = whiteBackgroundColorString.map {
        try {
            Color(parseColor(it))
        } catch (e: Exception) {
            null
        }
    }
        .stateIn(preferenceScope, SharingStarted.Lazily, Preferences.default.lightBackgroundColor)
    fun changeWhiteColorString(priority: Priority, colorString: String) {
        whiteColorStrings.value = whiteColorStrings.value + (priority to colorString)
    }
    fun changeWhiteBackgroundColor(colorString: String) {
        whiteBackgroundColorString.value = colorString
    }
    // endregion

    //region dark theme
    val darkColorStrings = MutableStateFlow(preferencesFlow.value.darkColorByPriority.toColorStrings())
    val darkValidColorsByPriority = darkColorStrings.map {
        it.mapValues { (_, color) ->
            try {
                Color(parseColor(color))
            } catch (e: Exception) {
                null
            }
        }
    }
        .stateIn(preferenceScope, SharingStarted.Lazily, Priority.values().associateWith { Color.Black })
    val darkBackgroundColorString = MutableStateFlow(preferencesFlow.value.darkBackgroundColor.toColorString())
    val darkBackgroundValidColor = darkBackgroundColorString.map {
        try {
            Color(parseColor(it))
        } catch (e: Exception) {
            null
        }
    }
        .stateIn(preferenceScope, SharingStarted.Lazily, Preferences.default.darkBackgroundColor)
    fun changeDarkColorString(priority: Priority, colorString: String) {
        darkColorStrings.value = darkColorStrings.value + (priority to colorString)
    }
    fun changeDarkBackgroundColor(colorString: String) {
        darkBackgroundColorString.value = colorString
    }
    //endregion

    val expandJsonWhenLoadFlow = MutableStateFlow(preferencesFlow.value.expandJsonWhenLoad)

    var saveEnabled = whiteValidColorsByPriority.map { map -> map.values.all { it != null } }
        .zip(darkValidColorsByPriority.map { map -> map.values.all { it != null } }) { b1, b2 -> b1 && b2 }
        .stateIn(preferenceScope, SharingStarted.Lazily, false)

    fun changeExpandJsonWhenLoad(expandJsonWhenLoad: Boolean) {
        expandJsonWhenLoadFlow.value = expandJsonWhenLoad
    }

    fun save() {
        val whiteSavingColors = whiteValidColorsByPriority.value
        val whiteSavingBackgroundColor = whiteBackgroundValidColor.value
        val darkSavingColors = darkValidColorsByPriority.value
        val darkSavingBackgroundColor = darkBackgroundValidColor.value
        if (whiteSavingColors.any { (_, color) -> color == null }
            || whiteSavingBackgroundColor == null
            || darkSavingColors.any { (_, color) -> color == null }
            || darkSavingBackgroundColor == null
        ) {
            return
        }
        preferencesFlow.value = preferencesFlow.value.copy(
            colorTheme = colorThemeFlow.value,
            lightColorByPriority = whiteSavingColors.mapValues { (_, color) -> color!! },
            lightBackgroundColor = whiteSavingBackgroundColor,
            darkColorByPriority = darkSavingColors.mapValues { (_, color) -> color!! },
            darkBackgroundColor = darkSavingBackgroundColor,
            expandJsonWhenLoad = expandJsonWhenLoadFlow.value
        )

        Preferences.file.outputStream().use {
            Json.encodeToStream(preferencesFlow.value, it)
        }
    }

    fun restoreToDefault() {
        whiteColorStrings.value = Preferences.default.lightColorByPriority.toColorStrings()
        whiteBackgroundColorString.value = Preferences.default.lightBackgroundColor.toColorString()
        darkColorStrings.value = Preferences.default.darkColorByPriority.toColorStrings()
        darkBackgroundColorString.value = Preferences.default.darkBackgroundColor.toColorString()
        expandJsonWhenLoadFlow.value = Preferences.default.expandJsonWhenLoad
    }

    private fun Color.toColorString() = String.format("#%x", this.toArgb())

    private fun Map<Priority, Color>.toColorStrings() = mapValues { (_, c) -> c.toColorString() }
    private fun parseColor(colorString: String): Int {
        if (colorString[0] == '#') { // Use a long to avoid rollovers on #ffXXXXXX
            var color = colorString.substring(1).toLong(16)
            if (colorString.length == 7) { // Set the alpha value
                color = color or -0x1000000
            } else require(colorString.length == 9) { "Unknown color" }
            return color.toInt()
        }
        throw IllegalArgumentException("Unknown color")
    }
}
