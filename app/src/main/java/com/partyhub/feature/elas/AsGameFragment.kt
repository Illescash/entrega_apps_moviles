package com.partyhub.feature.elas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.partyhub.R
import com.partyhub.databinding.FragmentAsGameBinding
import com.partyhub.feature.elas.engine.AsStatus
import com.partyhub.feature.lan.LanLobbyViewModel
import com.partyhub.core.model.SpanishCard
import com.partyhub.core.Event
import com.google.android.material.snackbar.Snackbar

class AsGameFragment : Fragment() {

    private var _binding: FragmentAsGameBinding? = null
    private val binding get() = _binding!!

    private val args: AsGameFragmentArgs by navArgs()

    private val viewModel: AsViewModel by lazy {
        ViewModelProvider(this).get(AsViewModel::class.java)
    }

    private val lanLobbyViewModel: LanLobbyViewModel by activityViewModels()

    private lateinit var playerAdapter: AsPlayerAdapter
    private var isLanMode = false
    private var isCardHiddenLocal = true
    private var isCardFlippedLan = false
    private var lastPlayerIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            isCardHiddenLocal = it.getBoolean("isCardHiddenLocal", true)
            isCardFlippedLan = it.getBoolean("isCardFlippedLan", false)
            lastPlayerIndex = it.getInt("lastPlayerIndex", -1)
        }

        isLanMode = args.isLanMode

        setupRecyclerView()

        if (isLanMode) {
            setupLanMode()
        } else {
            if (viewModel.gameState.value == null) {
                viewModel.startGame(args.numPlayers)
            }
        }

        setupObservers()
        setupClickListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isCardHiddenLocal", isCardHiddenLocal)
        outState.putBoolean("isCardFlippedLan", isCardFlippedLan)
        outState.putInt("lastPlayerIndex", lastPlayerIndex)
    }

    private fun setupLanMode() {
        val isHost = lanLobbyViewModel.isHost.value ?: false
        val server = lanLobbyViewModel.getServer()
        val client = lanLobbyViewModel.getClient()
        val playerId = lanLobbyViewModel.getLocalPlayerId()

        viewModel.setupLanMode(server, client, playerId, isHost)

        if (isHost && viewModel.gameState.value == null) {
            val playerNames = lanLobbyViewModel.connectedPlayers.value ?: emptyList()
            viewModel.startLanGame(playerNames)
        }
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

            // Ocultar la carta local al cambiar el turno si no estamos revelando
            if (!isLanMode && currentPlayerIndex != lastPlayerIndex && state.status == AsStatus.WAITING_ACTION) {
                isCardHiddenLocal = true
            }
            lastPlayerIndex = currentPlayerIndex

            // Actualizar imagen de la carta
            if (isLanMode) {
                // En LAN, la carta central es SIEMPRE la del jugador local
                val myName = viewModel.getLocalPlayerId()
                val localPlayer = state.players.find { it.player.name == myName }
                if (localPlayer != null) {
                    updateCardImage(localPlayer.hand)
                }
            } else {
                // En modo LOCAL (un solo móvil), mostramos la carta del jugador actual
                if (isCardHiddenLocal && state.status == AsStatus.WAITING_ACTION) {
                    binding.ivCard.setImageResource(R.drawable.ic_partyhub_logo)
                    binding.btnShowCard.isVisible = true
                } else {
                    binding.btnShowCard.isVisible = false
                    updateCardImage(currentPlayer.hand)
                }
            }

            val isPlaying = state.status == AsStatus.WAITING_ACTION
            val isRevealing = state.status == AsStatus.REVEALING
            val isRoundOver = state.status == AsStatus.ROUND_OVER

            if (isLanMode) {
                // En LAN, solo habilitar acciones si es tu turno
                val myName = viewModel.getLocalPlayerId()
                val myIndex = state.players.indexOfFirst { it.player.name == myName }
                val isMyTurn = currentPlayerIndex == myIndex
                binding.llActions.isVisible = isPlaying && isMyTurn
                binding.btnResolveRound.isVisible = isRevealing
                binding.btnNextRound.isVisible = isRoundOver
            } else {
                // En local, las acciones solo se muestran si la carta no está oculta
                binding.llActions.isVisible = isPlaying && !isCardHiddenLocal
                binding.btnResolveRound.isVisible = isRevealing
                binding.btnNextRound.isVisible = isRoundOver
            }

            // Mostrar última acción (mensaje de Rey, etc)
            if (!state.lastAction.isNullOrEmpty()) {
                binding.tvLastAction?.text = state.lastAction
                binding.tvLastAction?.isVisible = true
            } else {
                binding.tvLastAction?.isVisible = false
            }

            if (state.status == AsStatus.GAME_OVER) {
                val winner = state.players.firstOrNull { !it.isOut }?.player?.name ?: "Nadie"
                val action = AsGameFragmentDirections
                    .actionAsGameFragmentToAsResultFragment(winnerName = winner)
                findNavController().navigate(action)
            }
        }

        if (isLanMode) {
            viewModel.errorEvent.observe(viewLifecycleOwner) { event ->
                event.getContentIfNotHandled()?.let { msg ->
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
                    findNavController().popBackStack()
                }
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
        binding.btnStay.setOnClickListener {
            binding.llActions.isVisible = false
            viewModel.stay()
        }
        binding.btnSwap.setOnClickListener {
            binding.llActions.isVisible = false
            viewModel.swap()
        }
        binding.btnResolveRound.setOnClickListener {
            binding.btnResolveRound.isVisible = false
            viewModel.resolveRound()
        }
        binding.btnNextRound.setOnClickListener {
            binding.btnNextRound.isVisible = false
            viewModel.nextRound()
        }

        binding.btnShowCard.setOnClickListener {
            isCardHiddenLocal = false
            // Forzar actualización del state para redibujar sin ocultar
            viewModel.gameState.value?.let { state ->
                binding.btnShowCard.isVisible = false
                binding.llActions.isVisible = state.status == AsStatus.WAITING_ACTION
                updateCardImage(state.players[state.currentPlayerIndex].hand)
            }
        }

        binding.ivCard.setOnClickListener {
            if (isLanMode) {
                val state = viewModel.gameState.value ?: return@setOnClickListener
                val currentPlayerIndex = state.currentPlayerIndex

                if (viewModel.isMyTurn.value == true && state.status == AsStatus.WAITING_ACTION) {
                    isCardFlippedLan = !isCardFlippedLan
                    if (isCardFlippedLan) {
                        binding.ivCard.setImageResource(R.drawable.ic_partyhub_logo)
                    } else {
                        updateCardImage(state.players[currentPlayerIndex].hand)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
