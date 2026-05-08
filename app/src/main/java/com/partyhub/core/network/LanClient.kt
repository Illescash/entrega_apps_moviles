package com.partyhub.core.network

import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

/**
 * Cliente TCP para conectar al Host en modo LAN.
 *
 * - Se conecta a la IP y puerto del Host.
 * - Escucha mensajes en un hilo dedicado.
 * - Permite enviar mensajes al Host.
 */
class LanClient {

    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    @Volatile
    private var connected = false

    var onMessageReceived: ((message: String) -> Unit)? = null
    var onDisconnected: (() -> Unit)? = null

    /**
     * Conecta al Host. Debe llamarse desde un hilo de IO.
     */
    fun connect(ip: String, port: Int) {
        disconnect()
        try {
            socket = Socket(ip, port)
            writer = PrintWriter(socket!!.getOutputStream(), true)
            connected = true

            Timber.d("LAN Client: conectado a $ip:$port")

            // Hilo de lectura (este sí debe ser separado para no bloquear)
            val readerThread = Thread {
                try {
                    val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                    var line: String? = null
                    while (connected && reader.readLine().also { line = it } != null) {
                        line?.let { msg ->
                            Timber.d("LAN Client: recibido: $msg")
                            onMessageReceived?.invoke(msg)
                        }
                    }
                } catch (e: Exception) {
                    if (connected) Timber.w(e, "LAN Client: error en hilo de lectura")
                } finally {
                    connected = false
                    onDisconnected?.invoke()
                    Timber.d("LAN Client: desconectado")
                }
            }
            readerThread.isDaemon = true
            readerThread.name = "LAN-Client-Reader"
            readerThread.start()

        } catch (e: Exception) {
            connected = false
            Timber.e(e, "LAN Client: error al establecer conexión inicial")
            throw e // Re-lanzar para que el ViewModel sepa que falló
        }
    }

    /**
     * Envía un mensaje al Host.
     */
    fun send(message: String) {
        Thread {
            if (!connected) return@Thread
            try {
                writer?.println(message)
            } catch (e: Exception) {
                Timber.w(e, "LAN Client: error enviando mensaje")
                disconnect()
            }
        }.start()
    }

    fun isConnected(): Boolean = connected

    fun disconnect() {
        connected = false
        try { socket?.close() } catch (_: Exception) {}
        socket = null
        writer = null
        Timber.d("LAN Client: cerrado")
    }
}
