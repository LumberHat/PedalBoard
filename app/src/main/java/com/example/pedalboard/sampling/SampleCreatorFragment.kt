package com.example.pedalboard.sampling


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pedalboard.AudioHub
import com.example.pedalboard.FilesInator
import com.example.pedalboard.R
import com.example.pedalboard.databinding.FragmentSampleCreatorBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


private const val TAG = "SampleCreatorFragment"

class SampleCreatorFragment: Fragment() {
    private var _binding: FragmentSampleCreatorBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val audioHub = AudioHub.get()

    private val coroutineScope: CoroutineScope = GlobalScope

    private val args: SampleCreatorFragmentArgs by navArgs()
    private val sampleCreatorViewModel: SampleCreatorViewModel by viewModels() {
        SampleCreatorViewModelFactory(args.sampleId)
    }

    private lateinit var sampleFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            _binding =
                FragmentSampleCreatorBinding.inflate(inflater, container, false)
            return binding.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            playSample.setOnClickListener {
                Log.d(TAG, "clicked play")

                play()
            }
            recordSample.setOnClickListener {
                toggleRecording()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sampleCreatorViewModel.sample.collect { sample ->
                    sample?.let { updateUi(it) }
                }
            }
        }
    }



    private fun logAll() {
        logSample()
    }

    private fun logSample() {
        Log.d(TAG, "Sample: ${sampleCreatorViewModel.sample.value?.id}\n" +
                "Title: ${sampleCreatorViewModel.sample.value?.title}\n")
    }

    lateinit var menu: Menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        this.menu = menu
        inflater.inflate(R.menu.fragment_sample_creator, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.duplicate_sample -> {
                Log.d(TAG, "Clicked Duplicate")
                duplicate()
                true
            }
            R.id.delete_sample -> {
                Log.d(TAG, "Clicked Delete")
                delete()
                findNavController().navigateUp()
                true
            }
            R.id.save_sample -> {
                Log.d(TAG, "Clicked Save")
                save()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun play() {
        Log.d(TAG,"playing")
        binding.playSample.text = "..."
        binding.playSample.isEnabled = false

        try {
            audioHub.play {
                binding.playSample.isEnabled = true
                binding.playSample.text = getString(R.string.play)
            }
        } catch (e: IOException) {
            Log.e(TAG, "playback failed with exception: $e")
            binding.playSample.isEnabled = true
            binding.playSample.text = getString(R.string.play)
            Snackbar.make(requireView(), R.string.playback_error, Snackbar.LENGTH_LONG).show()
        }
    }


    private fun toggleRecording() {
        try {
            audioHub.toggleRecording()
        } catch (e: IOException) {
            Log.e(TAG, "recording failed with exception: ${e}")
            Snackbar.make(requireView(), R.string.recording_error, Snackbar.LENGTH_LONG).show()
        }
        binding.recordSample.text = if (!audioHub.recorder.isRecording) getString(R.string.record) else getString(R.string.stop)
        menu.getItem(0).setEnabled(!audioHub.recorder.isRecording)
        menu.getItem(1).setEnabled(!audioHub.recorder.isRecording)
    }

    private fun delete() {
        sampleCreatorViewModel.deleteSample()
    }

    private fun save() {
        Log.d(TAG,"Saving")
        sampleCreatorViewModel.updateSample { oldSample ->
            oldSample.copy(title = binding.sampleTitle.text.toString(),
                description = binding.sampleDescription.text.toString())
        }
        audioHub.writeFromCache()
    }

    private fun duplicate() {
        Log.d(TAG,"Duplicating")
        sampleCreatorViewModel.sample.value?.let {
            coroutineScope.launch {
                sampleCreatorViewModel.duplicateSample(it)
            }
        }
    }

    override fun onDestroyView() {
        audioHub.killAll()
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(sample: Sample) {
        audioHub.setSample(sample)
        binding.apply {
            if (sampleTitle.text.toString() != sample.title) {
                sampleTitle.setText(sample.title)
            }
            if (sampleDescription.text.toString() != sample.description) {
                sampleDescription.setText(sample.description)
            }
        }
    }
}