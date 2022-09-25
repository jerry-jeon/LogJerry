package tab

import kotlinx.coroutines.flow.MutableStateFlow
import source.Source
import source.SourceManager
import java.io.File

class TabManager(initialTab: Tab = Tab(name = "Getting Started", sourceManager = SourceManager())) {

    val tabs = MutableStateFlow(Tabs(listOf(initialTab), initialTab))

    fun findShortcutPressed() {
        tabs.value.activated.sourceManager.turnOnKeywordDetection()
    }

    fun onNewFileSelected(file: File) {
        val newActiveTab = Tab(file.name, SourceManager())
        newActiveTab.sourceManager.changeSource(Source.File(file))

        val (tabList, activated) = tabs.value

        val newTabList = when (activated.sourceManager.sourceFlow.value) {
            Source.None -> {
                // Remove getting started view
                (tabList - activated) + newActiveTab
            }
            else -> {
                tabList + newActiveTab
            }
        }
        tabs.value = tabs.value.copy(
            tabList = newTabList,
            activated = newActiveTab
        )
    }

    fun activate(tab: Tab) {
        tabs.value = tabs.value.copy(activated = tab)
    }

    fun newTab() {
        val newActiveTab = Tab("Getting Started", SourceManager())
        val (tabList, _) = tabs.value
        tabs.value = tabs.value.copy(
            tabList = tabList + newActiveTab,
            activated = newActiveTab
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
        tabs.value = tabs.value.copy(activated = newActiveTab)
    }

    fun moveToNextTab() {
        val (tabList, activated) = tabs.value
        val index = tabList.indexOf(activated)
        val newActiveTab = if (index >= tabList.size - 1) {
            tabList[0]
        } else {
            tabList[index + 1]
        }
        tabs.value = tabs.value.copy(activated = newActiveTab)
    }
}
