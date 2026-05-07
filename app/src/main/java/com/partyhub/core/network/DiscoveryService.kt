package com.partyhub.core.network

import org.json.JSONObject
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Servicio de descubrimiento de salas LAN mediante UDP broadcast.
 *
 * - El **Host** usa [startAnnouncing] para enviar paquetes cada 2 segundos.
 * - Los **Clientes** usan [startListening] para detectar hosts en la red.
 */
class DiscoveryService {

    companion object {
        const val DISCOVERY_PORT = 8888
        const val APP_IDENTIFIER = "partyhub"
        private const val ANNOUNCE_INTERVAL_MS = 2000L
        private const val BUFFER_SIZE = 1024
    }

    @Volatile
    private var announcing = false

    @Volatile
    private var listening = false

    private var announceSocket: DatagramSocket? = null
    private var listenSocket: DatagramSocket? = null

    /**
     * Empieza a anunciar la sala del Host por UDP broadcast.
     * Debe llamarse desde un hilo de IO.
     */
    fun startAnnouncing(hostName: String, tcpPort: Int) {
        announcing = true
        val thread = Thread {
            try {
                announceSocket = DatagramSocket().apply {
                    broadcast = true
                }
                val message = JSONObject().apply {
                    put("app", APP_IDENTIFIER)
                    put("host", hostName)
                    put("port", tcpPort)
                }.toString()

                val data = message.toByteArray()
                val broadcastAddress = InetAddress.getByName("255.255.255.255")
                val packet = DatagramPacket(data, data.size, broadcastAddress, DISCOVERY_PORT)

                Timber.d("LAN Discovery: anunciando como '$hostName' en puerto TCP $tcpPort")

                while (announcing) {
                    try {
                        announceSocket?.send(packet)
                    } catch (e: Exception) {
                        if (announcing) Timber.w(e, "Error enviando anuncio UDP")
                    }
                    Thread.sleep(ANNOUNCE_INTERVAL_MS)
                }
            } catch (e: Exception) {
                if (announcing) Timber.e(e, "Error en hilo de anuncio UDP")
            } finally {
                announceSocket?.close()
                announceSocket = null
            }
        }
        thread.isDaemon = true
        thread.name = "LAN-Announcer"
        thread.start()
    }

    /**
     * Empieza a escuchar anuncios UDP de hosts en la red.
     * Llama al [onHostFound] cada vez que detecta un host (puede repetirse).
     */
    fun startListening(onHostFound: (HostInfo) -> Unit) {
        listening = true
        val thread = Thread {
            try {
                listenSocket = DatagramSocket(DISCOVERY_PORT).apply {
                    reuseAddress = true
                    soTimeout = 3000
                }
                val buffer = ByteArray(BUFFER_SIZE)
                Timber.d("LAN Discovery: escuchando en puerto UDP $DISCOVERY_PORT")

                while (listening) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        listenSocket?.receive(packet)

                        val message = String(packet.data, 0, packet.length)
                        val json = JSONObject(message)

                        if (json.optString("app") == APP_IDENTIFIER) {
                            val hostInfo = HostInfo(
                                hostName = json.getString("host"),
                                ip = packet.address.hostAddress ?: "unknown",
                                port = json.getInt("port")
                            )
                            onHostFound(hostInfo)
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        // Timeout normal, seguimos escuchando
                    } catch (e: Exception) {
                        if (listening) Timber.w(e, "Error recibiendo paquete UDP")
                    }
                }
            } catch (e: Exception) {
                if (listening) Timber.e(e, "Error en hilo de escucha UDP")
            } finally {
                listenSocket?.close()
                listenSocket = null
            }
        }
        thread.isDaemon = true
        thread.name = "LAN-Listener"
        thread.start()
    }

    fun stopAnnouncing() {
        announcing = false
        announceSocket?.close()
    }

    fun stopListening() {
        listening = false
        listenSocket?.close()
    }

    fun stopAll() {
        stopAnnouncing()
        stopListening()
    }
}
