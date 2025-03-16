package com.rudraksha.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.*

@Serializable
sealed class WebSocketData {

    @Serializable
    @SerialName("Message")
    data class Message(
        val id: String = UUID.randomUUID().toString(),
        val sender: String,
        val receivers: List<String>,
        val chatId: String,
        val content: String? = null,
        val timestamp: Long = System.currentTimeMillis(),
        val isRead: Boolean = false
    ): WebSocketData()

    @Serializable
    @SerialName("JoinRequest")
    data class JoinRequest(
        val sender: String,
        val receiver: String,
        val joinMessage: String? = null,
    ): WebSocketData()

    @Serializable
    @SerialName("JoinResponse")
    data class JoinResponse(
        val sender: String,
        val receiver: String,
        val accepted: Boolean = false,
    ): WebSocketData()

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

// Create the SerializersModule
val webSocketDataModule = SerializersModule {
    polymorphic(WebSocketData::class) {
        subclass(WebSocketData.Message::class, WebSocketData.Message.serializer())
        subclass(WebSocketData.JoinRequest::class, WebSocketData.JoinRequest.serializer())
        subclass(WebSocketData.JoinResponse::class, WebSocketData.JoinResponse.serializer())
        subclass(WebSocketData.GetUsers::class, WebSocketData.GetUsers.serializer())
        subclass(WebSocketData.UserList::class, WebSocketData.UserList.serializer())
        subclass(WebSocketData.UserStatus::class, WebSocketData.UserStatus.serializer())
        subclass(WebSocketData.TypingStatus::class, WebSocketData.TypingStatus.serializer())
        subclass(WebSocketData.Acknowledgment::class, WebSocketData.Acknowledgment.serializer())
        subclass(WebSocketData.Error::class, WebSocketData.Error.serializer())
    }
}

