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
            
            // Si el jugador está fuera, lo mostramos más tenue y sin carta
            if (state.isOut) {
                binding.root.alpha = 0.5f
                binding.ivSmallCard.setImageResource(android.R.color.transparent)
            } else {
                binding.root.alpha = 1.0f
                if (state.hand != null) {
                    val suitPrefix = when (state.hand.suit) {
                        com.partyhub.core.model.SpanishCard.Suit.OROS -> "oro"
                        com.partyhub.core.model.SpanishCard.Suit.COPAS -> "copa"
                        com.partyhub.core.model.SpanishCard.Suit.ESPADAS -> "espada"
                        com.partyhub.core.model.SpanishCard.Suit.BASTOS -> "basto"
                    }
                    val resId = binding.root.context.resources.getIdentifier("${suitPrefix}_${state.hand.number}", "drawable", binding.root.context.packageName)
                    if (resId != 0) {
                        binding.ivSmallCard.setImageResource(resId)
                    } else {
                        binding.ivSmallCard.setImageResource(android.R.drawable.ic_menu_help)
                    }
                } else {
                    binding.ivSmallCard.setImageResource(com.partyhub.R.drawable.ic_launcher_foreground)
                }
            }
            
            binding.executePendingBindings()
        }
    }
}
