package com.example.pedalboard.filtering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "FilterListViewModel"

class FilterListViewModel() : ViewModel() {
    private val filterRepository = FilterRepository.get()

    private val _filters: MutableStateFlow<List<Filter>> = MutableStateFlow(emptyList())

    val filters: StateFlow<List<Filter>>
        get() = _filters.asStateFlow()

    init {
        viewModelScope.launch {
            filterRepository.getFilters().collect {
                _filters.value = it
            }
        }
    }

    suspend fun addFilter(filter: Filter) {
        filterRepository.addFilter(filter)
    }

}