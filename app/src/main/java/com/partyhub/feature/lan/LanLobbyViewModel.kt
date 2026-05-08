package com.partyhub.feature.lan

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partyhub.core.network.*
import org.json.JSONObject
import timber.log.Timber

/**
 * ViewModel para el lobby LAN.
 * Gestiona el descubrimiento de salas, la creación de servidor y la conexión como cliente.
 */
class LanLobbyViewModel : ViewModel() {

    private val discovery = DiscoveryService()
    private var server: LanServer? = null
    private var client: LanClient? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    // -- Estado observable --

    private val _isHost = MutableLiveData(false)
    val isHost: LiveData<Boolean> get() = _isHost

    private val _connectedPlayers = MutableLiveData<List<String>>(emptyList())
    val connectedPlayers: LiveData<List<String>> get() = _connectedPlayers

    private val _availableHosts = MutableLiveData<List<HostInfo>>(emptyList())
    val availableHosts: LiveData<List<HostInfo>> get() = _availableHosts

    private val _selectedGame = MutableLiveData("the_mind")
    val selectedGame: LiveData<String> get() = _selectedGame

    private val _lobbyStatus = MutableLiveData(LobbyStatus.BROWSING)
    val lobbyStatus: LiveData<LobbyStatus> get() = _lobbyStatus

    private val _gameStartEvent = MutableLiveData<GameStartConfig?>()
    val gameStartEvent: LiveData<GameStartConfig?> get() = _gameStartEvent

    private val _errorEvent = MutableLiveData<String?>()
    val errorEvent: LiveData<String?> get() = _errorEvent

    private var hostName: String = "Host"
    private var localPlayerName: String = "Jugador"
    private var myPlayerId: Int = -1 // Índice del jugador local en la lista

    // -------------------------------------------------------
    // Acciones del Host
    // -------------------------------------------------------

    fun createRoom(playerName: String) {
        hostName = playerName
        localPlayerName = playerName
        _isHost.value = true
        _lobbyStatus.value = LobbyStatus.IN_ROOM
        _connectedPlayers.value = listOf(playerName)

        // Iniciar servidor TCP
        server?.stop()
        server = LanServer().apply {
            onClientConnected = { clientId ->
                Timber.d("LAN Lobby: cliente $clientId conectado al servidor")
            }
            onMessageReceived = { clientId, message ->
                handleServerMessage(clientId, message)
            }
            onClientDisconnected = { clientId, name ->
                mainHandler.post {
                    val updated = _connectedPlayers.value?.toMutableList() ?: mutableListOf()
                    updated.remove(name)
                    _connectedPlayers.value = updated
                    broadcastLobbyState()
                }
            }
            start()
        }

        // Iniciar anuncio UDP
        discovery.startAnnouncing(playerName, LanServer.TCP_PORT)
        Timber.d("LAN Lobby: sala creada por $playerName")
    }

    fun selectGame(gameId: String) {
        _selectedGame.value = gameId
        if (_isHost.value == true) {
            broadcastLobbyState()
        }
    }

    fun startGame() {
        val game = _selectedGame.value ?: "the_mind"
        val players = _connectedPlayers.value ?: return
        if (players.size < 2) {
            _errorEvent.value = "Se necesitan al menos 2 jugadores"
            return
        }

        // Parar el anuncio UDP
        discovery.stopAnnouncing()

        // Enviar GAME_START a todos los clientes
        val startMsg = NetworkMessage.createGameStart(game, players.size)
        server?.broadcast(startMsg)

        // Evento local para navegar al juego
        myPlayerId = 0 // El host siempre es el jugador 0
        _gameStartEvent.value = GameStartConfig(
            game = game,
            playerCount = players.size,
            playerNames = players,
            localPlayerId = hostName,
            isHost = true
        )
    }

    // -------------------------------------------------------
    // Acciones del Cliente
    // -------------------------------------------------------

    private val knownHosts = mutableMapOf<String, HostInfo>()

    fun startBrowsing() {
        _lobbyStatus.value = LobbyStatus.BROWSING
        knownHosts.clear()
        _availableHosts.value = emptyList()

        discovery.startListening { hostInfo ->
            mainHandler.post {
                if (hostInfo.lastSeen == -1L) {
                    knownHosts.remove(hostInfo.ip)
                } else {
                    knownHosts[hostInfo.ip] = hostInfo
                }
                _availableHosts.value = knownHosts.values.toList()
            }
        }
        
        // Iniciar podadora de salas fantasma
        startPruning()
    }

    fun stopBrowsing() {
        discovery.stopListening()
        stopPruning()
    }

    private fun startPruning() {
        stopPruning()
        mainHandler.postDelayed(pruneRunnable, 3000)
    }

    private fun stopPruning() {
        mainHandler.removeCallbacks(pruneRunnable)
    }

    private val pruneRunnable: Runnable = object : Runnable {
        override fun run() {
            pruneStaleHosts()
            mainHandler.postDelayed(this, 3000)
        }
    }

    private fun pruneStaleHosts() {
        val now = System.currentTimeMillis()
        val beforeCount = knownHosts.size
        val iterator = knownHosts.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (now - entry.value.lastSeen > 4000) {
                iterator.remove()
            }
        }
        if (knownHosts.size != beforeCount) {
            _availableHosts.value = knownHosts.values.toList()
            Timber.d("LAN Lobby: salas fantasma eliminadas. Quedan: ${knownHosts.size}")
        }
    }

    fun joinRoom(hostInfo: HostInfo, playerName: String) {
        localPlayerName = playerName
        _isHost.value = false
        _lobbyStatus.value = LobbyStatus.CONNECTING

        discovery.stopListening()

        client?.disconnect()
        client = LanClient().apply {
            onMessageReceived = { message ->
                handleClientMessage(message)
            }
            onDisconnected = {
                mainHandler.post {
                    _lobbyStatus.value = LobbyStatus.BROWSING
                    _errorEvent.value = "Se ha perdido la conexión con el anfitrión"
                }
            }
            
            // Conexión en hilo de fondo
            Thread {
                try {
                    connect(hostInfo.ip, hostInfo.port)
                    // Una vez conectado, enviamos el JOIN
                    send(NetworkMessage.createJoin(playerName))
                    Timber.d("LAN Lobby: conexión establecida y mensaje JOIN enviado")
                } catch (e: Exception) {
                    Timber.e(e, "Error al conectar con el host")
                    mainHandler.post {
                        _lobbyStatus.value = LobbyStatus.BROWSING
                        _errorEvent.value = "No se pudo conectar con la sala"
                    }
                }
            }.start()
        }
    }

    // -------------------------------------------------------
    // Procesamiento de mensajes
    // -------------------------------------------------------

    private fun handleServerMessage(clientId: Int, raw: String) {
        try {
            val msg = NetworkMessage.parse(raw)
            when (NetworkMessage.getType(msg)) {
                NetworkMessage.TYPE_JOIN -> {
                    val name = msg.getString("playerName")
                    server?.setPlayerName(clientId, name)
                    mainHandler.post {
                        val updated = _connectedPlayers.value?.toMutableList() ?: mutableListOf()
                        if (!updated.contains(name)) {
                            updated.add(name)
                            _connectedPlayers.value = updated
                        }
                        broadcastLobbyState()
                    }
                }
                NetworkMessage.TYPE_LEAVE -> {
                    val name = msg.getString("playerName")
                    mainHandler.post {
                        val updated = _connectedPlayers.value?.toMutableList() ?: mutableListOf()
                        updated.remove(name)
                        _connectedPlayers.value = updated
                        broadcastLobbyState()
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Error parseando mensaje del cliente $clientId")
        }
    }

    private fun handleClientMessage(raw: String) {
        try {
            val msg = NetworkMessage.parse(raw)
            when (NetworkMessage.getType(msg)) {
                NetworkMessage.TYPE_LOBBY -> {
                    val players = mutableListOf<String>()
                    val arr = msg.getJSONArray("players")
                    for (i in 0 until arr.length()) {
                        players.add(arr.getString(i))
                    }
                    val game = msg.optString("game", "the_mind")
                    mainHandler.post {
                        _connectedPlayers.value = players
                        _selectedGame.value = game
                        _lobbyStatus.value = LobbyStatus.IN_ROOM
                    }
                }
                NetworkMessage.TYPE_GAME_START -> {
                    val game = msg.getString("game")
                    val playerCount = msg.getInt("playerCount")
                    val players = _connectedPlayers.value ?: emptyList()
                    
                    mainHandler.post {
                        // Usar el nombre como ID para que coincida con el motor del juego
                        val localId = localPlayerName
                        _gameStartEvent.value = GameStartConfig(
                            game = game,
                            playerCount = playerCount,
                            playerNames = players,
                            localPlayerId = localId,
                            isHost = false
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Timber.w(e, "Error parseando mensaje del host")
        }
    }

    private fun broadcastLobbyState() {
        val players = _connectedPlayers.value ?: return
        val game = _selectedGame.value ?: "the_mind"
        val msg = NetworkMessage.createLobby(players, game, hostName)
        Timber.d("LAN Lobby: haciendo broadcast del estado: $msg")
        server?.broadcast(msg)
    }

    // -------------------------------------------------------
    // Acceso a la red para los fragments de juego
    // -------------------------------------------------------

    fun getServer(): LanServer? = server
    fun getClient(): LanClient? = client
    fun getLocalPlayerId(): String = localPlayerName
    fun getLocalPlayerName(): String = localPlayerName

    fun consumeGameStartEvent() {
        _gameStartEvent.value = null
    }

    fun consumeErrorEvent() {
        _errorEvent.value = null
    }

    // -------------------------------------------------------
    // Limpieza
    // -------------------------------------------------------

    fun leaveRoom() {
        if (_isHost.value == true) {
            server?.broadcast(NetworkMessage.createLeave(hostName))
            server?.stop()
            server = null
            discovery.stopAnnouncing(hostName, LanServer.TCP_PORT)
        } else {
            client?.send(NetworkMessage.createLeave(localPlayerName))
            client?.disconnect()
            client = null
        }
        discovery.stopListening()
        stopPruning()
        mainHandler.post {
            _lobbyStatus.value = LobbyStatus.BROWSING
            _connectedPlayers.value = emptyList()
        }
    }

    override fun onCleared() {
        super.onCleared()
        discovery.stopAll()
        stopPruning()
        server?.stop()
        client?.disconnect()
        Timber.d("LAN Lobby: ViewModel cleared, todos los recursos de red liberados")
    }
}

enum class LobbyStatus {
    BROWSING,    // Buscando salas
    CONNECTING,  // Conectándose a una sala
    IN_ROOM      // Dentro de una sala
}

data class GameStartConfig(
    val game: String,
    val playerCount: Int,
    val playerNames: List<String>,
    val localPlayerId: String,
    val isHost: Boolean
)
