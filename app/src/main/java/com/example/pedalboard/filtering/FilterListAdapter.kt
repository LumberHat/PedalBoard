package com.example.pedalboard.filtering

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pedalboard.databinding.ListItemFilterBinding
import java.util.UUID

class FilterHolder(
    private val binding: ListItemFilterBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(filter: Filter, onFilterClicked: (filterId: UUID) -> Unit) {
        binding.filterTitle.text = if (filter.title != "") filter.title else "Untitled"
        binding.filterDescription.text = filter.description


        binding.root.setOnClickListener {
            onFilterClicked(filter.id)
        }
    }
}

class FilterListAdapter(
    private val filters: List<Filter>,
    private val onFilterClicked: (filterId: UUID) -> Unit
) : RecyclerView.Adapter<FilterHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemFilterBinding.inflate(inflater, parent, false)
        return  FilterHolder(binding)
    }

    override fun getItemCount() = filters.size

    override fun onBindViewHolder(holder: FilterHolder, position: Int) {
        val filter = filters[position]
        holder.bind(filter, onFilterClicked)
    }
}