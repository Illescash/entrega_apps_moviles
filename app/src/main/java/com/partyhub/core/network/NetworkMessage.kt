package com.partyhub.core.network

import org.json.JSONArray
import org.json.JSONObject

/**
 * Utilidades para construir y parsear los mensajes JSON del protocolo LAN.
 * Cada mensaje se envía como una línea JSON terminada en '\n'.
 */
object NetworkMessage {

    // Tipos de mensaje
    const val TYPE_JOIN = "JOIN"
    const val TYPE_LOBBY = "LOBBY"
    const val TYPE_GAME_START = "GAME_START"
    const val TYPE_PRIVATE_HAND = "PRIVATE_HAND"
    const val TYPE_ACTION = "ACTION"
    const val TYPE_GAME_STATE = "GAME_STATE"
    const val TYPE_GAME_OVER = "GAME_OVER"
    const val TYPE_LEAVE = "LEAVE"

    // Acciones de juego
    const val ACTION_PLAY_CARD = "PLAY_CARD"
    const val ACTION_SWAP = "SWAP"
    const val ACTION_STAY = "STAY"
    const val ACTION_RESOLVE = "RESOLVE"
    const val ACTION_NEXT_LEVEL = "NEXT_LEVEL"
    const val ACTION_NEXT_ROUND = "NEXT_ROUND"

    // -- Mensajes del cliente al Host --

    fun createJoin(playerName: String): String {
        return JSONObject().apply {
            put("type", TYPE_JOIN)
            put("playerName", playerName)
        }.toString()
    }

    fun createAction(action: String, playerId: String = ""): String {
        return JSONObject().apply {
            put("type", TYPE_ACTION)
            put("action", action)
            put("playerId", playerId)
        }.toString()
    }

    fun createLeave(playerName: String): String {
        return JSONObject().apply {
            put("type", TYPE_LEAVE)
            put("playerName", playerName)
        }.toString()
    }

    // -- Mensajes del Host a los clientes --

    fun createLobby(players: List<String>, game: String, hostName: String): String {
        return JSONObject().apply {
            put("type", TYPE_LOBBY)
            put("players", JSONArray(players))
            put("game", game)
            put("hostName", hostName)
        }.toString()
    }

    fun createGameStart(game: String, playerCount: Int, difficulty: String = "NORMAL"): String {
        return JSONObject().apply {
            put("type", TYPE_GAME_START)
            put("game", game)
            put("playerCount", playerCount)
            put("difficulty", difficulty)
        }.toString()
    }

    fun createPrivateHand(cards: List<Int>): String {
        return JSONObject().apply {
            put("type", TYPE_PRIVATE_HAND)
            put("cards", JSONArray(cards))
        }.toString()
    }

    fun createMindGameState(
        level: Int,
        lives: Int,
        playedCards: List<Triple<Int, String, Boolean>>,
        status: String,
        pendingCount: Int
    ): String {
        val cardsArray = JSONArray()
        playedCards.forEach { (number, playerId, wasCorrect) ->
            cardsArray.put(JSONObject().apply {
                put("number", number)
                put("playerId", playerId)
                put("wasCorrect", wasCorrect)
            })
        }
        return JSONObject().apply {
            put("type", TYPE_GAME_STATE)
            put("game", "the_mind")
            put("level", level)
            put("lives", lives)
            put("playedCards", cardsArray)
            put("status", status)
            put("pendingCount", pendingCount)
        }.toString()
    }

    fun createAsGameState(
        playersJson: JSONArray,
        currentPlayerIndex: Int,
        status: String,
        lastAction: String?
    ): String {
        return JSONObject().apply {
            put("type", TYPE_GAME_STATE)
            put("game", "el_as")
            put("players", playersJson)
            put("currentPlayerIndex", currentPlayerIndex)
            put("status", status)
            if (lastAction != null) put("lastAction", lastAction)
        }.toString()
    }

    fun createGameOver(winner: String, extra: JSONObject? = null): String {
        return JSONObject().apply {
            put("type", TYPE_GAME_OVER)
            put("winner", winner)
            if (extra != null) put("extra", extra)
        }.toString()
    }

    // -- Parsing --

    fun parse(line: String): JSONObject = JSONObject(line)

    fun getType(msg: JSONObject): String = msg.optString("type", "")
}
