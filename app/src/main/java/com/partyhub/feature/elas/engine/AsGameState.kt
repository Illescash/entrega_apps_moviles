package com.partyhub.feature.elas.engine

import com.partyhub.core.model.Player
import com.partyhub.core.model.SpanishCard

data class AsGameState(
    val players: List<AsPlayer>,
    val deck: List<SpanishCard>,
    val currentPlayerIndex: Int,
    val status: AsStatus,
    val lastAction: String? = null
)

data class AsPlayer(
    val player: Player,
    val hand: SpanishCard?,
    val lives: Int = 3,
    val isOut: Boolean = false
)

enum class AsStatus {
    WAITING_ACTION, // Esperando que el jugador decida INTERCAMBIAR o QUEDARSE
    REVEALING,      // Todos muestran sus cartas
    ROUND_OVER,     // Se quitan vidas
    GAME_OVER       // Alguien gana
}
