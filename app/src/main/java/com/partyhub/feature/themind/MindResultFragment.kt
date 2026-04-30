package com.partyhub.feature.themind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.partyhub.R
import com.partyhub.databinding.FragmentMindResultBinding
import timber.log.Timber

class MindResultFragment : Fragment() {

    private var _binding: FragmentMindResultBinding? = null
    private val binding get() = _binding!!

    private val args: MindResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMindResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("The Mind: pantalla de resultados — victoria=${args.isVictory}, nivel=${args.levelReached}")

        binding.tvResultStatus.text = if (args.isVictory) {
            getString(R.string.mind_result_victory)
        } else {
            getString(R.string.mind_result_game_over)
        }

        // Mostrar nivel alcanzado
        binding.tvLevelReached.text = getString(R.string.mind_result_level, args.levelReached)

        binding.btnShare.setOnClickListener {
            val text = getString(R.string.mind_share_text, args.levelReached)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(intent, null))
        }

        binding.btnBackToHub.setOnClickListener {
            findNavController().popBackStack(R.id.hubFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
