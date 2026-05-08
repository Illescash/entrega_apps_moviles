package com.partyhub.feature.themind.engine

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.partyhub.core.model.Player

@Parcelize
data class MindGameState(
    val level: Int,
    val lives: Int,
    val players: List<Player>,
    val playerHands: Map<String, List<Int>>,
    val playedCards: List<PlayedCard>,
    val pendingCards: List<Int>,
    val status: MindStatus
) : Parcelable

@Parcelize
data class PlayedCard(
    val number: Int,
    val playerId: String,
    val wasCorrect: Boolean
) : Parcelable

@Parcelize
enum class MindStatus : Parcelable {
    PLAYING,
    REVEALING,
    LEVEL_COMPLETE,
    GAME_OVER,
    VICTORY
}
