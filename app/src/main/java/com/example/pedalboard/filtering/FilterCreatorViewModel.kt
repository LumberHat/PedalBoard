package com.example.pedalboard.filtering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pedalboard.sampling.Sample
import com.example.pedalboard.sampling.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class FilterCreatorViewModel(filterId: UUID) : ViewModel() {

    private val filterRepository = FilterRepository.get()

    private val sampleRepository = SampleRepository.get()

    private val _samples: MutableStateFlow<List<Sample>> = MutableStateFlow(emptyList())
    val samples: StateFlow<List<Sample>>
        get() = _samples.asStateFlow()


    private val _filter: MutableStateFlow<Filter?> = MutableStateFlow(null)
    val filter: StateFlow<Filter?> = _filter.asStateFlow()

    init {
        viewModelScope.launch {
            _filter.value = filterRepository.getFilter(filterId)

            sampleRepository.getSamples().collect {
                _samples.value = it
            }
        }
    }

    fun deleteFilter() {
        filter.value?.let {
            filterRepository.deleteFilter(it)
        }
        _filter.value = null
    }

    suspend fun addFilter(filter: Filter) {
        filterRepository.addFilter(filter)
    }

    suspend fun duplicateFilter(filter: Filter) {
        filterRepository.duplicateFilter(filter)
    }

    fun updateFilter(onUpdate: (Filter) -> Filter) {
        _filter.update { oldFilter ->
            oldFilter?.let {
                onUpdate(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        filter.value?.let { filterRepository.updateFilter(it) }
    }
}

class FilterCreatorViewModelFactory(private val filterId: UUID) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FilterCreatorViewModel(filterId) as T
    }
}