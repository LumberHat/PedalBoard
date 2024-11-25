package com.example.pedalboard.sampling

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.pedalboard.databinding.FragmentSampleCreatorBinding

class SampleCreatorFragment: Fragment() {
    private var _binding: FragmentSampleCreatorBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: SampleCreatorFragmentArgs by navArgs()
}