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
import com.partyhub.R
import com.partyhub.databinding.FragmentAsGameBinding
import com.partyhub.feature.elas.engine.AsStatus
import com.partyhub.core.model.SpanishCard

class AsGameFragment : Fragment() {

    private var _binding: FragmentAsGameBinding? = null
    private val binding get() = _binding!!

    private val args: AsGameFragmentArgs by navArgs()

    private val viewModel: AsViewModel by lazy {
        ViewModelProvider(this).get(AsViewModel::class.java)
    }

    private lateinit var playerAdapter: AsPlayerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        if (viewModel.gameState.value == null) {
            viewModel.startGame(args.numPlayers)
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        playerAdapter = AsPlayerAdapter(emptyList(), -1)
        binding.rvPlayers.adapter = playerAdapter
    }

    private fun setupObservers() {
        viewModel.gameState.observe(viewLifecycleOwner) { state ->
            val currentPlayerIndex = state.currentPlayerIndex
            val currentPlayer = state.players[currentPlayerIndex]
            
            playerAdapter.updateData(state.players, currentPlayerIndex)
            binding.rvPlayers.smoothScrollToPosition(currentPlayerIndex)

            // Actualizar imagen de la carta
            updateCardImage(currentPlayer.hand)

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

    private fun updateCardImage(card: SpanishCard?) {
        if (card == null) {
            binding.ivCard.setImageResource(android.R.color.transparent)
            return
        }

        // Mapear el palo a su nombre en minúsculas y singular (OROS -> oro)
        val suitPrefix = when (card.suit) {
            SpanishCard.Suit.OROS -> "oro"
            SpanishCard.Suit.COPAS -> "copa"
            SpanishCard.Suit.ESPADAS -> "espada"
            SpanishCard.Suit.BASTOS -> "basto"
        }

        val drawableName = "${suitPrefix}_${card.number}"
        val resId = resources.getIdentifier(drawableName, "drawable", requireContext().packageName)

        if (resId != 0) {
            binding.ivCard.setImageResource(resId)
        } else {
            // Fallback si no encuentra el drawable (ponemos un icono de ayuda)
            binding.ivCard.setImageResource(android.R.drawable.ic_menu_help)
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
