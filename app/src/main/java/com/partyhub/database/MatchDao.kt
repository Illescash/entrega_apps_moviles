package com.partyhub.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) para la tabla match_history.
 * 
 * Incluye operaciones asíncronas (suspend) para escritura y 
 * LiveData para observación reactiva de los datos.
 */
@Dao
interface MatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: MatchHistory)

    @Query("SELECT * FROM match_history ORDER BY finishedAt DESC")
    fun getAllMatches(): LiveData<List<MatchHistory>>

    @Query("DELETE FROM match_history")
    suspend fun deleteAllMatches()
}
