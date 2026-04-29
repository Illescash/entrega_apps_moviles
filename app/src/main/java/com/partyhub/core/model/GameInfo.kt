package com.partyhub.core.model

import androidx.annotation.DrawableRes

/**
 * Información básica de un juego para mostrar en el Hub.
 * Se usa para alimentar el RecyclerView de la pantalla principal.
 */
data class GameInfo(
    val id: String,
    val name: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val minPlayers: Int,
    val maxPlayers: Int
)
