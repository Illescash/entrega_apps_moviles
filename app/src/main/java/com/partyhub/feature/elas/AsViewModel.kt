package com.partyhub.feature.elas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partyhub.feature.elas.engine.AsGameEngine
import com.partyhub.feature.elas.engine.AsGameState
import com.partyhub.feature.elas.engine.AsStatus

class AsViewModel : ViewModel() {

    private val engine = AsGameEngine()
    private val _gameState = MutableLiveData<AsGameState>()
    val gameState: LiveData<AsGameState> get() = _gameState

    fun startGame(playerCount: Int) {
        _gameState.value = engine.startNewGame(playerCount)
    }

    fun swap() {
        val current = _gameState.value ?: return
        _gameState.value = engine.swap(current)
    }

    fun stay() {
        val current = _gameState.value ?: return
        _gameState.value = engine.stay(current)
    }

    fun resolveRound() {
        val current = _gameState.value ?: return
        _gameState.value = engine.resolveRound(current)
    }

    fun nextRound() {
        val current = _gameState.value ?: return
        _gameState.value = engine.nextRound(current)
    }
}
