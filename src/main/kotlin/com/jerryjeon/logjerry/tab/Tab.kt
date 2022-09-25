package com.jerryjeon.logjerry.tab

import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.source.SourceManager

class Tab(
    val name: String,
    val sourceManager: SourceManager,
) {
    companion object {
        fun gettingStarted(preferences: Preferences) = Tab("Getting Started", SourceManager(preferences))
    }
}
