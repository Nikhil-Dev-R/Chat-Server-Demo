package com.rudraksha

import com.rudraksha.model.Message
import com.rudraksha.model.WebSocketData
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

val activeUsers = ConcurrentHashMap<String, DefaultWebSocketSession>()
val chatIdList: MutableList<String> = mutableListOf()
val userIdList: MutableList<String> = mutableListOf()

const val HOST = "0.0.0.0"
//const val HOST = "192.168.15.96"
//const val HOST = "192.168.43.63"

fun main() {
    embeddedServer(
        Netty,
        host = HOST,
        port = System.getenv("PORT")?.toInt() ?: 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureSockets()
    configureRouting()
    configureMonitoring()
}

/**
 * Broadcasts the updated user list to all connected users
 */
suspend fun broadcastUserList() {
    val activeSessions = mutableListOf<DefaultWebSocketSession>()

    // Remove inactive users
    mutex.withLock {
        activeUsers.entries.removeIf { entry ->
            val isActive = entry.value.isActive
            if (isActive) activeSessions.add(entry.value)
            !isActive
        }
    }

    // Broadcast updated user list to all active users
    val userKeys = activeUsers.keys.toList()
    val userListMessage = json.encodeToString(WebSocketData.serializer(), WebSocketData.UserList(userKeys))

    activeSessions.forEach { session ->
        session.send(Frame.Text(userListMessage))
    }
}
