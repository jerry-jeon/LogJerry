package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.Priority
import com.jerryjeon.logjerry.preferences.SortOrderPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class FilterManager(
    originalLogsFlow: StateFlow<List<Log>>,
    defaultSortOption: SortOrderPreferences = SortOrderPreferences.load(),
) {
    private val filterScope = CoroutineScope(Dispatchers.Default)

    val textFiltersFlow: MutableStateFlow<List<TextFilter>> = MutableStateFlow(emptyList())
    val priorityFilterFlow: MutableStateFlow<PriorityFilter> = MutableStateFlow(PriorityFilter(Priority.Verbose))
    val hiddenLogIndicesFlow: MutableStateFlow<HiddenFilter> = MutableStateFlow(HiddenFilter(emptySet()))
    val packageFiltersFlow = MutableStateFlow(PackageFilters(emptyList()))
    val tagFiltersFlow = MutableStateFlow(TagFilters(emptyList()))

    val packageFilterSortOptionFlow = MutableStateFlow(
        defaultSortOption.packageFilterSortOption to defaultSortOption.packageFilterSortOrder
    )
    val tagFilterSortOptionFlow = MutableStateFlow(
        defaultSortOption.tagFilterSortOption to defaultSortOption.tagFilterSortOrder
    )

    private val packageFilterComparator = packageFilterSortOptionFlow.map {
        val (option, order) = it
        when (option) {
            FilterSortOption.Frequency -> compareByDescending<PackageFilter> { it.frequency }
            FilterSortOption.Name -> compareByDescending { it.packageName }
        }.let { comparator ->
            if (order == SortOrder.Ascending) {
                comparator.reversed()
            } else {
                comparator
            }
        }
    }

    private val tagFilterComparator = tagFilterSortOptionFlow.map { it ->
        val (option, order) = it
        when (option) {
            FilterSortOption.Frequency -> compareByDescending<TagFilter> { it.frequency }
            FilterSortOption.Name -> compareByDescending { it.tag }
        }.let { comparator ->
            if (order == SortOrder.Ascending) {
                comparator.reversed()
            } else {
                comparator
            }
        }
    }

    val sortedPackageFiltersFlow = packageFiltersFlow.combine(packageFilterComparator) { packageFilters, comparator ->
        packageFilters.copy(filters = packageFilters.filters.sortedWith(comparator))
    }
        .stateIn(filterScope, SharingStarted.Eagerly, PackageFilters(emptyList()))
    val sortedTagFiltersFlow = tagFiltersFlow.combine(tagFilterComparator) { tagFilters, comparator ->
        tagFilters.copy(filters = tagFilters.filters.sortedWith(comparator))
    }
        .stateIn(filterScope, SharingStarted.Eagerly, TagFilters(emptyList()))

    val filtersFlow = combine(
        textFiltersFlow,
        priorityFilterFlow,
        hiddenLogIndicesFlow,
        packageFiltersFlow,
        tagFiltersFlow,
    ) { textFilters, priorityFilter, hiddenLogIndices, packageFilters, tagFilters ->
        textFilters + listOf(priorityFilter, hiddenLogIndices) + packageFilters + tagFilters
    }

    init {
        originalLogsFlow.onEach { originalLogs ->
            val packageFilters = originalLogs.groupingBy { it.packageName }.eachCount()
                .map { (packageName, frequency) ->
                    PackageFilter(packageName, frequency, true)
                }
                .let { PackageFilters(it) }
            packageFiltersFlow.value = packageFilters
        }
            .launchIn(filterScope)

        originalLogsFlow.onEach { originalLogs ->
            val tagFilters = originalLogs.groupingBy { it.tag }.eachCount()
                .map { (tag, frequency) ->
                    TagFilter(tag, frequency, true)
                }
                .let { TagFilters(it) }
            tagFiltersFlow.value = tagFilters
        }
            .launchIn(filterScope)
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

    fun hide(logIndex: Int) {
        hiddenLogIndicesFlow.update { it.copy(hiddenLogIndices = it.hiddenLogIndices + logIndex) }
    }

    fun unhide(logIndex: Int) {
        hiddenLogIndicesFlow.update { it.copy(hiddenLogIndices = it.hiddenLogIndices - logIndex) }
    }

    fun togglePackageFilter(packageFilter: PackageFilter) {
        packageFiltersFlow.update { packageFilters ->
            packageFilters.copy(
                filters = packageFilters.filters.map {
                    if (it.packageName == packageFilter.packageName) {
                        it.copy(include = !it.include)
                    } else {
                        it
                    }
                }
            )
        }
    }

    fun setAllPackageFilter(include: Boolean) {
        packageFiltersFlow.update { packageFilters ->
            packageFilters.copy(
                filters = packageFilters.filters.map {
                    it.copy(include = include)
                }
            )
        }
    }

    fun toggleTagFilter(tagFilter: TagFilter) {
        tagFiltersFlow.update { tagFilters ->
            tagFilters.copy(
                filters = tagFilters.filters.map {
                    if (it.tag == tagFilter.tag) {
                        it.copy(include = !it.include)
                    } else {
                        it
                    }
                }
            )
        }
    }

    fun setAllTagFilter(include: Boolean) {
        tagFiltersFlow.update { tagFilters ->
            tagFilters.copy(
                filters = tagFilters.filters.map {
                    it.copy(include = include)
                }
            )
        }
    }

    fun setPackageFilterSortOption(option: FilterSortOption, order: SortOrder) {
        packageFilterSortOptionFlow.value = option to order
        SortOrderPreferences.save(
            SortOrderPreferences(
                tagFilterSortOptionFlow.value.first,
                tagFilterSortOptionFlow.value.second,
                option,
                order,
            )
        )
    }

    fun setTagFilterSortOption(option: FilterSortOption, order: SortOrder) {
        tagFilterSortOptionFlow.value = option to order
        SortOrderPreferences.save(
            SortOrderPreferences(
                option,
                order,
                packageFilterSortOptionFlow.value.first,
                packageFilterSortOptionFlow.value.second,
            )
        )
    }
}
