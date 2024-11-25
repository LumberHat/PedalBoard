package com.example.pedalboard

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pedalboard.databinding.FragmentPlayerBinding
import java.io.IOException

private const val TAG: String = "PlayerFragment"

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private var audioPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playRecording.setOnClickListener {
            playRecording()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_player, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_recorder -> {
                findNavController().navigate(
                    R.id.show_recorder
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun playRecording() {
        val fileName = "${context?.getFilesDir()}/recording.3gp"
        audioPlayer = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed in playRecording()")
            }
        }

        audioPlayer?.setOnCompletionListener {
            audioPlayer?.release()
            audioPlayer = null
        }
    }
}