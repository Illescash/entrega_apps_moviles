package com.partyhub.feature.themind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.partyhub.databinding.FragmentMindGameBinding

class MindGameFragment : Fragment() {

    private var _binding: FragmentMindGameBinding? = null
    private val binding get() = _binding!!

    private val args: MindGameFragmentArgs by navArgs()

    private val viewModel: MindViewModel by lazy {
        ViewModelProvider(this).get(MindViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMindGameBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // El uso de args.numPlayers y args.difficulty se implementará en el próximo commit
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
