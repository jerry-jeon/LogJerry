package com.jerryjeon.logjerry.transformation

import com.jerryjeon.logjerry.detection.ExceptionDetector
import com.jerryjeon.logjerry.detection.JsonDetector
import com.jerryjeon.logjerry.detection.KeywordDetectionRequest
import com.jerryjeon.logjerry.detection.KeywordDetector
import com.jerryjeon.logjerry.log.Priority
import com.jerryjeon.logjerry.log.refine.PriorityFilter
import com.jerryjeon.logjerry.log.refine.TextFilter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TransformationManager {
    private val transformationScope = MainScope()

    private val defaultDetectors = listOf(ExceptionDetector(), JsonDetector())

    private val keywordDetectionEnabledStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val detectingKeywordFlow = MutableStateFlow("")
    val keywordDetectionRequestFlow =
        combine(keywordDetectionEnabledStateFlow, detectingKeywordFlow) { enabled, keyword ->
            if (enabled) {
                KeywordDetectionRequest.TurnedOn(keyword)
            } else {
                KeywordDetectionRequest.TurnedOff
            }
        }.stateIn(transformationScope, SharingStarted.Lazily, KeywordDetectionRequest.TurnedOff)

    val textFiltersFlow: MutableStateFlow<List<TextFilter>> = MutableStateFlow(emptyList())
    val priorityFilterFlow: MutableStateFlow<PriorityFilter> = MutableStateFlow(PriorityFilter(Priority.Verbose))
    private val filtersFlow = combine(textFiltersFlow, priorityFilterFlow) { textFilters, priorityFilter ->
        textFilters + listOf(priorityFilter)
    }

    val transformerFlow = combine(filtersFlow, keywordDetectionRequestFlow) { filters, findStatus ->
        Transformation(
            filters,
            when (findStatus) {
                is KeywordDetectionRequest.TurnedOn -> defaultDetectors + listOf(KeywordDetector(findStatus.keyword))
                KeywordDetectionRequest.TurnedOff -> defaultDetectors
            }
        )
    }

    fun addTextFilter(textFilter: TextFilter) {
        textFiltersFlow.value = textFiltersFlow.value + textFilter
    }

    fun removeTextFilter(textFilter: TextFilter) {
        textFiltersFlow.value = textFiltersFlow.value - textFilter
    }

    fun setPriorityFilter(priorityFilter: PriorityFilter) {
        this.priorityFilterFlow.value = priorityFilter
    }

    fun findKeyword(keyword: String) {
        detectingKeywordFlow.value = keyword
    }

    fun setKeywordDetectionEnabled(enabled: Boolean) {
        keywordDetectionEnabledStateFlow.value = enabled
    }
}
