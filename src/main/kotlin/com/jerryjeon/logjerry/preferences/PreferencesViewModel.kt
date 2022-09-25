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

    var colorStrings = MutableStateFlow(preferencesFlow.value.colorByPriority.toColorStrings())
    var validColorsByPriority = colorStrings.map {
        it.mapValues { (_, color) ->
            try {
                Color(parseColor(color))
            } catch (e: Exception) {
                null
            }
        }
    }
        .stateIn(preferenceScope, SharingStarted.Lazily, Priority.values().associateWith { Color.Black })

    val expandJsonWhenLoadFlow = MutableStateFlow(preferencesFlow.value.expandJsonWhenLoad)

    var saveEnabled = validColorsByPriority.map { map -> map.values.all { it != null } }
        .stateIn(preferenceScope, SharingStarted.Lazily, false)

    fun changeColorString(priority: Priority, colorString: String) {
        colorStrings.value = colorStrings.value + (priority to colorString)
    }
    
    fun changeExpandJsonWhenLoad(expandJsonWhenLoad: Boolean) {
        expandJsonWhenLoadFlow.value = expandJsonWhenLoad
    }

    fun save() {
        val saving = validColorsByPriority.value
        if (saving.any { (_, color) -> color == null }) {
            return
        }
        preferencesFlow.value = preferencesFlow.value.copy(
            colorByPriority = saving.mapValues { (_, color) -> color!! },
            expandJsonWhenLoad = expandJsonWhenLoadFlow.value
        )

        Preferences.file.outputStream().use {
            Json.encodeToStream(preferencesFlow.value, it)
        }
    }

    fun restoreToDefault() {
        colorStrings.value = Preferences.default.colorByPriority.toColorStrings()
        expandJsonWhenLoadFlow.value = Preferences.default.expandJsonWhenLoad
    }

    private fun Map<Priority, Color>.toColorStrings() = mapValues { (_, c) -> String.format("#%x", c.toArgb()) }
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
