package com.example.pedalboard.sampling

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pedalboard.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SampleSelectorFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val manager = LinearLayoutManager(context)
            val view = inflater.inflate(R.layout.dialog_sample_picker, null)
            val recyclerView = view.findViewById<RecyclerView>(R.id.sample_picker_recycler_view)
            recyclerView.layoutManager = manager

            GlobalScope.launch {
                var samples: List<Sample>
                SampleRepository.get().getSamples().collect {samples ->
                    recyclerView.adapter = SampleListAdapter(samples) { sampleId ->
                        setFragmentResult(
                            REQUEST_KEY_SAMPLE,
                            bundleOf(
                                BUNDLE_KEY_SAMPLE to sampleId
                            )
                        )
                        dismiss()
                    }
                }
            }

            builder.setView(view)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        const val REQUEST_KEY_SAMPLE = "REQUEST_KEY_SAMPLE"
        const val BUNDLE_KEY_SAMPLE = "BUNDLE_KEY_SAMPLE"

    }

}



//override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//    return activity?.let {
//        val builder = AlertDialog.Builder(it)
//        val inflater = requireActivity().layoutInflater;
//        val manager = LinearLayoutManager(context)
//        val view = inflater.inflate(R.layout.dialog_sample_picker, null)
//        val recyclerView = view.findViewById<RecyclerView>(R.id.sample_picker_recycler_view)
//        recyclerView.layoutManager = manager
//
//        var samples = SampleRepository.get().getSamples().collect {it -> it}
//
//        recyclerView.adapter = SampleListAdapter(, {i -> 1})
//
//
//        builder.setView(inflater.inflate(R.layout.dialog_sample_picker, null))
//
//        builder.create()
//    } ?: throw IllegalStateException("Activity cannot be null")
//}