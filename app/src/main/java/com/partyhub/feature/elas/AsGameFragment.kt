package com.partyhub.feature.elas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.partyhub.databinding.FragmentAsGameBinding
import com.partyhub.feature.elas.engine.AsStatus

class AsGameFragment : Fragment() {

    private var _binding: FragmentAsGameBinding? = null
    private val binding get() = _binding!!

    private val args: AsGameFragmentArgs by navArgs()

    private val viewModel: AsViewModel by lazy {
        ViewModelProvider(this).get(AsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.gameState.value == null) {
            viewModel.startGame(args.numPlayers)
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.gameState.observe(viewLifecycleOwner) { state ->
            val currentPlayer = state.players[state.currentPlayerIndex]
            
            // UI Update
            binding.tvCurrentPlayer.text = currentPlayer.player.name
            binding.tvLives.text = "Vidas: ${currentPlayer.lives}"
            binding.tvCard.text = currentPlayer.hand?.number?.toString() ?: "?"
            binding.tvSuit.text = currentPlayer.hand?.suit?.name ?: ""

            // Status visibility
            val isPlaying = state.status == AsStatus.WAITING_ACTION
            val isRevealing = state.status == AsStatus.REVEALING
            val isRoundOver = state.status == AsStatus.ROUND_OVER

            binding.llActions.isVisible = isPlaying
            binding.btnResolveRound.isVisible = isRevealing
            binding.btnNextRound.isVisible = isRoundOver

            if (state.status == AsStatus.GAME_OVER) {
                val winner = state.players.firstOrNull { !it.isOut }?.player?.name ?: "Nadie"
                val action = AsGameFragmentDirections
                    .actionAsGameFragmentToAsResultFragment(winnerName = winner)
                findNavController().navigate(action)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnStay.setOnClickListener { viewModel.stay() }
        binding.btnSwap.setOnClickListener { viewModel.swap() }
        binding.btnResolveRound.setOnClickListener { viewModel.resolveRound() }
        binding.btnNextRound.setOnClickListener { viewModel.nextRound() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
