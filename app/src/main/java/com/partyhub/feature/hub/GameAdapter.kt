package com.partyhub.feature.hub

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.partyhub.core.model.GameInfo
import com.partyhub.databinding.ListItemGameBinding

/**
 * Adaptador para la lista de juegos del Hub.
 * Usa DataBinding para vincular cada [GameInfo] con su vista.
 */
class GameAdapter(
    private val games: List<GameInfo>,
    private val onGameClick: (GameInfo) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemGameBinding.inflate(layoutInflater, parent, false)
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(games[position])
    }

    override fun getItemCount(): Int = games.size

    /**
     * ViewHolder que contiene la lógica de vinculación de datos para un item.
     */
    inner class GameViewHolder(private val binding: ListItemGameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameInfo) {
            // Asignamos el objeto game a la variable definida en el XML
            binding.game = game
            
            // Configuramos el click listener en la raíz del item
            binding.root.setOnClickListener { onGameClick(game) }
            
            // Obligamos a que los cambios se apliquen inmediatamente
            binding.executePendingBindings()
        }
    }
}
