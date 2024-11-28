package com.example.pedalboard.filtering


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
import com.example.pedalboard.R
import com.example.pedalboard.databinding.FragmentFilterCreatorBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException


private const val TAG = "FilterCreatorFragment"

class FilterCreatorFragment: Fragment() {
    private var _binding: FragmentFilterCreatorBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val audioHub = AudioHub.get()

    private val coroutineScope: CoroutineScope = GlobalScope

    private val args: FilterCreatorFragmentArgs by navArgs()
    private val filterCreatorViewModel: FilterCreatorViewModel by viewModels() {
        FilterCreatorViewModelFactory(args.filterId)
    }

    private lateinit var filterFile: File

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                filterCreatorViewModel.filter.collect { filter ->
                    filter?.let { updateUi(it) }
                    initialize()
                }
            }
        }
    }

    private fun initialize() {
        filterFile = File(filterCreatorViewModel.filter.value?.filePath.toString())



        audioHub.setFile(filterFile)
        logAll()
    }

    private fun logAll() {
        logFilter()
    }

    private fun logFilter() {
        Log.d(TAG, "Filter: ${filterCreatorViewModel.filter.value?.id}\n" +
                "Title: ${filterCreatorViewModel.filter.value?.title}\n" +
                "Path: ${filterCreatorViewModel.filter.value?.filePath}")
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
        filterCreatorViewModel.updateFilter { oldFilter ->
            oldFilter.copy(title = binding.filterTitle.text.toString(),
                description = binding.filterDescription.text.toString())
        }
        audioHub.writeFromCache()
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
        }
    }
}