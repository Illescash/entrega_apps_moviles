package com.partyhub.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa el historial de una partida finalizada.
 * 
 * Se almacenan los datos básicos para la persistencia local con Room.
 * Nota: Los jugadores se guardan como un String separado por comas para simplificar,
 * tal y como se especifica en el plan de la Entrega 3.
 */
@Entity(tableName = "match_history")
data class MatchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameName: String = "",
    val players: String = "",
    val winner: String = "",
    val durationMs: Long = 0,
    val finishedAt: Long = System.currentTimeMillis()
)
