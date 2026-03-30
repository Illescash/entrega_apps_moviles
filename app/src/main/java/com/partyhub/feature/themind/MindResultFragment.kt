package com.partyhub.feature.themind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.partyhub.databinding.FragmentMindResultBinding

class MindResultFragment : Fragment() {

    private var _binding: FragmentMindResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MindViewModel by lazy {
        ViewModelProvider(this).get(MindViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMindResultBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
