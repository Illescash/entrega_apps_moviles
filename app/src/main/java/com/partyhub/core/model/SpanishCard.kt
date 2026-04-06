package com.partyhub.core.model

data class SpanishCard(
    val number: Int, // 1-7, 10-12
    val suit: Suit
) {
    enum class Suit {
        OROS, COPAS, ESPADAS, BASTOS
    }
    
    val value: Int get() = number // Simple value for comparison
}
