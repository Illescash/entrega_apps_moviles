package com.partyhub.core.network

import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Servidor TCP para el modo LAN. Solo el HOST crea una instancia.
 *
 * - Acepta conexiones entrantes en un puerto fijo.
 * - Mantiene una lista thread-safe de clientes conectados.
 * - Permite broadcast a todos o envío individual.
 */
class LanServer(private val port: Int = TCP_PORT) {

    companion object {
        const val TCP_PORT = 9999
    }

    /**
     * Representa una conexión con un cliente.
     */
    data class ClientConnection(
        val id: Int,
        val socket: Socket,
        val writer: PrintWriter,
        var playerName: String = ""
    )

    private var serverSocket: ServerSocket? = null
    private val clients = CopyOnWriteArrayList<ClientConnection>()

    @Volatile
    private var running = false
    private var nextClientId = 0

    var onMessageReceived: ((clientId: Int, message: String) -> Unit)? = null
    var onClientConnected: ((clientId: Int) -> Unit)? = null
    var onClientDisconnected: ((clientId: Int, playerName: String) -> Unit)? = null

    /**
     * Arranca el servidor. Debe llamarse desde un hilo de IO.
     */
    fun start() {
        running = true
        val thread = Thread {
            try {
                serverSocket = ServerSocket(port)
                Timber.d("LAN Server: escuchando en puerto TCP $port")

                while (running) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        val clientId = nextClientId++
                        val writer = PrintWriter(clientSocket.getOutputStream(), true)

                        val connection = ClientConnection(
                            id = clientId,
                            socket = clientSocket,
                            writer = writer
                        )
                        clients.add(connection)
                        onClientConnected?.invoke(clientId)

                        Timber.d("LAN Server: cliente $clientId conectado desde ${clientSocket.inetAddress}")

                        // Hilo de lectura para este cliente
                        startClientReader(connection)

                    } catch (e: Exception) {
                        if (running) Timber.w(e, "Error aceptando cliente")
                    }
                }
            } catch (e: Exception) {
                if (running) Timber.e(e, "Error iniciando servidor TCP")
            }
        }
        thread.isDaemon = true
        thread.name = "LAN-Server"
        thread.start()
    }

    private fun startClientReader(connection: ClientConnection) {
        val thread = Thread {
            try {
                val reader = BufferedReader(InputStreamReader(connection.socket.getInputStream()))
                var line: String?
                while (running && reader.readLine().also { line = it } != null) {
                    line?.let { msg ->
                        Timber.d("LAN Server: recibido de cliente ${connection.id}: $msg")
                        onMessageReceived?.invoke(connection.id, msg)
                    }
                }
            } catch (e: Exception) {
                if (running) Timber.w("LAN Server: cliente ${connection.id} desconectado")
            } finally {
                removeClient(connection)
            }
        }
        thread.isDaemon = true
        thread.name = "LAN-Client-${connection.id}-Reader"
        thread.start()
    }

    /**
     * Envía un mensaje a todos los clientes conectados.
     */
    fun broadcast(message: String) {
        clients.forEach { client ->
            try {
                client.writer.println(message)
            } catch (e: Exception) {
                Timber.w(e, "Error enviando a cliente ${client.id}")
                removeClient(client)
            }
        }
    }

    /**
     * Envía un mensaje a un cliente específico.
     */
    fun sendTo(clientId: Int, message: String) {
        val client = clients.find { it.id == clientId } ?: return
        try {
            client.writer.println(message)
        } catch (e: Exception) {
            Timber.w(e, "Error enviando a cliente $clientId")
            removeClient(client)
        }
    }

    /**
     * Asocia un nombre de jugador a un cliente.
     */
    fun setPlayerName(clientId: Int, name: String) {
        clients.find { it.id == clientId }?.playerName = name
    }

    fun getPlayerNames(): List<String> = clients.map { it.playerName }.filter { it.isNotEmpty() }

    fun getClientCount(): Int = clients.size

    fun getClientIds(): List<Int> = clients.map { it.id }

    private fun removeClient(connection: ClientConnection) {
        if (clients.remove(connection)) {
            val name = connection.playerName
            try {
                connection.socket.close()
            } catch (_: Exception) {}
            onClientDisconnected?.invoke(connection.id, name)
            Timber.d("LAN Server: cliente ${connection.id} ($name) eliminado")
        }
    }

    fun stop() {
        running = false
        clients.forEach { client ->
            try { client.socket.close() } catch (_: Exception) {}
        }
        clients.clear()
        try { serverSocket?.close() } catch (_: Exception) {}
        serverSocket = null
        Timber.d("LAN Server: detenido")
    }
}
