package com.example.pedalboard.filtering

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pedalboard.R
import com.example.pedalboard.databinding.FragmentFilterListBinding
import kotlinx.coroutines.launch
import java.util.UUID


private const val TAG = "FilterListFragment"
private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FilterListFragment : Fragment() {

    private var _binding: FragmentFilterListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val filterListViewModel: FilterListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFilterListBinding.inflate(inflater, container, false)
        binding.filterRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                filterListViewModel.filters.collect { filters ->
                    binding.filterRecyclerView.adapter = FilterListAdapter(filters) { filterId ->
                        Log.d(TAG, "Opening Filter with ID: $filterId")
                        findNavController().navigate(
                            FilterListFragmentDirections.showFilterCreator(filterId)
                        )
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(TAG, "Is it running?")

        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_filter_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.new_filter -> {
                showNewFilter()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun showNewFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newFilter = Filter(
                id = UUID.randomUUID(),
                title = "",
                description = "",
                filePath = ""
            )

            filterListViewModel.addFilter(newFilter)
            findNavController().navigate(
                    FilterListFragmentDirections.showFilterCreator(newFilter.id)
            )
        }
    }

}

