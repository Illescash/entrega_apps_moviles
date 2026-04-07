package com.partyhub.feature.elas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.partyhub.R
import com.partyhub.databinding.FragmentAsResultBinding

class AsResultFragment : Fragment() {

    private var _binding: FragmentAsResultBinding? = null
    private val binding get() = _binding!!

    private val args: AsResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvWinner.text = getString(R.string.as_result_winner, args.winnerName)

        binding.btnShare.setOnClickListener {
            val text = getString(R.string.as_share_text, args.winnerName)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(intent, null))
        }

        binding.btnBackToHub.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
