package com.partyhub.feature.themind

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partyhub.core.network.LanClient
import com.partyhub.core.network.LanServer
import com.partyhub.core.network.NetworkMessage
import com.partyhub.feature.themind.engine.MindGameEngine
import com.partyhub.feature.themind.engine.MindGameState
import com.partyhub.feature.themind.engine.MindStatus
import com.partyhub.feature.themind.engine.PlayedCard
import org.json.JSONObject
import timber.log.Timber

class MindViewModel : ViewModel() {

    private val engine = MindGameEngine()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _gameState = MutableLiveData<MindGameState>()
    val gameState: LiveData<MindGameState> get() = _gameState

    private val _errorEvent = MutableLiveData<com.partyhub.core.Event<String>>()
    val errorEvent: LiveData<com.partyhub.core.Event<String>> get() = _errorEvent

    // Mano privada del jugador local (solo en modo LAN)
    private val _localHand = MutableLiveData<List<Int>>()
    val localHand: LiveData<List<Int>> get() = _localHand

    // Modo LAN
    private var isLanMode = false
    private var isHost = false
    private var server: LanServer? = null
    private var client: LanClient? = null
    private var localPlayerId: Int = -1

    // -------------------------------------------------------
    // Modo local (sin cambios respecto al original)
    // -------------------------------------------------------

    fun startGame(playerNames: List<String>, lives: Int = 3) {
        Timber.d("The Mind: iniciando partida con ${playerNames.size} jugadores, $lives vidas")
        _gameState.value = engine.newGame(playerNames, lives)
    }

    fun playCard(playerId: String) {
        if (isLanMode) {
            // En modo LAN, enviar la acción al host
            if (isHost) {
                handlePlayCardOnHost(playerId)
            } else {
                client?.send(NetworkMessage.createAction(NetworkMessage.ACTION_PLAY_CARD, playerId))
            }
        } else {
            val current = _gameState.value ?: return
            Timber.d("The Mind: jugador $playerId juega carta")
            _gameState.value = engine.playCard(current, playerId)
        }
    }

    fun resolveLevel() {
        if (isLanMode) {
            if (isHost) {
                handleResolveLevelOnHost()
            } else {
                client?.send(NetworkMessage.createAction(NetworkMessage.ACTION_RESOLVE))
            }
        } else {
            val current = _gameState.value ?: return
            Timber.d("The Mind: resolviendo nivel ${current.level}")
            _gameState.value = engine.resolveLevel(current)
        }
    }

    fun nextLevel() {
        if (isLanMode) {
            if (isHost) {
                handleNextLevelOnHost()
            } else {
                client?.send(NetworkMessage.createAction(NetworkMessage.ACTION_NEXT_LEVEL))
            }
        } else {
            val current = _gameState.value ?: return
            Timber.d("The Mind: avanzando al nivel ${current.level + 1}")
            _gameState.value = engine.startNextLevel(current)
        }
    }

    // -------------------------------------------------------
    // Modo LAN - Configuración
    // -------------------------------------------------------

    fun setupLanMode(
        lanServer: LanServer?,
        lanClient: LanClient?,
        playerId: Int,
        host: Boolean
    ) {
        isLanMode = true
        isHost = host
        server = lanServer
        client = lanClient
        localPlayerId = playerId

        if (isHost) {
            setupHostListeners()
        } else {
            setupClientListeners()
        }
    }

    fun startLanGame(playerNames: List<String>, lives: Int = 3) {
        if (!isHost) return
        val state = engine.newGame(playerNames, lives)
        _gameState.value = state
        broadcastGameState(state)
        sendPrivateHands(state)
    }

    fun getLocalPlayerId(): Int = localPlayerId
    fun isLanMode(): Boolean = isLanMode

    // -------------------------------------------------------
    // Host: procesar acciones
    // -------------------------------------------------------

    private fun handlePlayCardOnHost(playerId: String) {
        val current = _gameState.value ?: return
        Timber.d("The Mind LAN Host: jugador $playerId juega carta")
        val newState = engine.playCard(current, playerId)
        _gameState.value = newState
        broadcastGameState(newState)
        sendPrivateHands(newState)
    }

    private fun handleResolveLevelOnHost() {
        val current = _gameState.value ?: return
        Timber.d("The Mind LAN Host: resolviendo nivel")
        val newState = engine.resolveLevel(current)
        _gameState.value = newState
        broadcastGameState(newState)
    }

    private fun handleNextLevelOnHost() {
        val current = _gameState.value ?: return
        Timber.d("The Mind LAN Host: siguiente nivel")
        val newState = engine.startNextLevel(current)
        _gameState.value = newState
        broadcastGameState(newState)
        sendPrivateHands(newState)
    }

    private fun setupHostListeners() {
        server?.onMessageReceived = { clientId, raw ->
            try {
                val msg = NetworkMessage.parse(raw)
                when (NetworkMessage.getType(msg)) {
                    NetworkMessage.TYPE_ACTION -> {
                        val action = msg.getString("action")
                        mainHandler.post {
                            when (action) {
                                NetworkMessage.ACTION_PLAY_CARD -> {
                                    val playerId = msg.getString("playerId")
                                    handlePlayCardOnHost(playerId)
                                }
                                NetworkMessage.ACTION_RESOLVE -> handleResolveLevelOnHost()
                                NetworkMessage.ACTION_NEXT_LEVEL -> handleNextLevelOnHost()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Error procesando mensaje del cliente $clientId")
            }
        }
    }

    // -------------------------------------------------------
    // Cliente: escuchar estados del Host
    // -------------------------------------------------------

    private fun setupClientListeners() {
        client?.onMessageReceived = { raw ->
            try {
                val msg = NetworkMessage.parse(raw)
                when (NetworkMessage.getType(msg)) {
                    NetworkMessage.TYPE_GAME_STATE -> {
                        mainHandler.post {
                            val state = parseGameStateFromJson(msg)
                            _gameState.value = state
                        }
                    }
                    NetworkMessage.TYPE_PRIVATE_HAND -> {
                        val cards = mutableListOf<Int>()
                        val arr = msg.getJSONArray("cards")
                        for (i in 0 until arr.length()) {
                            cards.add(arr.getInt(i))
                        }
                        mainHandler.post {
                            _localHand.value = cards
                        }
                    }
                    NetworkMessage.TYPE_GAME_OVER -> {
                        // El estado ya se actualiza via GAME_STATE
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Error procesando mensaje del host")
            }
        }
        client?.onDisconnected = {
            mainHandler.post {
                _errorEvent.value = com.partyhub.core.Event("El anfitrión se ha desconectado")
            }
        }
    }

    // -------------------------------------------------------
    // Utilidades de red
    // -------------------------------------------------------

    private fun broadcastGameState(state: MindGameState) {
        val playedCardsTriples = state.playedCards.map {
            Triple(it.number, it.playerId, it.wasCorrect)
        }
        val msg = NetworkMessage.createMindGameState(
            level = state.level,
            lives = state.lives,
            playedCards = playedCardsTriples,
            status = state.status.name,
            pendingCount = state.pendingCards.size
        )
        server?.broadcast(msg)
    }

    private fun sendPrivateHands(state: MindGameState) {
        // El host (jugador 0) se manda a sí mismo su mano via LiveData
        val hostHand = state.playerHands["0"] ?: emptyList()
        _localHand.value = hostHand

        // A cada cliente le enviamos solo su mano
        val clientIds = server?.getClientIds() ?: return
        clientIds.forEachIndexed { index, clientId ->
            val playerId = (index + 1).toString() // Los clientes son jugador 1, 2, 3...
            val hand = state.playerHands[playerId] ?: emptyList()
            server?.sendTo(clientId, NetworkMessage.createPrivateHand(hand))
        }
    }

    private fun parseGameStateFromJson(msg: JSONObject): MindGameState {
        val level = msg.getInt("level")
        val lives = msg.getInt("lives")
        val status = MindStatus.valueOf(msg.getString("status"))
        val pendingCount = msg.getInt("pendingCount")

        val playedCards = mutableListOf<PlayedCard>()
        val arr = msg.getJSONArray("playedCards")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            playedCards.add(
                PlayedCard(
                    number = obj.getInt("number"),
                    playerId = obj.getString("playerId"),
                    wasCorrect = obj.getBoolean("wasCorrect")
                )
            )
        }

        // El cliente no conoce las manos de todos (solo la suya via PRIVATE_HAND)
        // Creamos un state parcial con la info pública
        return MindGameState(
            level = level,
            lives = lives,
            players = emptyList(), // Se rellena localmente
            playerHands = emptyMap(), // Solo la mano local se muestra
            playedCards = playedCards,
            pendingCards = (1..pendingCount).toList(), // Placeholder, solo interesa el conteo
            status = status
        )
    }
}
