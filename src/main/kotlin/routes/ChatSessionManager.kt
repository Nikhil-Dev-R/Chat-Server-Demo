package com.rudraksha.routes

import com.rudraksha.database.Participants
import com.rudraksha.model.Message
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

class ChatSessionManager {
    private val activeSessions = ConcurrentHashMap<String, WebSocketSession>()
    private val mutex = Mutex()

    suspend fun join(userId: String, session: WebSocketSession) {
        mutex.withLock {
            activeSessions[userId] = session
        }
    }

    suspend fun leave(userId: String) {
        mutex.withLock {
            activeSessions.remove(userId)
        }
    }

    suspend fun broadcast(message: Message) {
//        val participants = getChatParticipants(message.chatId)
//        mutex.withLock {
//            participants.forEach { userId ->
//                activeSessions[userId]?.send(Frame.Text(Json.encodeToString(message)))
//            }
//        }
    }

    private fun getChatParticipants(chatId: String): List<String> = transaction {
        Participants.select { Participants.chatId eq chatId }
            .map { it[Participants.userId] }
    }
}