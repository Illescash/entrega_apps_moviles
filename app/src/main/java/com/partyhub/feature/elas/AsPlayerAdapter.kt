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
            
            // Gestionar la mini-carta del jugador
            if (state.lives > 0) {
                binding.ivPlayerCard?.visibility = android.view.View.VISIBLE
                if (state.hand != null) {
                    // Si tenemos la info de la carta, la mostramos
                    val context = binding.root.context
                    val suitName = state.hand.suit.name.lowercase()
                    val resName = "${suitName}_${state.hand.number}"
                    val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                    if (resId != 0) {
                        binding.ivPlayerCard?.setImageResource(resId)
                    } else {
                        binding.ivPlayerCard?.setImageResource(com.partyhub.R.drawable.ic_partyhub_logo)
                    }
                } else {
                    // Si no tenemos la info (es de otro jugador y no es fase de revelado), boca abajo
                    binding.ivPlayerCard?.setImageResource(com.partyhub.R.drawable.ic_partyhub_logo)
                }
            } else {
                binding.ivPlayerCard?.visibility = android.view.View.GONE
            }

            // Si el jugador está fuera, lo mostramos más tenue
            if (state.isOut) {
                binding.root.alpha = 0.5f
            } else {
                binding.root.alpha = 1.0f
            }
            
            binding.executePendingBindings()
        }
    }
}
