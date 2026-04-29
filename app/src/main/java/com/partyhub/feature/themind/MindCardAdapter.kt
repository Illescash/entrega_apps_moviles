package com.partyhub.feature.themind

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.partyhub.databinding.ListItemMindCardBinding

/**
 * Adaptador para mostrar la mano de cartas de un jugador en The Mind.
 */
class MindCardAdapter(
    private var cards: List<Int>,
    private val onCardClick: () -> Unit
) : RecyclerView.Adapter<MindCardAdapter.CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ListItemMindCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(cards[position], position == 0)
    }

    override fun getItemCount(): Int = cards.size

    fun updateCards(newCards: List<Int>) {
        cards = newCards
        notifyDataSetChanged()
    }

    inner class CardViewHolder(private val binding: ListItemMindCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(number: Int, isLowest: Boolean) {
            binding.cardNumber = number
            
            // Si es la carta más baja (la que debería jugar), resaltamos el borde
            binding.cardContainer.strokeWidth = if (isLowest) 3 else 1
            
            // Solo permitimos jugar pulsando la primera carta (o todas, depende de la lógica)
            binding.root.setOnClickListener {
                if (isLowest) onCardClick()
            }
            
            binding.executePendingBindings()
        }
    }
}
