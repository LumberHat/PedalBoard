package com.example.pedalboard

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.pedalboard.databinding.FragmentRecorderBinding
import java.io.IOException


private const val TAG = "RecorderFragment"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RecorderFragment : Fragment() {

    private var _binding: FragmentRecorderBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    private var audioRecorder: MediaRecorder? = null
    private var recording: Boolean = false
    private var fileName: String = ""

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permissionGranted ->
        if (permissionGranted) {
            Log.d(TAG, "Granted Audio Recording permission")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "Is it running?")

        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_recorder, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_player -> {
                findNavController().navigate(
                    R.id.show_player
                )
                true
            }
            R.id.action_samples -> {
                findNavController().navigate(
                    R.id.show_sample_list
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        requestPermissions()

        fileName = "${context?.getFilesDir()}/recording.3gp"
        Log.d(TAG, "Set path to: ${fileName}")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRecorderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toggleRecording.setOnClickListener {
            toggleRecording()
        }
    }

    private fun toggleRecording() {
        if (recording) {
            audioRecorder?.apply {
                stop()
                release()
            }
            audioRecorder = null;
            Log.d(TAG, "Stopping Recording")
        } else {
            audioRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(fileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                try {
                    prepare()
                } catch (e: IOException) {
                    Log.e(TAG, "prepare() failed")
                }

                start()
            }
            Log.d(TAG, "Starting Recording")
        }
        recording = !recording
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        audioRecorder?.release()
        audioRecorder = null
    }

    fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

