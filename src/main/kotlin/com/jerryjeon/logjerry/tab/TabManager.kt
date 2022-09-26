package com.jerryjeon.logjerry.tab

import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.source.Source
import com.jerryjeon.logjerry.source.SourceManager
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class TabManager(
    private val preferences: Preferences,
    initialTab: Tab = Tab(name = "Getting Started", sourceManager = SourceManager(preferences))
) {

    val tabs = MutableStateFlow(Tabs(listOf(initialTab), initialTab))

    fun findShortcutPressed() {
        tabs.value.active.sourceManager.turnOnKeywordDetection()
    }

    fun onNewFileSelected(file: File) {
        val newActiveTab = Tab(file.name, SourceManager(preferences))
        if (file.extension.equals("zip", true)) {
            newActiveTab.sourceManager.changeSource(Source.ZipFile(file))
        } else {
            newActiveTab.sourceManager.changeSource(Source.File(file))
        }

        val (tabList, active) = tabs.value

        val newTabList = when (active.sourceManager.sourceFlow.value) {
            Source.None -> {
                // Remove getting started view
                (tabList - active) + newActiveTab
            }
            else -> {
                tabList + newActiveTab
            }
        }
        tabs.value = tabs.value.copy(
            tabList = newTabList,
            active = newActiveTab
        )
    }

    fun activate(tab: Tab) {
        tabs.value = tabs.value.copy(active = tab)
    }

    fun newTab() {
        val newActiveTab = Tab.gettingStarted(preferences)
        val (tabList, _) = tabs.value
        tabs.value = tabs.value.copy(
            tabList = tabList + newActiveTab,
            active = newActiveTab
        )
    }

    fun moveToPreviousTab() {
        val (tabList, activated) = tabs.value
        val index = tabList.indexOf(activated)
        val newActiveTab = if (index <= 0) {
            tabList[tabList.size - 1]
        } else {
            tabList[index - 1]
        }
        tabs.value = tabs.value.copy(active = newActiveTab)
    }

    fun moveToNextTab() {
        val (tabList, activated) = tabs.value
        val index = tabList.indexOf(activated)
        val newActiveTab = if (index >= tabList.size - 1) {
            tabList[0]
        } else {
            tabList[index + 1]
        }
        tabs.value = tabs.value.copy(active = newActiveTab)
    }
    fun closeActiveTab() {
        close(tabs.value.active)
    }

    fun close(tab: Tab) {
        val tabList = tabs.value.tabList
        val closingTabIndex = tabList.indexOf(tab)

        when (tab) {
            tabs.value.active -> {
                when {
                    tabList.size <= 1 -> {
                        val newActiveTab = Tab.gettingStarted(preferences)
                        tabs.value = Tabs(
                            tabList = listOf(newActiveTab),
                            active = newActiveTab
                        )
                    }
                    else -> {
                        val nexIndex = if(closingTabIndex <= 0) tabList.size - 1 else closingTabIndex - 1
                        val newActiveTab = tabList[nexIndex]
                        tabs.value = tabs.value.copy(
                            tabList = (tabList - tab),
                            active = newActiveTab
                        )
                    }
                }
            }
            else -> {
                tabs.value = tabs.value.copy(tabList = (tabList - tab))
            }
        }
    }
}
