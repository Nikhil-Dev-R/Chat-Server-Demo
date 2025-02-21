package com.rudraksha.service

import com.rudraksha.database.Chats
import com.rudraksha.database.Participants
import com.rudraksha.model.Chat
import com.rudraksha.model.ChatType
import com.rudraksha.model.Message
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ChatService {
    private val connections = ConcurrentHashMap<String, WebSocketSession>()
    private val mutex = Mutex()

    suspend fun joinChat(userId: String, session: WebSocketSession) {
        mutex.withLock {
            connections[userId] = session
        }
    }

    suspend fun leaveChat(userId: String) {
        mutex.withLock {
            connections.remove(userId)
        }
    }

    suspend fun broadcastMessage(message: Message) {
        mutex.withLock {
            connections.values.forEach { session ->
                try {
                    session.send(
                        Frame.Text( Json.encodeToString(message))
                    )
                } catch (e: Exception) {
                    // Handle connection errors
                }
            }
        }
    }

    fun createPrivateChat(user1: String, user2: String): Chat = transaction {
        val existingChat = findExistingPrivateChat(user1, user2)
        existingChat ?: createNewChat(
            participants = listOf(user1, user2),
            type = ChatType.PRIVATE,

        )
    }

    fun createGroupChat(name: String, creator: String, participants: List<String>): Chat {
        return transaction {
            createNewChat(
                name = name,
                participants = participants + creator,
                type = ChatType.GROUP,
                creator = creator
            )
        }
    }

    private fun findExistingPrivateChat(user1: String, user2: String): Chat? {
        return Chats.join(Participants, JoinType.INNER, Chats.id, Participants.chatId)
            .select {
                (Participants.userId eq user1) and
                        (Chats.type eq ChatType.PRIVATE)
            }
            .groupBy(Chats.id)
            .having { Participants.userId.count() eq 2 }
            .singleOrNull()
            ?.let { row ->
                Chat(
                    id = row[Chats.id],
                    name = row[Chats.name],
                    type = row[Chats.type],
                    participants = listOf(user1, user2),
                    createdBy = row[Chats.createdBy],
                    createdAt = row[Chats.createdAt]
                )
            }
    }

    private fun createNewChat(
        name: String? = null,
        participants: List<String>,
        type: ChatType,
        creator: String = ""
    ): Chat {
        val chatId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        Chats.insert {
            it[id] = chatId
            it[Chats.name] = name
            it[Chats.type] = type
            it[createdBy] = creator
            it[createdAt] = now
        }

        participants.forEach { userId ->
            Participants.insert {
                it[Participants.chatId] = chatId
                it[Participants.userId] = userId
            }
        }

        return Chat(
            id = chatId,
            name = name,
            type = type,
            participants = participants,
            createdBy = creator,
            createdAt = now
        )
    }
}