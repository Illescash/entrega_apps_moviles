package com.partyhub.feature.themind.engine

import com.partyhub.core.model.Player

data class MindGameState(
    val level: Int,
    val lives: Int,
    val players: List<Player>,
    val playerHands: Map<String, List<Int>>,
    val playedCards: List<PlayedCard>,
    val pendingCards: List<Int>,
    val status: MindStatus
)

data class PlayedCard(
    val number: Int,
    val playerId: String,
    val wasCorrect: Boolean
)

enum class MindStatus {
    PLAYING,
    REVEALING,
    LEVEL_COMPLETE,
    GAME_OVER,
    VICTORY
}
