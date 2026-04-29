package com.partyhub.feature.hub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.partyhub.R
import com.partyhub.core.model.GameInfo
import com.partyhub.databinding.FragmentHubBinding
import com.partyhub.feature.elas.ElAsActivity
import com.partyhub.feature.themind.TheMindActivity

/**
 * Pantalla principal que muestra la lista de juegos disponibles.
 */
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
        setupRecyclerView()
        setupFab()
    }

    private fun setupFab() {
        binding.fabLan.setOnClickListener {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "Próximamente: Modo LAN para jugar con amigos",
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun setupRecyclerView() {
        // 1. Preparamos los datos de los juegos (pueden venir de un strings.xml más adelante)
        val games = listOf(
            GameInfo(
                id = "the_mind",
                name = getString(R.string.game_mind_title),
                description = getString(R.string.game_mind_desc),
                iconRes = R.drawable.ic_launcher_foreground, // Cambiar por icono real en Fase 6
                minPlayers = 2,
                maxPlayers = 8
            ),
            GameInfo(
                id = "el_as",
                name = getString(R.string.game_as_title),
                description = getString(R.string.game_as_desc),
                iconRes = R.drawable.ic_launcher_foreground, // Cambiar por icono real en Fase 6
                minPlayers = 3,
                maxPlayers = 8
            )
        )

        // 2. Creamos el adaptador y definimos qué hacer al pulsar un juego
        val adapter = GameAdapter(games) { game ->
            navigateToGame(game.id)
        }

        // 3. Lo vinculamos al RecyclerView del layout
        binding.rvGames.adapter = adapter
    }

    private fun navigateToGame(gameId: String) {
        val intent = when (gameId) {
            "the_mind" -> Intent(requireContext(), TheMindActivity::class.java)
            "el_as" -> Intent(requireContext(), ElAsActivity::class.java)
            else -> null
        }
        intent?.let { startActivity(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
