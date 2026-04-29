package com.partyhub.feature.elas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.partyhub.databinding.ListItemAsPlayerBinding
import com.partyhub.feature.elas.engine.AsPlayer

/**
 * Adaptador para mostrar la lista de jugadores y sus vidas en El As.
 */
class AsPlayerAdapter(
    private var players: List<AsPlayer>,
    private var currentPlayerIndex: Int
) : RecyclerView.Adapter<AsPlayerAdapter.PlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ListItemAsPlayerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val playerState = players[position]
        holder.bind(playerState, position == currentPlayerIndex)
    }

    override fun getItemCount(): Int = players.size

    fun updateData(newPlayers: List<AsPlayer>, newCurrentIndex: Int) {
        players = newPlayers
        currentPlayerIndex = newCurrentIndex
        notifyDataSetChanged()
    }

    inner class PlayerViewHolder(private val binding: ListItemAsPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(state: AsPlayer, isTurn: Boolean) {
            binding.playerName = state.player.name
            binding.lives = state.lives
            binding.isTurn = isTurn
            
            // Si el jugador está fuera, lo mostramos más tenue
            binding.root.alpha = if (state.isOut) 0.5f else 1.0f
            
            binding.executePendingBindings()
        }
    }
}
