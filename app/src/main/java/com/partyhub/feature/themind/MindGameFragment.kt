package com.partyhub.feature.themind

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.partyhub.R
import com.partyhub.databinding.FragmentMindGameBinding
import com.partyhub.feature.themind.engine.MindStatus

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

        // Inicializar el juego si no ha empezado
        if (viewModel.gameState.value == null) {
            val playerNames = (1..args.numPlayers).map { "Jugador $it" }
            val initialLives = when (args.difficulty) {
                "EASY" -> 4
                "HARD" -> 2
                else -> 3
            }
            viewModel.startGame(playerNames, initialLives)
        }

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.gameState.observe(viewLifecycleOwner) { state ->
            // Actualizar visibilidad de estados
            binding.tvStatus.isVisible = state.status == MindStatus.REVEALING
            binding.btnResolve.isVisible = state.status == MindStatus.REVEALING
            binding.btnNextLevel.isVisible = state.status == MindStatus.LEVEL_COMPLETE
            
            // Navegación a resultados si el juego termina
            if (state.status == MindStatus.GAME_OVER || state.status == MindStatus.VICTORY) {
                val action = MindGameFragmentDirections
                    .actionMindGameFragmentToMindResultFragment(
                        levelReached = state.level,
                        isVictory = state.status == MindStatus.VICTORY
                    )
                findNavController().navigate(action)
            }

            // Actualizar manos de los jugadores (LinearLayout)
            updatePlayerActions(state.players.map { it.id }, state.playerHands)
        }
    }

    private fun setupClickListeners() {
        binding.btnResolve.setOnClickListener {
            viewModel.resolveLevel()
        }
        binding.btnNextLevel.setOnClickListener {
            viewModel.nextLevel()
        }
    }

    private fun updatePlayerActions(playerIds: List<String>, playerHands: Map<String, List<Int>>) {
        binding.playerActionsContainer.removeAllViews()
        
        playerIds.forEach { playerId ->
            val hand = playerHands[playerId] ?: emptyList()
            if (hand.isNotEmpty()) {
                val playerView = layoutInflater.inflate(R.layout.item_mind_player_action, binding.playerActionsContainer, false)
                
                val tvPlayerName = playerView.findViewById<TextView>(R.id.tvPlayerName)
                val btnPlayCard = playerView.findViewById<Button>(R.id.btnPlayCard)
                
                tvPlayerName.text = getString(R.string.mind_label_player_hand, playerId.toInt().plus(1))
                btnPlayCard.text = getString(R.string.mind_btn_play_card, hand.first())
                
                btnPlayCard.setOnClickListener {
                    viewModel.playCard(playerId)
                }
                
                // Deshabilitar botones si no estamos en fase de juego
                btnPlayCard.isEnabled = viewModel.gameState.value?.status == MindStatus.PLAYING
                
                binding.playerActionsContainer.addView(playerView)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
