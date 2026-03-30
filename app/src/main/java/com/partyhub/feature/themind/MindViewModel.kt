package com.partyhub.feature.themind

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partyhub.feature.themind.engine.MindGameEngine
import com.partyhub.feature.themind.engine.MindGameState

class MindViewModel : ViewModel() {

    private val engine = MindGameEngine()

    private val _gameState = MutableLiveData<MindGameState>()
    val gameState: LiveData<MindGameState> get() = _gameState

    fun startGame(playerNames: List<String>, lives: Int = 3) {
        _gameState.value = engine.newGame(playerNames, lives)
    }

    fun playCard(playerId: String) {
        val current = _gameState.value ?: return
        _gameState.value = engine.playCard(current, playerId)
    }

    fun resolveLevel() {
        val current = _gameState.value ?: return
        _gameState.value = engine.resolveLevel(current)
    }

    fun nextLevel() {
        val current = _gameState.value ?: return
        _gameState.value = engine.startNextLevel(current)
    }
}
