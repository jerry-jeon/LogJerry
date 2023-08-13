@file:OptIn(ExperimentalSerializationApi::class)

package com.jerryjeon.logjerry.preferences

import com.jerryjeon.logjerry.filter.FilterSortOption
import com.jerryjeon.logjerry.filter.SortOrder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.decodeFromStream
import okio.use
import java.io.File
import java.io.FileInputStream

@Serializable
data class SortOrderPreferences(
    val tagFilterSortOption: FilterSortOption = FilterSortOption.Frequency,
    val tagFilterSortOrder: SortOrder = SortOrder.Descending,
    val packageFilterSortOption: FilterSortOption = FilterSortOption.Frequency,
    val packageFilterSortOrder: SortOrder = SortOrder.Descending,
) {

    companion object {
        val default = SortOrderPreferences()
        val file = File(System.getProperty("java.io.tmpdir"), "LogJerrySortOrderPreferences.json")

        fun load(): SortOrderPreferences {
            return if (file.exists()) {
                val use = file.inputStream().use<FileInputStream, SortOrderPreferences> { PreferencesViewModel.json.decodeFromStream(it) }
                    .also { println("Loaded SortOrderPreferences: $it") }
                use
            } else {
                default
            }
        }
        fun save(preferences: SortOrderPreferences) = file.writeText(PreferencesViewModel.json.encodeToString(serializer(), preferences))
    }

}