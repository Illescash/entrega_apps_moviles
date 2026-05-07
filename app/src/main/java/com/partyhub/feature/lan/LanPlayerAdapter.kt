package com.partyhub.feature.lan

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.partyhub.R

/**
 * Adapter sencillo para mostrar la lista de jugadores conectados en la sala LAN.
 */
class LanPlayerAdapter(
    private var players: List<String>
) : RecyclerView.Adapter<LanPlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvPlayerEmoji)
        val tvName: TextView = view.findViewById(R.id.tvPlayerName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_lan_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.tvEmoji.text = if (position == 0) "👑" else "👤"
        holder.tvName.text = players[position]
    }

    override fun getItemCount(): Int = players.size

    fun updateData(newPlayers: List<String>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}
