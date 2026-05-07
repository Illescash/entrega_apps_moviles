package com.partyhub.core

/**
 * Clase envoltorio para datos que se exponen a través de LiveData y que deben representarse como un evento.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Devuelve el contenido y evita su uso futuro.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Devuelve el contenido, incluso si ya ha sido manejado.
     */
    fun peekContent(): T = content
}
