package com.example.pedalboard.sampling

import android.media.MediaPlayer
import android.media.MediaRecorder
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
import androidx.lifecycle.withStarted
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pedalboard.R
import com.example.pedalboard.databinding.FragmentSampleCreatorBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


private const val TAG = "SampleCreatorFragment"

class SampleCreatorFragment: Fragment() {
    private var _binding: FragmentSampleCreatorBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val coroutineScope: CoroutineScope = GlobalScope

    private val args: SampleCreatorFragmentArgs by navArgs()
    private val sampleCreatorViewModel: SampleCreatorViewModel by viewModels() {
        SampleCreatorViewModelFactory(args.sampleId)
    }

    private var audioPlayer: MediaPlayer? = null
    private var audioRecorder: MediaRecorder? = null

    private lateinit var sampleFilePath: String
    private lateinit var sampleFile: File

    private lateinit var tempFilePath: String
    private lateinit var tempFile: File

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
            testSample.setOnClickListener {
                logAll()
            }
            initSample.setOnClickListener {
                initialize()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sampleCreatorViewModel.sample.collect { sample ->
                    sample?.let { updateUi(it) }
                    initialize()
                }
            }
        }
    }

    private fun initialize() {
        sampleFilePath = sampleCreatorViewModel.sample.value?.filePath.toString()
        sampleFile = File(sampleFilePath)

        tempFilePath = "${context?.getDir("samples", 0)?.absolutePath}/tempFile.3gp"
        tempFile = File(tempFilePath)

        logAll()
    }

    private fun logAll() {
        logSampleCreator()
        logSample()
    }

    private fun logSample() {
        Log.d(TAG, "Sample: ${sampleCreatorViewModel.sample.value?.id}\n" +
                "Title: ${sampleCreatorViewModel.sample.value?.title}\n" +
                "Path: ${sampleCreatorViewModel.sample.value?.filePath}")
    }

    private fun logSampleCreator() {
        Log.d(TAG, "tempFilePath: ${if (this::tempFilePath.isInitialized) tempFilePath else "Uninitialized"}\n" +
                "sampleFilePath: ${if (this::sampleFilePath.isInitialized) sampleFilePath else "Uninitialized"}")
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

        audioPlayer = MediaPlayer().apply {
            try {
                setDataSource(if (recorded) tempFilePath else sampleFilePath)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed in playRecording()")
                Snackbar.make(requireView(), R.string.playback_error, Snackbar.LENGTH_LONG).show()
                binding.playSample.isEnabled = true
                binding.playSample.text = getString(R.string.play)
                audioPlayer?.release()
                audioPlayer = null
            }
        }

        audioPlayer?.setOnCompletionListener {
            binding.playSample.isEnabled = true
            binding.playSample.text = getString(R.string.play)
            audioPlayer?.release()
            audioPlayer = null
        }
    }

    private var recording: Boolean = false
    private var recorded: Boolean = false
    private fun toggleRecording() {
        recording = !recording
        binding.recordSample.text = if (!recording) getString(R.string.record) else getString(R.string.stop)
        menu.getItem(0).setEnabled(!recording)
        menu.getItem(1).setEnabled(!recording)

        if (recording) {
            audioRecorder?.apply {
                stop()
                release()
            }
            audioRecorder = null;
            Log.d(TAG, "Stopping Recording")
        } else {
            recorded = true
            audioRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(tempFilePath)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e(TAG, "prepare() failed")
                    Snackbar.make(requireView(), R.string.recording_error, Snackbar.LENGTH_LONG).show()

                    audioRecorder?.apply {
                        stop()
                        release()
                    }
                    audioRecorder = null;
                }

                start()
            }
            Log.d(TAG, "Starting Recording")
        }
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
        coroutineScope.launch {
            copyFile(tempFilePath, sampleFilePath)
        }
    }

    private fun duplicate() {
        Log.d(TAG,"Duplicating")
        val newSample = Sample(
            id = UUID.randomUUID(),
            title = sampleCreatorViewModel.sample.value?.title.toString(),
            description = sampleCreatorViewModel.sample.value?.description.toString(),
            filePath = "${context?.getFilesDir()}/${id}.3gp"
        )

        coroutineScope.launch {
            copyFile(tempFilePath, newSample.filePath)
            sampleCreatorViewModel.addSample(newSample)
        }
    }

    //https://stackoverflow.com/questions/9292954/how-to-make-a-copy-of-a-file-in-android
    @Throws(IOException::class)
    fun copyFile(srcPath: String, dstPath: String) {
        Log.d(TAG,"Copying ${srcPath} to ${dstPath}")
        val src = File(srcPath)

        val dst = File(dstPath)
        if (!dst.exists()) {dst.createNewFile()}

        if (src.exists()) {
            FileInputStream(src).use { `in` ->
                FileOutputStream(dst).use { out ->
                    // Transfer bytes from in to out
                    val buf = ByteArray(1024)
                    var len: Int
                    while ((`in`.read(buf).also { len = it }) > 0) {
                        out.write(buf, 0, len)
                    }
                }
            }
        } else {
            Log.d(TAG, "Sourcefile nonexistant")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(sample: Sample) {
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