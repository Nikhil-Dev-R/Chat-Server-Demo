package com.rudraksha.model

import kotlinx.serialization.Serializable

@Serializable
sealed class WebSocketData {

    @Serializable
    data class Message(
        val id: String,
        val sender: String,
        val receivers: String,
        val content: String,
        val timestamp: Long,
        val isRead: Boolean = false
    ) : WebSocketData()

    @Serializable
    data class JoinRequest(
        val username: String,
        val userId: String
    ) : WebSocketData()

    @Serializable
    data class JoinResponse(
        val success: Boolean,
        val message: String,
        val userId: String? = null
    ) : WebSocketData()

    @Serializable
    data class GetUsers(
        val user: String
    ) : WebSocketData() // Request to fetch all users

    @Serializable
    data class UserList(
        val users: List<String>
    ) : WebSocketData()

    @Serializable
    data class UserStatus(
        val username: String,
        val isOnline: Boolean
    ) : WebSocketData()

    @Serializable
    data class TypingStatus(
        val sender: String,
        val receiver: String,
        val isTyping: Boolean
    ) : WebSocketData()

    @Serializable
    data class Acknowledgment(
        val messageId: String,
        val status: Status
    ) : WebSocketData() {
        @Serializable
        enum class Status {
            SENT, DELIVERED, READ
        }
    }

    @Serializable
    data class Error(
        val errorCode: Int,
        val errorMessage: String
    ) : WebSocketData()
}