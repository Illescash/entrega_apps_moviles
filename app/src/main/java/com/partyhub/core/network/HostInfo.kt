package com.partyhub.core.network

/**
 * Información de un host descubierto por UDP broadcast.
 */
data class HostInfo(
    val hostName: String,
    val ip: String,
    val port: Int
)
