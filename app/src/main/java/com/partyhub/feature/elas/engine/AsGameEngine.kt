package com.partyhub.feature.elas.engine

import com.partyhub.core.model.Player
import com.partyhub.core.model.SpanishCard

class AsGameEngine {

    fun generateDeck(): List<SpanishCard> {
        val suits = SpanishCard.Suit.values()
        val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 10, 11, 12)
        return suits.flatMap { suit ->
            numbers.map { SpanishCard(it, suit) }
        }.shuffled()
    }

    fun startNewGame(playerCount: Int): AsGameState {
        val deck = generateDeck().toMutableList()
        val players = (0 until playerCount).map {
            AsPlayer(
                player = Player(it.toString(), "Jugador ${it + 1}"),
                hand = deck.removeAt(0),
                lives = 3
            )
        }
        return AsGameState(players, deck, 0, AsStatus.WAITING_ACTION)
    }

    fun swap(state: AsGameState): AsGameState {
        val currIdx = state.currentPlayerIndex
        val players = state.players.toMutableList()
        val deck = state.deck.toMutableList()
        
        val currHand = players[currIdx].hand ?: return state
        
        // El último jugador intercambia con la baraja.
        if (currIdx == players.size - 1) {
            val nextHand = if (deck.isNotEmpty()) deck.removeAt(0) else currHand
            players[currIdx] = players[currIdx].copy(hand = nextHand)
        } else {
            // Un jugador normal intercambia con el siguiente.
            val nextHand = players[currIdx + 1].hand ?: return state
            
            // Un Rey (12) no deja pasar.
            if (nextHand.number == 12) {
                return state.copy(lastAction = "¡${players[currIdx+1].player.name} no deja pasar!")
            }

            players[currIdx] = players[currIdx].copy(hand = nextHand)
            players[currIdx + 1] = players[currIdx + 1].copy(hand = currHand)
        }

        return nextTurn(state.copy(players = players, deck = deck))
    }

    fun stay(state: AsGameState): AsGameState {
        return nextTurn(state)
    }

    private fun nextTurn(state: AsGameState): AsGameState {
        val nextIdx = state.currentPlayerIndex + 1
        if (nextIdx >= state.players.size) {
            return state.copy(status = AsStatus.REVEALING)
        }
        return state.copy(currentPlayerIndex = nextIdx)
    }

    fun resolveRound(state: AsGameState): AsGameState {
        if (state.status != AsStatus.REVEALING) return state
        
        val lowestValue = state.players.filter { !it.isOut }.minByOrNull { it.hand?.number ?: 99 }?.hand?.number ?: 99
        
        val newPlayers = state.players.map { player ->
            if (!player.isOut && player.hand?.number == lowestValue) {
                val newLives = player.lives - 1
                player.copy(lives = newLives, isOut = newLives <= 0)
            } else {
                player
            }
        }

        val alivePlayers = newPlayers.count { !it.isOut }
        val newStatus = if (alivePlayers <= 1) AsStatus.GAME_OVER else AsStatus.ROUND_OVER
        
        return state.copy(players = newPlayers, status = newStatus)
    }

    fun nextRound(state: AsGameState): AsGameState {
        val deck = generateDeck().toMutableList()
        val newPlayers = state.players.map { 
            if (it.isOut) it.copy(hand = null) 
            else it.copy(hand = deck.removeAt(0)) 
        }
        return state.copy(
            players = newPlayers,
            deck = deck,
            currentPlayerIndex = newPlayers.indexOfFirst { !it.isOut },
            status = AsStatus.WAITING_ACTION
        )
    }
}
