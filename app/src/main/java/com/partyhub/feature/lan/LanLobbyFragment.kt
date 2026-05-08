package com.partyhub.feature.lan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.partyhub.R
import com.partyhub.databinding.FragmentLanLobbyBinding
import com.partyhub.feature.settings.SettingsFragment
import timber.log.Timber

/**
 * Pantalla del lobby LAN donde se crean o buscan salas para jugar en red local.
 */
class LanLobbyFragment : Fragment() {

    private var _binding: FragmentLanLobbyBinding? = null
    private val binding get() = _binding!!

    // activityViewModels para compartir el ViewModel con los fragments de juego
    private val viewModel: LanLobbyViewModel by activityViewModels()

    private lateinit var hostAdapter: LanHostAdapter
    private lateinit var playerAdapter: LanPlayerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanLobbyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupClickListeners()
        observeViewModel()

        // Empezar a buscar salas al abrir
        viewModel.startBrowsing()
    }

    private fun setupAdapters() {
        // Adapter para salas descubiertas
        hostAdapter = LanHostAdapter(emptyList()) { hostInfo ->
            val playerName = getLocalPlayerName()
            viewModel.joinRoom(hostInfo, playerName)
        }
        binding.rvHosts.adapter = hostAdapter

        // Adapter para jugadores en la sala
        playerAdapter = LanPlayerAdapter(emptyList())
        binding.rvPlayers.adapter = playerAdapter
    }

    private fun setupClickListeners() {
        binding.btnCreateRoom.setOnClickListener {
            val playerName = getLocalPlayerName()
            viewModel.createRoom(playerName)
        }

        binding.btnStartGame.setOnClickListener {
            viewModel.startGame()
        }

        binding.btnLeaveRoom.setOnClickListener {
            viewModel.leaveRoom()
        }

        // Selector de juego
        binding.toggleGame.check(R.id.btnGameMind)
        binding.toggleGame.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val game = when (checkedId) {
                    R.id.btnGameMind -> "the_mind"
                    R.id.btnGameAs -> "el_as"
                    else -> "the_mind"
                }
                viewModel.selectGame(game)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.lobbyStatus.observe(viewLifecycleOwner) { status ->
            binding.groupBrowsing.isVisible = status == LobbyStatus.BROWSING
            binding.groupRoom.isVisible = status == LobbyStatus.IN_ROOM || status == LobbyStatus.CONNECTING

            binding.tvLobbyStatus.text = when (status) {
                LobbyStatus.BROWSING -> getString(R.string.lan_browsing)
                LobbyStatus.CONNECTING -> getString(R.string.lan_connecting)
                LobbyStatus.IN_ROOM -> getString(R.string.lan_waiting)
            }
        }

        viewModel.availableHosts.observe(viewLifecycleOwner) { hosts ->
            hostAdapter.updateData(hosts)
        }

        viewModel.connectedPlayers.observe(viewLifecycleOwner) { players ->
            playerAdapter.updateData(players)
        }

        viewModel.isHost.observe(viewLifecycleOwner) { isHost ->
            binding.btnStartGame.isVisible = isHost
            binding.toggleGame.isVisible = isHost
        }

        viewModel.gameStartEvent.observe(viewLifecycleOwner) { config ->
            if (config != null) {
                navigateToGame(config)
                viewModel.consumeGameStartEvent()
            }
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
                viewModel.consumeErrorEvent()
            }
        }
    }

    private var isStartingGame = false

    private fun navigateToGame(config: GameStartConfig) {
        isStartingGame = true
        Timber.d("LAN: navegando al juego ${config.game} con ${config.playerCount} jugadores")
        when (config.game) {
            "the_mind" -> {
                val action = LanLobbyFragmentDirections
                    .actionLanLobbyFragmentToMindGameFragment(
                        numPlayers = config.playerCount,
                        difficulty = "NORMAL",
                        isLanMode = true
                    )
                findNavController().navigate(action)
            }
            "el_as" -> {
                val action = LanLobbyFragmentDirections
                    .actionLanLobbyFragmentToAsGameFragment(
                        numPlayers = config.playerCount,
                        isLanMode = true
                    )
                findNavController().navigate(action)
            }
        }
    }

    private fun getLocalPlayerName(): String {
        return SettingsFragment.getPlayerAlias(requireContext())
    }

    override fun onPause() {
        super.onPause()
        // Parar de buscar salas al salir de la pantalla
        viewModel.stopBrowsing()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Solo limpiamos la sala si salimos de la pantalla SIN ir a un juego
        if (!isStartingGame) {
            viewModel.leaveRoom()
        }
        _binding = null
    }
}
