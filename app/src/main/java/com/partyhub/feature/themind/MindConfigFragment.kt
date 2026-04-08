package com.partyhub.feature.themind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.partyhub.R
import com.partyhub.databinding.FragmentMindConfigBinding

class MindConfigFragment : Fragment() {

    private var _binding: FragmentMindConfigBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MindViewModel by lazy {
        ViewModelProvider(this).get(MindViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMindConfigBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let { state ->
            val playersId = state.getInt(KEY_PLAYERS, R.id.rbPlayers2)
            val difficultyId = state.getInt(KEY_DIFFICULTY, R.id.rbEasy)
            binding.rgPlayers.check(playersId)
            binding.rgDifficulty.check(difficultyId)
        }

        binding.btnStart.setOnClickListener {
            val numPlayers = when (binding.rgPlayers.checkedRadioButtonId) {
                R.id.rbPlayers2 -> 2
                R.id.rbPlayers3 -> 3
                R.id.rbPlayers4 -> 4
                else -> 2
            }

            val difficulty = when (binding.rgDifficulty.checkedRadioButtonId) {
                R.id.rbEasy -> "EASY"
                R.id.rbNormal -> "NORMAL"
                R.id.rbHard -> "HARD"
                else -> "NORMAL"
            }

            val action = MindConfigFragmentDirections
                .actionMindConfigFragmentToMindGameFragment(numPlayers, difficulty)
            findNavController().navigate(action)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let { b ->
            outState.putInt(KEY_PLAYERS, b.rgPlayers.checkedRadioButtonId)
            outState.putInt(KEY_DIFFICULTY, b.rgDifficulty.checkedRadioButtonId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_PLAYERS = "mind_config_players"
        private const val KEY_DIFFICULTY = "mind_config_difficulty"
    }
}
