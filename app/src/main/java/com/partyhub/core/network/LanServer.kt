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
                serverSocket = ServerSocket(port).apply {
                    reuseAddress = true
                }
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
                var line: String? = null
                while (running) {
                    line = reader.readLine()
                    if (line == null) break // Conexión cerrada por el cliente
                    
                    val msg = line!!
                    Timber.d("LAN Server: recibido de cliente ${connection.id}: $msg")
                    onMessageReceived?.invoke(connection.id, msg)
                }
            } catch (e: Exception) {
                if (running) Timber.e(e, "LAN Server: error leyendo del cliente ${connection.id}")
            } finally {
                Timber.d("LAN Server: cerrando conexión con cliente ${connection.id}")
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
        Thread {
            clients.forEach { client ->
                try {
                    client.writer.println(message)
                } catch (e: Exception) {
                    Timber.w(e, "Error enviando a cliente ${client.id}")
                    removeClient(client)
                }
            }
        }.start()
    }

    /**
     * Envía un mensaje a un cliente específico.
     */
    fun sendTo(clientId: Int, message: String) {
        Thread {
            val client = clients.find { it.id == clientId } ?: return@Thread
            try {
                client.writer.println(message)
            } catch (e: Exception) {
                Timber.w(e, "Error enviando a cliente $clientId")
                removeClient(client)
            }
        }.start()
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

    /**
     * Devuelve una lista de pares (ID de conexión, Nombre del jugador)
     */
    fun getConnectedClientsInfo(): List<Pair<Int, String>> = clients.map { it.id to it.playerName }

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
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Timber.w(e, "Error cerrando server socket")
        }
        serverSocket = null
        
        clients.forEach { client ->
            try {
                client.socket.close()
            } catch (e: Exception) {
                // Ya cerrado o error
            }
        }
        clients.clear()
        Timber.d("LAN Server: detenido")
    }
}
