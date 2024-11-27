package com.example.pedalboard.sampling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.appstate.cs.sample.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SampleCreatorViewModel(sampleId: UUID) : ViewModel() {

    private val sampleRepository = SampleRepository.get()

    private val _sample: MutableStateFlow<Sample?> = MutableStateFlow(null)
    val sample: StateFlow<Sample?> = _sample.asStateFlow()

    init {
        viewModelScope.launch {
            _sample.value = sampleRepository.getSample(sampleId)
        }
    }

    fun deleteSample() {
        sample.value?.let {
            sampleRepository.deleteSample(it)
        }
        _sample.value = null
    }

    suspend fun addSample(sample: Sample) {
        sampleRepository.addSample(sample)
    }

    fun updateSample(onUpdate: (Sample) -> Sample) {
        _sample.update { oldSample ->
            oldSample?.let {
                onUpdate(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sample.value?.let { sampleRepository.updateSample(it) }
    }
}

class SampleCreatorViewModelFactory(private val sampleId: UUID) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SampleCreatorViewModel(sampleId) as T
    }
}