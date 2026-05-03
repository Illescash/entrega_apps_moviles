package com.partyhub.feature.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.partyhub.database.MatchHistory
import com.partyhub.databinding.ListItemMatchBinding

/**
 * Adaptador para mostrar el historial de partidas en un RecyclerView.
 */
class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.MatchViewHolder>() {

    private var matches: List<MatchHistory> = emptyList()

    fun setMatches(newMatches: List<MatchHistory>) {
        this.matches = newMatches
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemMatchBinding.inflate(layoutInflater, parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(matches[position])
    }

    override fun getItemCount(): Int = matches.size

    class MatchViewHolder(private val binding: ListItemMatchBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(match: MatchHistory) {
            binding.match = match
            binding.executePendingBindings()
        }
    }
}
