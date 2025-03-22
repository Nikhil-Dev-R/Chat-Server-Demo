package com.rudraksha.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import java.util.*

@Serializable
sealed class WebSocketData {
    @Serializable
    @SerialName("ConnectionStatus")
    data class ConnectionStatus(
        val status: String
    ): WebSocketData()

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
    @SerialName("GetChats")
    data class GetChats(
        val user: String
    ) : WebSocketData() // Request to fetch all chat

    @Serializable
    @SerialName("UserList")
    data class UserList(
        val users: List<String>
    ) : WebSocketData()

    @Serializable
    @SerialName("ChatList")
    data class ChatList(
        val chats: List<String>
    ) : WebSocketData()

    @Serializable
    @SerialName("SaveUser")
    data class SaveUser(
        val user: String,
    ): WebSocketData()

    @Serializable
    @SerialName("DeleteUser")
    data class DeleteUser(
        val user: String
    ) : WebSocketData()

    @Serializable
    @SerialName("SaveChat")
    data class SaveChat(
        val chatId: String,
    ) : WebSocketData()

    @Serializable
    @SerialName("DeleteChat")
    data class DeleteChat(
        val chatId: String,
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

    @Serializable
    @SerialName("Chat")
    data class Chat(
        val chatId: String, // Chat id
        val name: String, // Chat name
        val chatType: ChatType = ChatType.PRIVATE,
        val participants: String = "",
        val createdBy: String, // Creator username
        val createdAt: Long = System.currentTimeMillis(),
        val lastMessage: String = "",
        val time: Long = System.currentTimeMillis(),
        val unreadCount: Int = 0,
        val profileImage: String = ""
    ) : WebSocketData()

    @Serializable
    @SerialName("User")
    data class User(
        val username: String,
        val passwordHash: String,
        val email: String,
        val createdAt: Long = System.currentTimeMillis(),
        val updatedAt: Long = System.currentTimeMillis(),
        val lastSeen: Long = System.currentTimeMillis(),
        val isOnline: Boolean = false,
        val profileImage: String = ""
    ) : WebSocketData()
}

// Create the SerializersModule
val webSocketDataModule = SerializersModule {
    polymorphic(WebSocketData::class) {
        subclass(WebSocketData.ConnectionStatus::class, WebSocketData.ConnectionStatus.serializer())
        subclass(WebSocketData.Message::class, WebSocketData.Message.serializer())
        subclass(WebSocketData.Chat::class, WebSocketData.Chat.serializer())
        subclass(WebSocketData.User::class, WebSocketData.User.serializer())
        subclass(WebSocketData.GetUsers::class, WebSocketData.GetUsers.serializer())
        subclass(WebSocketData.GetChats::class, WebSocketData.GetChats.serializer())
        subclass(WebSocketData.UserList::class, WebSocketData.UserList.serializer())
        subclass(WebSocketData.ChatList::class, WebSocketData.ChatList.serializer())
        subclass(WebSocketData.SaveUser::class, WebSocketData.SaveUser.serializer())
        subclass(WebSocketData.SaveChat::class, WebSocketData.SaveChat.serializer())
        subclass(WebSocketData.DeleteUser::class, WebSocketData.DeleteUser.serializer())
        subclass(WebSocketData.DeleteChat::class, WebSocketData.DeleteChat.serializer())
        subclass(WebSocketData.UserStatus::class, WebSocketData.UserStatus.serializer())
        subclass(WebSocketData.TypingStatus::class, WebSocketData.TypingStatus.serializer())
        subclass(WebSocketData.Acknowledgment::class, WebSocketData.Acknowledgment.serializer())
        subclass(WebSocketData.JoinRequest::class, WebSocketData.JoinRequest.serializer())
        subclass(WebSocketData.JoinResponse::class, WebSocketData.JoinResponse.serializer())
        subclass(WebSocketData.Error::class, WebSocketData.Error.serializer())
    }
}
