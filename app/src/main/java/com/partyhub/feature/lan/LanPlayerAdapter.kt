package com.partyhub.feature.lan

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter sencillo para mostrar la lista de jugadores conectados en la sala LAN.
 */
class LanPlayerAdapter(
    private var players: List<String>
) : RecyclerView.Adapter<LanPlayerAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        textView.setTextColor(android.graphics.Color.WHITE)
        textView.textSize = 18f
        textView.setPadding(32, 24, 32, 24)
        return PlayerViewHolder(textView)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val emoji = if (position == 0) "👑 " else "👤 "
        holder.textView.text = "$emoji${players[position]}"
    }

    override fun getItemCount(): Int = players.size

    fun updateData(newPlayers: List<String>) {
        players = newPlayers
        notifyDataSetChanged()
    }
}
