@file:OptIn(ExperimentalSerializationApi::class)

package preferences

import Priority
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream

class PreferencesViewModel(
    private val preferencesState: MutableState<Preferences>
) {
    private val preferenceScope = MainScope()
    var colorStrings = MutableStateFlow(
        preferencesState.value.colorByPriority.mapValues { (_, c) ->
            String.format("#%x", c.toArgb())
        }
    )

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
    var saveEnabled = validColorsByPriority.map { map -> map.values.all { it != null } }
        .stateIn(preferenceScope, SharingStarted.Lazily, false)

    fun changeColorString(priority: Priority, colorString: String) {
        colorStrings.value = colorStrings.value + (priority to colorString)
    }

    fun save() {
        val saving = validColorsByPriority.value
        if (saving.any { (_, color) -> color == null }) {
            return
        }
        preferencesState.value = preferencesState.value.copy(
            colorByPriority = saving.mapValues { (_, color) -> color!! }
        )

        Preferences.file.outputStream().use {
            Json.encodeToStream(preferencesState.value, it)
        }
    }

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
