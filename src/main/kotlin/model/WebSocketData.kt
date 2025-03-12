package com.rudraksha.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.SerialName

@Serializable
sealed class WebSocketData {

    @Serializable
    @SerialName("Message")
    data class Message(
        val id: String,
        val sender: String,
        val receivers: List<String>,
        val content: String,
        val timestamp: Long,
        val isRead: Boolean = false
    ) : WebSocketData()

    @Serializable
    @SerialName("JoinRequest")
    data class JoinRequest(
        val senderUsername: String,
        val receiverUsername: String,
        val joinMessage: String = "",
    ): WebSocketData()

    @Serializable
    @SerialName("JoinResponse")
    data class JoinResponse(
        val senderUsername: String,
        val receiverUsername: String,
        val accepted: Boolean = false,
    ) : WebSocketData()

    @Serializable
    @SerialName("GetUsers")
    data class GetUsers(
        val user: String
    ) : WebSocketData() // Request to fetch all users

    @Serializable
    @SerialName("UserList")
    data class UserList(
        val users: List<String>
    ) : WebSocketData()

    @Serializable
    @SerialName("UserStatus")
    data class UserStatus(
        val username: String,
        val isOnline: Boolean
    ) : WebSocketData()

    @Serializable
    @SerialName("TypingStatus")
    data class TypingStatus(
        val sender: String,
        val receivers: List<String>,
        val isTyping: Boolean
    ) : WebSocketData()

    @Serializable
    @SerialName("Acknowledgment")
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
    @SerialName("Error")
    data class Error(
        val errorCode: Int,
        val errorMessage: String
    ) : WebSocketData()
}

fun Message.toWebSocketMessage(): WebSocketData.Message {
    return WebSocketData.Message(
        id = this.messageId,
        sender = this.senderId,
        receivers = this.receiversId.split(","),
        content = this.content ?: "",
        timestamp = this.timestamp,
    )
}

// Create the SerializersModule
val webSocketDataModule = SerializersModule {
    polymorphic(WebSocketData::class) {
        subclass(WebSocketData.Message::class)
        subclass(WebSocketData.JoinRequest::class)
        subclass(WebSocketData.JoinResponse::class)
        subclass(WebSocketData.GetUsers::class)
        subclass(WebSocketData.UserList::class)
        subclass(WebSocketData.UserStatus::class)
        subclass(WebSocketData.TypingStatus::class)
        subclass(WebSocketData.Acknowledgment::class)
        subclass(WebSocketData.Error::class)
    }
}