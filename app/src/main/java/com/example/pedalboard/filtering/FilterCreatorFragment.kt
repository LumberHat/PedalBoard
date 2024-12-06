package com.example.pedalboard.filtering


import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pedalboard.AudioHub
import com.example.pedalboard.AudioPlayer
import com.example.pedalboard.R
import com.example.pedalboard.databinding.FragmentFilterCreatorBinding
import com.example.pedalboard.databinding.FragmentSettingsBarBinding
import com.example.pedalboard.filtering.baseFilters.Eq
import com.example.pedalboard.filtering.baseFilters.FilterComponent
import com.example.pedalboard.sampling.Sample
import com.example.pedalboard.sampling.SampleListFragment
import com.example.pedalboard.sampling.SampleRepository
import com.example.pedalboard.sampling.SampleSelectorFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import android.app.Activity
import android.widget.Button
import com.example.pedalboard.FilesInator
import com.example.pedalboard.filtering.baseFilters.DigitalFilter
import com.example.pedalboard.filtering.baseFilters.FilterData
import com.google.android.material.snackbar.Snackbar
import java.io.IOException


private const val TAG = "FilterCreatorFragment"

class FilterCreatorFragment: Fragment() {
    private var _binding: FragmentFilterCreatorBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    lateinit var audioHub: AudioHub
    private var testSample: Sample? = null
    private var digitalFilter: DigitalFilter? = null

    private val coroutineScope: CoroutineScope = GlobalScope

    private val args: FilterCreatorFragmentArgs by navArgs()
    private val filterCreatorViewModel: FilterCreatorViewModel by viewModels() {
        FilterCreatorViewModelFactory(args.filterId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding =
            FragmentFilterCreatorBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioHub = AudioHub.get()


        setFragmentResultListener(
            SampleSelectorFragment.REQUEST_KEY_SAMPLE
        ) {_, bundle ->
            val sampleId = bundle.getSerializable(SampleSelectorFragment.BUNDLE_KEY_SAMPLE) as UUID
            loadSample(sampleId)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                filterCreatorViewModel.filter.collect { filter ->
                    filter?.let { updateUi(it) }
                }
            }
        }
    }

    private fun play() {

        if (digitalFilter == null) {
            Snackbar.make(requireView(), "Cannot play before a filter is loaded", Snackbar.LENGTH_LONG).show()
            return
        } else if (testSample == null) {
            Snackbar.make(requireView(), "Select a sample first", Snackbar.LENGTH_LONG).show()
            return
        }

        Log.d(TAG,"playing")
        binding.playFilter.text = "..."
        binding.playFilter.isEnabled = false

        try {
            audioHub.play {
                binding.playFilter.isEnabled = true
                binding.playFilter.text = getString(R.string.play)
            }
        } catch (e: IOException) {
            Log.e(TAG, "playback failed with exception: $e")
            binding.playFilter.isEnabled = true
            binding.playFilter.text = getString(R.string.play)
            Snackbar.make(requireView(), R.string.playback_error, Snackbar.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "The media player was in an invalid state. It likely was provided an invalid file path")
            binding.playFilter.isEnabled = true
            binding.playFilter.text = getString(R.string.play)
            Snackbar.make(requireView(), R.string.playback_error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun loadSample(sampleId: UUID) {
        Log.d(TAG, "loadSample: $sampleId")
        coroutineScope.launch {
            loadSample(SampleRepository.get().getSample(sampleId))
        }
    }

    private fun loadSample(sample: Sample) {
        requireActivity().runOnUiThread {
            testSample = sample
            binding.loadSample.text = testSample!!.title
            audioHub.setSample(sample)
        }
    }

    private fun logAll() {
        logFilter()
    }

    private fun logFilter() {
        Log.d(TAG, "Id: ${filterCreatorViewModel.filter.value?.id}\n" +
                "Title: ${filterCreatorViewModel.filter.value?.title}\n" +
                "Filter: ${filterCreatorViewModel.filter.value?.config}")
    }

    lateinit var menu: Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.fragment_filter_creator, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.duplicate_filter -> {
                Log.d(TAG, "Clicked Duplicate")
                duplicate()
                true
            }
            R.id.delete_filter -> {
                Log.d(TAG, "Clicked Delete")
                delete()
                findNavController().navigateUp()
                true
            }
            R.id.save_filter -> {
                Log.d(TAG, "Clicked Save")
                save()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun delete() {
        filterCreatorViewModel.deleteFilter()
    }

    private fun save() {
        Log.d(TAG,"Saving")
        var filterConfig: FilterData = FilterData()
        if (digitalFilter != null) {
            filterConfig = digitalFilter!!.getData()
        }
        filterCreatorViewModel.updateFilter { oldFilter ->
            oldFilter.copy(
                title = binding.filterTitle.text.toString(),
                description = binding.filterDescription.text.toString(),
                config = filterConfig)
        }
    }

    private fun duplicate() {
        Log.d(TAG,"Duplicating")
        filterCreatorViewModel.filter.value?.let {
            coroutineScope.launch {
                filterCreatorViewModel.duplicateFilter(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(filter: Filter) {

        binding.apply {
            if (filterTitle.text.toString() != filter.title) {
                filterTitle.setText(filter.title)
            }
            if (filterDescription.text.toString() != filter.description) {
                filterDescription.setText(filter.description)
            }

            playFilter.setOnClickListener {
                play()
            }

            loadSample.setOnClickListener {
                findNavController().navigate(
                    FilterCreatorFragmentDirections.showSamplePicker()
                )
            }
        }
        try {
            digitalFilter = DigitalFilter.fromFilter(filter)
        } catch (e: Exception) {
            Snackbar.make(requireView(), "Could not create digital filter" , Snackbar.LENGTH_SHORT).show()
        }

        if (digitalFilter != null) {
            Log.d(TAG, "updateUi: filter not null?: $digitalFilter ${digitalFilter!!.componentCount()}")
            for (i in 0..digitalFilter!!.componentCount()) {
                val settingsBar = SettingsBarFragment(digitalFilter!!.getComponent(i))
                parentFragmentManager.beginTransaction()
                    .add(binding.componentSettings.id, settingsBar, "$i").commit()
            }
        } else {
            val b = Button(context)
            b.setOnClickListener {
                digitalFilter = Eq(filter)
                updateUi(filter)
                binding.componentSettings.removeView(b)
            }
            b.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            b.text = "Default to EQ"
            binding.componentSettings.addView(b)
        }
    }
}

class SettingsBarFragment(private val component: FilterComponent) : Fragment() {
    private var _binding: FragmentSettingsBarBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding =
            FragmentSettingsBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            componentTitle.text = component.label
            componentValue.text = component.value.toString()
            seekBar.progress = component.value
            if (component.max != null) {
                seekBar.max = component.max
            }
            if (component.min != null) {
                seekBar.min = component.min
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar, progress: Int, fromUser: Boolean) {
                    component.value = progress
                    componentValue.text = component.value.toString()
                }
                override fun onStartTrackingTouch(bar: SeekBar) {}
                override fun onStopTrackingTouch(bar: SeekBar) {}
            })
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



