package com.partyhub.feature.hub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.partyhub.databinding.FragmentHubBinding
import com.partyhub.feature.elas.ElAsActivity
import com.partyhub.feature.themind.TheMindActivity

class HubFragment : Fragment() {

    private var _binding: FragmentHubBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHubBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnTheMind.setOnClickListener {
            val intent = Intent(requireContext(), TheMindActivity::class.java)
            startActivity(intent)
        }

        binding.btnElAs.setOnClickListener {
            val intent = Intent(requireContext(), ElAsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
