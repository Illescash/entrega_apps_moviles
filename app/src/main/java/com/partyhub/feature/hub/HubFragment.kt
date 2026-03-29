package com.partyhub.feature.hub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.partyhub.databinding.FragmentHubBinding

class HubFragment : Fragment() {

    // ViewBinding como exige CLAUDE.md
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
        
        // Listeners para iniciar los juegos.
        // La navegación (Intents) es el contenido del SIGUIENTE commit,
        // por lo que ahora mismo los dejamos preparados y vacíos.
        
        binding.btnTheMind.setOnClickListener {
            // TODO: feat(hub): añadir navegación Hub→Juegos con Intent explícito (siguiente commit por Diego)
        }

        binding.btnElAs.setOnClickListener {
            // TODO: feat(hub): añadir navegación Hub→Juegos con Intent explícito (siguiente commit por Diego)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpieza para evitar memory leaks (requisito CLAUDE.md)
    }
}
