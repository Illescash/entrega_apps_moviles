package com.partyhub.feature.elas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.partyhub.R
import com.partyhub.databinding.FragmentAsConfigBinding

class AsConfigFragment : Fragment() {

    private var _binding: FragmentAsConfigBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStart.setOnClickListener {
            val numPlayers = when (binding.rgPlayers.checkedRadioButtonId) {
                R.id.rbPlayers3 -> 3
                R.id.rbPlayers4 -> 4
                R.id.rbPlayers5 -> 5
                R.id.rbPlayers6 -> 6
                else -> 3
            }

            val action = AsConfigFragmentDirections
                .actionAsConfigFragmentToAsGameFragment(numPlayers)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
