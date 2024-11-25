package com.example.pedalboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedalboard.databinding.FragmentSampleListBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Date
import java.util.UUID


private const val TAG = "SampleListFragment"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SampleListFragment : Fragment() {

    private var _binding: FragmentSampleListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val sampleListViewModel: SampleListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSampleListBinding.inflate(inflater, container, false)
        binding.sampleRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sampleListViewModel.samples.collect {samples ->
                    binding.sampleRecyclerView.adapter = SampleListAdapter(samples, onSampleClicked())
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "Is it running?")

        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_sample_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_sample -> {
                showNewSample()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onSampleClicked() : (sampleId: UUID) -> Unit {
        return {Snackbar.make(binding.root, "Sample Clicked", Snackbar.LENGTH_LONG).show()}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun showNewSample() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newSample = Sample(
                id = UUID.randomUUID(),
                title = "",
                filePath = ""
            )
            sampleListViewModel.addSample(newSample)
            findNavController().navigate(
                    R.id.show_sample_creator
            )
        }
    }

}

