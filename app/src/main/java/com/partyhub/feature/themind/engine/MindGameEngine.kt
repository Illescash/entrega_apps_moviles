package com.partyhub.feature.themind.engine

import com.partyhub.core.model.Player

class MindGameEngine {

    fun newGame(playerNames: List<String>, lives: Int = 3): MindGameState {
        val players = playerNames.map { name ->
            Player(id = name, name = name)
        }
        val hands = dealCards(players, level = 1)
        val allCards = hands.values.flatten().sorted()

        return MindGameState(
            level = 1,
            lives = lives,
            players = players,
            playerHands = hands,
            playedCards = emptyList(),
            pendingCards = allCards,
            status = MindStatus.PLAYING
        )
    }

    fun playCard(state: MindGameState, playerId: String): MindGameState {
        if (state.status != MindStatus.PLAYING) return state

        val hand = state.playerHands[playerId] ?: return state
        if (hand.isEmpty()) return state

        val card = hand.minOrNull() ?: return state
        val lowestPending = state.pendingCards.firstOrNull() ?: return state
        val wasCorrect = card == lowestPending

        val newPlayedCards = state.playedCards + PlayedCard(
            number = card,
            playerId = playerId,
            wasCorrect = wasCorrect
        )

        val newHands = state.playerHands.mapValues { (_, playerHand) ->
            playerHand.filter { it > card }
        }

        // Quitar la carta jugada de pendientes.
        // Si es incorrecta, tambien quitar todas las que eran menores (se pierden).
        val newPending = state.pendingCards.filter { it > card }

        val allHandsEmpty = newHands.values.all { it.isEmpty() }
        val newStatus = if (allHandsEmpty) MindStatus.REVEALING else MindStatus.PLAYING

        return state.copy(
            playerHands = newHands,
            playedCards = newPlayedCards,
            pendingCards = newPending,
            status = newStatus
        )
    }

    fun resolveLevel(state: MindGameState): MindGameState {
        if (state.status != MindStatus.REVEALING) return state

        val errors = state.playedCards.count { !it.wasCorrect }
        val newLives = state.lives - errors

        val newStatus = if (newLives <= 0) {
            MindStatus.GAME_OVER
        } else {
            MindStatus.LEVEL_COMPLETE
        }

        return state.copy(
            lives = maxOf(newLives, 0),
            status = newStatus
        )
    }

    fun startNextLevel(state: MindGameState): MindGameState {
        if (state.status != MindStatus.LEVEL_COMPLETE) return state

        val nextLevel = state.level + 1
        val maxLevel = getMaxLevel(state.players.size)

        if (nextLevel > maxLevel) {
            return state.copy(status = MindStatus.VICTORY)
        }

        val hands = dealCards(state.players, nextLevel)
        val allCards = hands.values.flatten().sorted()

        return state.copy(
            level = nextLevel,
            playerHands = hands,
            playedCards = emptyList(),
            pendingCards = allCards,
            status = MindStatus.PLAYING
        )
    }

    fun getMaxLevel(playerCount: Int): Int = 12 - playerCount + 1

    private fun dealCards(players: List<Player>, level: Int): Map<String, List<Int>> {
        val totalCards = players.size * level
        val numbers = (1..100).shuffled().take(totalCards)

        val hands = mutableMapOf<String, List<Int>>()
        players.forEachIndexed { index, player ->
            val from = index * level
            val to = from + level
            hands[player.id] = numbers.subList(from, to).sorted()
        }
        return hands
    }
}
