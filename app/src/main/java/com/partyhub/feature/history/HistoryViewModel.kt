package com.partyhub.feature.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.partyhub.database.MatchDao
import com.partyhub.database.MatchHistory
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el historial de partidas.
 * Expone LiveData para la UI y métodos para insertar/borrar.
 */
class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    val allMatches: LiveData<List<MatchHistory>> = repository.allMatches

    fun insert(match: MatchHistory) = viewModelScope.launch {
        repository.saveMatch(match)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.clearLocalHistory()
    }

    fun syncWithCloud() = viewModelScope.launch {
        repository.syncFromFirestore()
    }
}

/**
 * Factory para instanciar el HistoryViewModel con su dependencia de HistoryRepository.
 */
class HistoryViewModelFactory(private val repository: HistoryRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
