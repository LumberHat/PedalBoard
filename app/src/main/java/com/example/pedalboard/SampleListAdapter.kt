package com.example.pedalboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pedalboard.databinding.ListItemSampleBinding
import com.example.pedalboard.sampling.Sample
import java.util.UUID

class SampleHolder(
    private val binding: ListItemSampleBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(sample: Sample, onSampleClicked: (sampleId: UUID) -> Unit) {
        binding.sampleTitle.text = if (sample.title != "") sample.title else "untitled"

        binding.root.setOnClickListener {
            onSampleClicked(sample.id)
        }
    }
}

class SampleListAdapter(
    private val samples: List<Sample>,
    private val onSampleClicked: (sampleId: UUID) -> Unit
) : RecyclerView.Adapter<SampleHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemSampleBinding.inflate(inflater, parent, false)
        return  SampleHolder(binding)
    }

    override fun getItemCount() = samples.size

    override fun onBindViewHolder(holder: SampleHolder, position: Int) {
        val sample = samples[position]
        holder.bind(sample, onSampleClicked)
    }
}