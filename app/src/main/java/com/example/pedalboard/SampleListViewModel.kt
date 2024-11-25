package com.example.pedalboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.appstate.cs.sample.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "SampleListViewModel"

class SampleListViewModel() : ViewModel() {
    private val sampleRepository = SampleRepository.get()

    private val _samples: MutableStateFlow<List<Sample>> = MutableStateFlow(emptyList())

    val samples: StateFlow<List<Sample>>
        get() = _samples.asStateFlow()

    init {
        viewModelScope.launch {
            sampleRepository.getSamples().collect {
                _samples.value = it
            }
        }
    }

    suspend fun addSample(sample: Sample) {
        sampleRepository.addSample(sample)
    }

}