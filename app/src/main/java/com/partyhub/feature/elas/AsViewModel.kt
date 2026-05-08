package com.partyhub.feature.elas

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.partyhub.core.network.LanClient
import com.partyhub.core.network.LanServer
import com.partyhub.core.network.NetworkMessage
import com.partyhub.feature.elas.engine.AsGameEngine
import com.partyhub.feature.elas.engine.AsGameState
import com.partyhub.feature.elas.engine.AsPlayer
import com.partyhub.feature.elas.engine.AsStatus
import com.partyhub.core.model.Player
import com.partyhub.core.model.SpanishCard
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import com.partyhub.core.Event
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle

class AsViewModel : ViewModel() {

    private val engine = AsGameEngine()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _gameState = MutableLiveData<AsGameState>()
    val gameState: LiveData<AsGameState> get() = _gameState

    private fun setGameState(state: AsGameState) {
        _gameState.value = state
    }

    private val _errorEvent = MutableLiveData<Event<String>>()
    val errorEvent: LiveData<Event<String>> get() = _errorEvent

    // Indica si es el turno del jugador local (LAN)
    private val _isMyTurn = MutableLiveData(false)
    val isMyTurn: LiveData<Boolean> get() = _isMyTurn

    // Modo LAN
    private var isLanMode = false
    private var isHost = false
    private var server: LanServer? = null
    private var client: LanClient? = null
    private var localPlayerId: String = ""

    // -------------------------------------------------------
    // Modo local
    // -------------------------------------------------------

    fun startGame(playerCount: Int) {
        Timber.d("El As: iniciando partida con $playerCount jugadores")
        setGameState(engine.startNewGame(playerCount))
    }

    fun swap() {
        if (isLanMode) {
            if (isHost) {
                handleSwapOnHost()
            } else {
                client?.send(NetworkMessage.createAction(NetworkMessage.ACTION_SWAP))
            }
        } else {
            val current = _gameState.value ?: return
            Timber.d("El As: jugador ${current.players[current.currentPlayerIndex].player.name} intercambia carta")
            setGameState(engine.swap(current))
        }
    }

    fun stay() {
        if (isLanMode) {
            if (isHost) {
                handleStayOnHost()
            } else {
                client?.send(NetworkMessage.createAction(NetworkMessage.ACTION_STAY))
            }
        } else {
            val current = _gameState.value ?: return
            Timber.d("El As: jugador ${current.players[current.currentPlayerIndex].player.name} se queda")
            setGameState(engine.stay(current))
        }
    }

    fun resolveRound() {
        if (isLanMode) {
            if (isHost) {
                handleResolveOnHost()
            } else {
                client?.send(NetworkMessage.createAction(NetworkMessage.ACTION_RESOLVE))
            }
        } else {
            val current = _gameState.value ?: return
            Timber.d("El As: resolviendo ronda")
            setGameState(engine.resolveRound(current))
            if (_gameState.value?.status == AsStatus.GAME_OVER) {
                val winner = _gameState.value?.players?.firstOrNull { !it.isOut }?.player?.name
                Timber.d("El As: partida terminada, ganador: $winner")
            }
        }
    }

    fun nextRound() {
        if (isLanMode) {
            if (isHost) {
                handleNextRoundOnHost()
            } else {
                client?.send(NetworkMessage.createAction(NetworkMessage.ACTION_NEXT_ROUND))
            }
        } else {
            val current = _gameState.value ?: return
            Timber.d("El As: iniciando nueva ronda")
            setGameState(engine.nextRound(current))
        }
    }

    // -------------------------------------------------------
    // Modo LAN - Configuración
    // -------------------------------------------------------

    fun setupLanMode(
        lanServer: LanServer?,
        lanClient: LanClient?,
        playerId: String,
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

    fun startLanGame(playerNames: List<String>) {
        if (!isHost) return
        viewModelScope.launch(Dispatchers.Default) {
            delay(1500)
            val state = engine.startNewGame(playerNames.size)
            // Reemplazar nombres genéricos por los nombres reales
            val namedPlayers = state.players.mapIndexed { index, asPlayer ->
                val name = if (index < playerNames.size) playerNames[index] else asPlayer.player.name
                asPlayer.copy(player = Player(index.toString(), name))
            }
            val namedState = state.copy(players = namedPlayers)
            mainHandler.post {
                setGameState(namedState)
                updateMyTurn(namedState)
                broadcastAsGameState(namedState)
            }
        }
    }

    fun getLocalPlayerId(): String = localPlayerId
    fun isLanMode(): Boolean = isLanMode

    // -------------------------------------------------------
    // Host: procesar acciones
    // -------------------------------------------------------

    private fun handleSwapOnHost() {
        val current = _gameState.value ?: return
        val newState = engine.swap(current)
        setGameState(newState)
        updateMyTurn(newState)
        broadcastAsGameState(newState)
    }

    private fun handleStayOnHost() {
        val current = _gameState.value ?: return
        val newState = engine.stay(current)
        setGameState(newState)
        updateMyTurn(newState)
        broadcastAsGameState(newState)
    }

    private fun handleResolveOnHost() {
        val current = _gameState.value ?: return
        val newState = engine.resolveRound(current)
        setGameState(newState)
        updateMyTurn(newState)
        broadcastAsGameState(newState)
    }

    private fun handleNextRoundOnHost() {
        val current = _gameState.value ?: return
        val newState = engine.nextRound(current)
        setGameState(newState)
        updateMyTurn(newState)
        broadcastAsGameState(newState)
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
                                NetworkMessage.ACTION_SWAP -> handleSwapOnHost()
                                NetworkMessage.ACTION_STAY -> handleStayOnHost()
                                NetworkMessage.ACTION_RESOLVE -> handleResolveOnHost()
                                NetworkMessage.ACTION_NEXT_ROUND -> handleNextRoundOnHost()
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
                            val state = parseAsGameStateFromJson(msg)
                            setGameState(state)
                            updateMyTurn(state)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Error procesando mensaje del host")
            }
        }
        client?.onDisconnected = {
            mainHandler.post {
                _errorEvent.value = Event("El anfitrión se ha desconectado")
            }
        }
    }

    // -------------------------------------------------------
    // Utilidades de red
    // -------------------------------------------------------

    private fun updateMyTurn(state: AsGameState) {
        if (!isLanMode) return
        val myIndex = state.players.indexOfFirst { it.player.name == localPlayerId }
        _isMyTurn.value = state.currentPlayerIndex == myIndex &&
                state.status == AsStatus.WAITING_ACTION
    }

    private fun broadcastAsGameState(state: AsGameState) {
        val clientIds = server?.getClientIds() ?: emptyList()
        
        // 1. Preparar la lista de jugadores (con cartas ocultas por defecto)
        fun createPlayersJson(forPlayerIndex: Int): JSONArray {
            val playersJson = JSONArray()
            state.players.forEachIndexed { index, asPlayer ->
                playersJson.put(JSONObject().apply {
                    put("id", asPlayer.player.id)
                    put("name", asPlayer.player.name)
                    put("lives", asPlayer.lives)
                    put("isOut", asPlayer.isOut)
                    
                    // Revelar carta si:
                    // - Es la fase de REVELAR/FIN DE RONDA
                    // - O es el jugador que está recibiendo el mensaje (su propia carta)
                    val shouldReveal = state.status == AsStatus.REVEALING ||
                                     state.status == AsStatus.ROUND_OVER ||
                                     state.status == AsStatus.GAME_OVER ||
                                     index == forPlayerIndex
                    
                    if (shouldReveal && asPlayer.hand != null) {
                        put("cardNumber", asPlayer.hand.number)
                        put("cardSuit", asPlayer.hand.suit.name)
                    }
                })
            }
            return playersJson
        }

        // 2. Enviar mensaje personalizado a cada cliente
        clientIds.forEachIndexed { index, clientId ->
            val playerIndex = index + 1 // El Host es 0, clientes empiezan en 1
            val personalMsg = NetworkMessage.createAsGameState(
                createPlayersJson(playerIndex),
                state.currentPlayerIndex,
                state.status.name,
                state.lastAction
            )
            server?.sendTo(clientId, personalMsg.toString())
        }

        // 3. El Host ya tiene el estado completo localmente, no necesita mensaje
    }

    private fun parseAsGameStateFromJson(msg: JSONObject): AsGameState {
        val currentPlayerIndex = msg.getInt("currentPlayerIndex")
        val status = AsStatus.valueOf(msg.getString("status"))
        val lastAction = msg.optString("lastAction", null)

        val playersJson = msg.getJSONArray("players")
        val players = mutableListOf<AsPlayer>()
        for (i in 0 until playersJson.length()) {
            val pj = playersJson.getJSONObject(i)
            val hand = if (pj.has("cardNumber")) {
                SpanishCard(
                    number = pj.getInt("cardNumber"),
                    suit = SpanishCard.Suit.valueOf(pj.getString("cardSuit"))
                )
            } else null

            players.add(
                AsPlayer(
                    player = Player(pj.getString("id"), pj.getString("name")),
                    hand = hand,
                    lives = pj.getInt("lives"),
                    isOut = pj.getBoolean("isOut")
                )
            )
        }

        return AsGameState(
            players = players,
            deck = emptyList(), // El cliente no necesita la baraja
            currentPlayerIndex = currentPlayerIndex,
            status = status,
            lastAction = lastAction
        )
    }
}
