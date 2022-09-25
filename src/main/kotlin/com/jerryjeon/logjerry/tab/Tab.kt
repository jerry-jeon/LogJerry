package com.jerryjeon.logjerry.tab

import com.jerryjeon.logjerry.source.SourceManager

class Tab(
    val name: String,
    val sourceManager: SourceManager
) {
    companion object {
        fun gettingStarted() = Tab("Getting Started", SourceManager())
    }
}
