@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSerializationApi::class)

package com.jerryjeon.logjerry.preferences

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.jerryjeon.logjerry.log.Priority
import com.jerryjeon.logjerry.table.ColumnInfo
import com.jerryjeon.logjerry.table.Header
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream

@OptIn(ExperimentalSerializationApi::class)
class PreferencesViewModel {
    private val preferenceScope = CoroutineScope(Dispatchers.Default)

    // TODO not to read on the main thread
    val preferencesFlow = MutableStateFlow(
        try {
            Preferences.file.inputStream().use { json.decodeFromStream(it) }
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

    var saveEnabled = whiteValidColorsByPriority.map { map -> map.values.all { it != null } }
        .zip(darkValidColorsByPriority.map { map -> map.values.all { it != null } }) { b1, b2 -> b1 && b2 }
        .stateIn(preferenceScope, SharingStarted.Lazily, false)

    val showExceptionDetection = MutableStateFlow(preferencesFlow.value.showExceptionDetection)
    val showInvalidSentences = MutableStateFlow(preferencesFlow.value.showInvalidSentences)

    fun changeShowExceptionDetection(showExceptionDetection: Boolean) {
        this.showExceptionDetection.value = showExceptionDetection
    }

    fun changeShowInvalidSentences(showInvalidSentences: Boolean) {
        this.showInvalidSentences.value = showInvalidSentences
    }

    val jsonPreviewSizeString = MutableStateFlow(preferencesFlow.value.jsonPreviewSize.toString())
    val jsonPreviewSize = jsonPreviewSizeString.map { it.toIntOrNull() }
        .stateIn(preferenceScope, SharingStarted.Lazily, preferencesFlow.value.jsonPreviewSize)

    fun changeJsonPreviewSize(jsonPreviewSize: String) {
        this.jsonPreviewSizeString.value = jsonPreviewSize
    }

    val maximizeWindow = MutableStateFlow(preferencesFlow.value.windowSizeWhenOpened == null)
    fun changeMaximizeWindow(maximizeWindow: Boolean) {
        this.maximizeWindow.value = maximizeWindow
    }

    val widthWhenOpenedString = MutableStateFlow(preferencesFlow.value.windowSizeWhenOpened?.width?.toString() ?: "900")
    val widthWhenOpened = widthWhenOpenedString.map { it.toIntOrNull() }
        .stateIn(preferenceScope, SharingStarted.Lazily, preferencesFlow.value.windowSizeWhenOpened?.width)

    fun changeWidthWhenOpened(widthWhenOpened: String) {
        this.widthWhenOpenedString.value = widthWhenOpened
    }

    val heightWhenOpenedString = MutableStateFlow(preferencesFlow.value.windowSizeWhenOpened?.height?.toString() ?: "900")
    val heightWhenOpened = heightWhenOpenedString.map { it.toIntOrNull() }
        .stateIn(preferenceScope, SharingStarted.Lazily, preferencesFlow.value.windowSizeWhenOpened?.height)

    fun changeHeightWhenOpened(heightWhenOpened: String) {
        this.heightWhenOpenedString.value = heightWhenOpened
    }

    fun save() {
        val whiteSavingColors = whiteValidColorsByPriority.value
        val whiteSavingBackgroundColor = whiteBackgroundValidColor.value
        val darkSavingColors = darkValidColorsByPriority.value
        val darkSavingBackgroundColor = darkBackgroundValidColor.value
        if (whiteSavingColors.any { (_, color) -> color == null } ||
            whiteSavingBackgroundColor == null ||
            darkSavingColors.any { (_, color) -> color == null } ||
            darkSavingBackgroundColor == null
        ) {
            return
        }
        val jsonPreviewSizeValue = jsonPreviewSize.value ?: return

        val maximizeWindow = maximizeWindow.value
        val windowSizeWhenOpened = if (maximizeWindow) {
            null
        } else {
            val widthWhenOpened = widthWhenOpened.value ?: return
            val heightWhenOpened = heightWhenOpened.value ?: return
            WindowSize(widthWhenOpened, heightWhenOpened)
        }

        preferencesFlow.value = preferencesFlow.value.copy(
            colorTheme = colorThemeFlow.value,
            lightColorByPriority = whiteSavingColors.mapValues { (_, color) -> color!! },
            lightBackgroundColor = whiteSavingBackgroundColor,
            darkColorByPriority = darkSavingColors.mapValues { (_, color) -> color!! },
            darkBackgroundColor = darkSavingBackgroundColor,
            showExceptionDetection = showExceptionDetection.value,
            showInvalidSentences = showInvalidSentences.value,
            jsonPreviewSize = jsonPreviewSizeValue,
            windowSizeWhenOpened = windowSizeWhenOpened
        )

        println("HMMMM? ${preferencesFlow.value}")

        Preferences.file.outputStream().use {
            json.encodeToStream(preferencesFlow.value, it)
        }
    }

    fun restoreToDefault() {
        whiteColorStrings.value = Preferences.default.lightColorByPriority.toColorStrings()
        whiteBackgroundColorString.value = Preferences.default.lightBackgroundColor.toColorString()
        darkColorStrings.value = Preferences.default.darkColorByPriority.toColorStrings()
        darkBackgroundColorString.value = Preferences.default.darkBackgroundColor.toColorString()
        showExceptionDetection.value = Preferences.default.showExceptionDetection
        showInvalidSentences.value = Preferences.default.showInvalidSentences
        jsonPreviewSizeString.value = Preferences.default.jsonPreviewSize.toString()
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

    fun setColumnInfoVisibility(columnInfo: ColumnInfo, visible: Boolean) {
        preferencesFlow.value.headerFlow.update { it.copyOf(columnInfo.columnType, columnInfo.copy(visible = visible)) }
        preferenceScope.launch {
            Header.file.outputStream().use {
                json.encodeToStream(preferencesFlow.value.headerFlow.value, it)
            }
        }
    }

    companion object {
        val json = Json {
            ignoreUnknownKeys = true
        }
    }
}
