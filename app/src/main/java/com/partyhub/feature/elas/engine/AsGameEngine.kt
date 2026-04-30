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
        return AsGameState(players, deck, 0, AsStatus.WAITING_ACTION, roundStarterIndex = 0)
    }

    fun swap(state: AsGameState): AsGameState {
        val currIdx = state.currentPlayerIndex
        val players = state.players.toMutableList()
        val deck = state.deck.toMutableList()
        
        val currHand = players[currIdx].hand ?: return state
        
        val activePlayersCount = players.count { !it.isOut }
        val isLastTurn = state.turnsPlayedInRound == activePlayersCount - 1
        
        // El último jugador intercambia con la baraja.
        if (isLastTurn) {
            val nextHand = if (deck.isNotEmpty()) deck.removeAt(0) else currHand
            players[currIdx] = players[currIdx].copy(hand = nextHand)
        } else {
            // Un jugador normal intercambia con el siguiente activo.
            var nextIdx = (currIdx + 1) % players.size
            while (players[nextIdx].isOut) {
                nextIdx = (nextIdx + 1) % players.size
            }
            
            val nextHand = players[nextIdx].hand ?: return state
            
            // Un Rey (12) no deja pasar.
            if (nextHand.number == 12) {
                return state.copy(lastAction = "¡${players[nextIdx].player.name} no deja pasar!")
            }

            players[currIdx] = players[currIdx].copy(hand = nextHand)
            players[nextIdx] = players[nextIdx].copy(hand = currHand)
        }

        return nextTurn(state.copy(players = players, deck = deck))
    }

    fun stay(state: AsGameState): AsGameState {
        return nextTurn(state)
    }

    private fun nextTurn(state: AsGameState): AsGameState {
        val activePlayersCount = state.players.count { !it.isOut }
        val nextTurnsPlayed = state.turnsPlayedInRound + 1

        if (nextTurnsPlayed >= activePlayersCount) {
            return state.copy(status = AsStatus.REVEALING, turnsPlayedInRound = nextTurnsPlayed)
        }

        var nextIdx = (state.currentPlayerIndex + 1) % state.players.size
        while (state.players[nextIdx].isOut) {
            nextIdx = (nextIdx + 1) % state.players.size
        }
        
        return state.copy(currentPlayerIndex = nextIdx, turnsPlayedInRound = nextTurnsPlayed)
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
        val nextStarterIdx = (state.roundStarterIndex + 1) % state.players.size
        
        // Buscamos al siguiente jugador que no esté fuera, empezando por el siguiente titular
        var actualStarter = nextStarterIdx
        while (newPlayers[actualStarter].isOut) {
            actualStarter = (actualStarter + 1) % newPlayers.size
        }

        return state.copy(
            players = newPlayers,
            deck = deck,
            currentPlayerIndex = actualStarter,
            roundStarterIndex = nextStarterIdx,
            turnsPlayedInRound = 0,
            status = AsStatus.WAITING_ACTION
        )
    }
}
