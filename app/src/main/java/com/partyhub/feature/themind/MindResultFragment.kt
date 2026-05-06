package com.partyhub.feature.themind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.partyhub.PartyHubApp
import com.partyhub.R
import com.partyhub.database.MatchHistory
import com.partyhub.databinding.FragmentMindResultBinding
import com.partyhub.feature.history.HistoryRepository
import com.partyhub.feature.history.HistoryViewModel
import com.partyhub.feature.history.HistoryViewModelFactory
import timber.log.Timber

class MindResultFragment : Fragment() {

    private var _binding: FragmentMindResultBinding? = null
    private val binding get() = _binding!!

    private val args: MindResultFragmentArgs by navArgs()

    private val historyViewModel: HistoryViewModel by viewModels {
        val app = requireActivity().application as PartyHubApp
        HistoryViewModelFactory(HistoryRepository(app.database.matchDao()))
    }

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

        saveMatch()
    }

    private fun saveMatch() {
        val winner = if (args.isVictory) getString(R.string.game_result_team) else getString(R.string.game_result_nobody)
        val match = MatchHistory(
            gameName = getString(R.string.game_mind_title),
            players = getString(R.string.game_result_multiplayer),
            winner = winner,
            durationMs = 0,
            finishedAt = System.currentTimeMillis()
        )
        historyViewModel.insert(match)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
