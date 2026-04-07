package com.partyhub.feature.elas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partyhub.feature.elas.engine.AsGameEngine
import com.partyhub.feature.elas.engine.AsGameState
import com.partyhub.feature.elas.engine.AsStatus
import timber.log.Timber

class AsViewModel : ViewModel() {

    private val engine = AsGameEngine()
    private val _gameState = MutableLiveData<AsGameState>()
    val gameState: LiveData<AsGameState> get() = _gameState

    fun startGame(playerCount: Int) {
        Timber.d("El As: iniciando partida con $playerCount jugadores")
        _gameState.value = engine.startNewGame(playerCount)
    }

    fun swap() {
        val current = _gameState.value ?: return
        Timber.d("El As: jugador ${current.players[current.currentPlayerIndex].player.name} intercambia carta")
        _gameState.value = engine.swap(current)
    }

    fun stay() {
        val current = _gameState.value ?: return
        Timber.d("El As: jugador ${current.players[current.currentPlayerIndex].player.name} se queda")
        _gameState.value = engine.stay(current)
    }

    fun resolveRound() {
        val current = _gameState.value ?: return
        Timber.d("El As: resolviendo ronda")
        _gameState.value = engine.resolveRound(current)
        if (_gameState.value?.status == AsStatus.GAME_OVER) {
            val winner = _gameState.value?.players?.firstOrNull { !it.isOut }?.player?.name
            Timber.d("El As: partida terminada, ganador: $winner")
        }
    }

    fun nextRound() {
        val current = _gameState.value ?: return
        Timber.d("El As: iniciando nueva ronda")
        _gameState.value = engine.nextRound(current)
    }
}
