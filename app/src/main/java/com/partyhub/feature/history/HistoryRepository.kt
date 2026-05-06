package com.partyhub.feature.history

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.partyhub.database.MatchDao
import com.partyhub.database.MatchHistory
import kotlinx.coroutines.tasks.await

/**
 * Repositorio que gestiona el historial de partidas combinando persistencia local (Room)
 * y sincronización remota (Firestore).
 */
class HistoryRepository(private val matchDao: MatchDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val allMatches: LiveData<List<MatchHistory>> = matchDao.getAllMatches()

    /**
     * Guarda una partida en local y la sincroniza con Firestore si el usuario está autenticado.
     */
    suspend fun saveMatch(match: MatchHistory) {
        // 1. Guardar en Room local
        matchDao.insert(match)

        // 2. Sincronizar con Firestore si hay usuario logueado
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                firestore.collection("users")
                    .document(userId)
                    .collection("matches")
                    .add(match)
                    .await()
            } catch (e: Exception) {
                // En una app real aquí gestionaríamos una cola de sincronización pendiente
                e.printStackTrace()
            }
        }
    }

    /**
     * Descarga el historial desde Firestore y lo inserta en Room.
     * Se llama típicamente al iniciar sesión.
     */
    suspend fun syncFromFirestore() {
        val userId = auth.currentUser?.uid ?: return
        
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("matches")
                .get()
                .await()

            val remoteMatches = snapshot.toObjects(MatchHistory::class.java)
            
            // 1. Limpiar historial local para evitar duplicados en el volcado
            matchDao.deleteAllMatches()

            // 2. Insertar los datos remotos en la base de datos local
            for (match in remoteMatches) {
                matchDao.insert(match.copy(id = 0)) 
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearLocalHistory() {
        matchDao.deleteAllMatches()
    }
}
