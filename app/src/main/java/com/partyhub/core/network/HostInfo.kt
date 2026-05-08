package com.partyhub.core.network

/**
 * Información de un host descubierto por UDP broadcast.
 */
data class HostInfo(
    val hostName: String,
    val ip: String,
    val port: Int,
    val lastSeen: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HostInfo) return false
        return ip == other.ip && port == other.port
    }

    override fun hashCode(): Int {
        var result = ip.hashCode()
        result = 31 * result + port
        return result
    }
}
