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
class HistoryViewModel(private val matchDao: MatchDao) : ViewModel() {

    val allMatches: LiveData<List<MatchHistory>> = matchDao.getAllMatches()

    fun insert(match: MatchHistory) = viewModelScope.launch {
        matchDao.insert(match)
    }

    fun deleteAll() = viewModelScope.launch {
        matchDao.deleteAllMatches()
    }
}

/**
 * Factory para instanciar el HistoryViewModel con su dependencia de MatchDao.
 */
class HistoryViewModelFactory(private val matchDao: MatchDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(matchDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
