package com.partyhub.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpanishCard(
    val number: Int, // 1-7, 10-12
    val suit: Suit
) : Parcelable {
    @Parcelize
    enum class Suit : Parcelable {
        OROS, COPAS, ESPADAS, BASTOS
    }
    
    val value: Int get() = number // Simple value for comparison
}
