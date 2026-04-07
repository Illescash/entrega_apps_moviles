package com.partyhub.feature.themind

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partyhub.feature.themind.engine.MindGameEngine
import com.partyhub.feature.themind.engine.MindGameState
import timber.log.Timber

class MindViewModel : ViewModel() {

    private val engine = MindGameEngine()

    private val _gameState = MutableLiveData<MindGameState>()
    val gameState: LiveData<MindGameState> get() = _gameState

    fun startGame(playerNames: List<String>, lives: Int = 3) {
        Timber.d("The Mind: iniciando partida con ${playerNames.size} jugadores, $lives vidas")
        _gameState.value = engine.newGame(playerNames, lives)
    }

    fun playCard(playerId: String) {
        val current = _gameState.value ?: return
        Timber.d("The Mind: jugador $playerId juega carta")
        _gameState.value = engine.playCard(current, playerId)
    }

    fun resolveLevel() {
        val current = _gameState.value ?: return
        Timber.d("The Mind: resolviendo nivel ${current.level}")
        _gameState.value = engine.resolveLevel(current)
    }

    fun nextLevel() {
        val current = _gameState.value ?: return
        Timber.d("The Mind: avanzando al nivel ${current.level + 1}")
        _gameState.value = engine.startNextLevel(current)
    }
}
