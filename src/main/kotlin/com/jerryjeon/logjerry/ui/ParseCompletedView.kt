@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.KeywordDetectionView
import com.jerryjeon.logjerry.filter.FilterManager
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.ParseCompleted
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.popup.PackageFilterPopup
import com.jerryjeon.logjerry.ui.popup.PriorityFilterPopup
import com.jerryjeon.logjerry.ui.popup.TagFilterPopup
import com.jerryjeon.logjerry.ui.popup.TextFilterPopup
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ParseCompletedView(
    preferences: Preferences,
    header: Header,
    parseCompleted: ParseCompleted,
    openNewTab: (StateFlow<List<Log>>) -> Unit,
) {
    val filterManager = parseCompleted.filterManager
    val detectorManager = parseCompleted.detectorManager
    val refineResult by parseCompleted.refineResultFlow.collectAsState()
    val optimizedHeader by parseCompleted.optimizedHeader.collectAsState()
    Column(
        modifier = Modifier
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            val statusByKey by refineResult.statusByKey.collectAsState()
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .height(IntrinsicSize.Min)
            ) {
                FilterView(filterManager)
                Spacer(Modifier.width(8.dp))
                statusByKey[DetectorKey.Json]?.let {
                    JsonDetectionView(
                        modifier = Modifier.fillMaxHeight(),
                        detectionStatus = it,
                        moveToPreviousOccurrence = refineResult::selectPreviousDetection,
                        moveToNextOccurrence = refineResult::selectNextDetection,
                    )
                }
                Spacer(Modifier.width(8.dp))
                statusByKey[DetectorKey.Exception]?.let {
                    ExceptionDetectionView(
                        modifier = Modifier.fillMaxHeight(),
                        detectionStatus = it,
                        moveToPreviousOccurrence = refineResult::selectPreviousDetection,
                        moveToNextOccurrence = refineResult::selectNextDetection,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val keywordDetectionRequest by detectorManager.keywordDetectionRequestFlow.collectAsState()
            KeywordDetectionView(
                keywordDetectionRequest = keywordDetectionRequest,
                detectionStatus = statusByKey[DetectorKey.Keyword],
                find = detectorManager::findKeyword,
                setFindEnabled = detectorManager::setKeywordDetectionEnabled,
                moveToPreviousOccurrence = refineResult::selectPreviousDetection,
                moveToNextOccurrence = refineResult::selectNextDetection,
            )
        }

        val textFilters by filterManager.textFiltersFlow.collectAsState()
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            textFilters.chunked(2).forEach {
                Row {
                    it.forEach { filter ->
                        AppliedTextFilter(filter, filterManager::removeTextFilter)
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        LogsView(
            refineResult = refineResult,
            parseCompleted = parseCompleted,
            preferences = preferences,
            detectorManager = detectorManager,
            header = optimizedHeader,
            hide = filterManager::hide,
            moveToPreviousMark = { refineResult.selectPreviousDetection(DetectorKey.Mark) },
            moveToNextMark = { refineResult.selectNextDetection(DetectorKey.Mark) }
        )
    }
}

@Composable
private fun FilterView(filterManager: FilterManager) {
    var showTextFilterPopup by remember { mutableStateOf(false) }
    var textFilterAnchor by remember { mutableStateOf(Offset.Zero) }

    var showLogLevelPopup by remember { mutableStateOf(false) }
    var logLevelAnchor by remember { mutableStateOf(Offset.Zero) }
    val priorityFilter by filterManager.priorityFilterFlow.collectAsState()

    var showPackageFilterPopup by remember { mutableStateOf(false) }
    var packageFilterAnchor by remember { mutableStateOf(Offset.Zero) }
    val packageFilters by filterManager.packageFiltersFlow.collectAsState()

    var showTagFilterPopup by remember { mutableStateOf(false) }
    var tagFilterAnchor by remember { mutableStateOf(Offset.Zero) }
    val tagFilters by filterManager.tagFiltersFlow.collectAsState()

    OutlinedButton(
        onClick = {
            showTextFilterPopup = true
        },
        modifier = Modifier
            .height(48.dp)
            .onGloballyPositioned { coordinates ->
                textFilterAnchor = coordinates.positionInRoot()
            },
    ) {
        Text("Add Filter")
    }
    Spacer(Modifier.width(8.dp))
    OutlinedButton(
        onClick = {
            showLogLevelPopup = true
        },
        modifier = Modifier
            .height(48.dp)
            .onGloballyPositioned { coordinates ->
                logLevelAnchor = coordinates.positionInRoot()
            },
    ) {
        Text("Log Level | ${priorityFilter.priority.name}")
    }
    Spacer(Modifier.width(8.dp))
    OutlinedButton(
        onClick = {
            showPackageFilterPopup = true
        },
        modifier = Modifier
            .height(48.dp)
            .onGloballyPositioned { coordinates ->
                packageFilterAnchor = coordinates.positionInRoot()
            },
    ) {
        Column {
            Row {
                Text("Packages")
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "(${packageFilters.filters.count { it.include }}/${packageFilters.filters.size})",
                )
            }
            packageFilters.filters.singleOrNull { it.include }?.let {
                Text(it.packageName ?: "?", style = MaterialTheme.typography.caption)
            }
        }
    }
    Spacer(Modifier.width(8.dp))
    OutlinedButton(
        onClick = {
            showTagFilterPopup = true
        },
        modifier = Modifier
            .height(48.dp)
            .onGloballyPositioned { coordinates ->
                tagFilterAnchor = coordinates.positionInRoot()
            },
    ) {
        Column {
            Row {
                Text("Tags")
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "(${tagFilters.filters.count { it.include }}/${tagFilters.filters.size})",
                )
            }
            tagFilters.filters.singleOrNull { it.include }?.let {
                Text(it.tag ?: "?", style = MaterialTheme.typography.caption)
            }
        }
    }

    TextFilterPopup(
        showTextFilterPopup = showTextFilterPopup,
        textFilterAnchor = textFilterAnchor,
        dismiss = { showTextFilterPopup = false },
        addTextFilter = filterManager::addTextFilter
    )
    PriorityFilterPopup(
        showPopup = showLogLevelPopup,
        anchor = logLevelAnchor,
        priorityFilter = priorityFilter,
        dismiss = { showLogLevelPopup = false },
        setPriorityFilter = filterManager::setPriorityFilter
    )
    PackageFilterPopup(
        showPackageFilterPopup = showPackageFilterPopup,
        packageFilterAnchor = packageFilterAnchor,
        dismiss = { showPackageFilterPopup = false },
        packageFilters = packageFilters,
        togglePackageFilter = filterManager::togglePackageFilter,
        includeAll = { filterManager.setAllPackageFilter(true) },
        excludeAll = { filterManager.setAllPackageFilter(false) },
    )
    TagFilterPopup(
        showTagFilterPopup = showTagFilterPopup,
        tagFilterAnchor = tagFilterAnchor,
        dismiss = { showTagFilterPopup = false },
        tagFilters = tagFilters,
        toggleTagFilter = filterManager::toggleTagFilter,
        includeAll = { filterManager.setAllTagFilter(true) },
        excludeAll = { filterManager.setAllTagFilter(false) },
    )
}
