package com.rudraksha.routes

import com.rudraksha.model.Message
import com.rudraksha.service.ChatService
import com.rudraksha.service.MessageService
import com.rudraksha.service.UserService
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json

fun Route.chatRoutes(
    userService: UserService,
    chatService: ChatService,
    messageService: MessageService,
    sessionManager: ChatSessionManager
) {
    route("/chats") {
        post("/private") {
            val (user1, user2) = call.receive<PrivateChatRequest>()
            val chat = chatService.createPrivateChat(user1, user2)
            call.respond(chat)
        }

        post("/group") {
            val request = call.receive<GroupChatRequest>()
            val chat = chatService.createGroupChat(
                name = request.name,
                creator = request.creator,
                participants = request.participants
            )
            call.respond(chat)
        }

        get("/{chatId}/messages") {
            val chatId = call.parameters["chatId"]!!
            val messages = messageService.getChatHistory(chatId)
            call.respond(messages)
        }
    }

    webSocket("/ws") {
        val userId = call.request.headers["X-User-ID"] ?: error("Missing user ID")
        val user = userService.getUser(userId) ?: error("User not found")

        sessionManager.join(userId, this)
        try {
            send("Welcome to the chat, ${user.name}!")

            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val message = Json.decodeFromString<Message>(frame.readText())
                        messageService.sendMessage(message)
                        sessionManager.broadcast(message)
                    }
                    else -> {}
                }
            }
        } finally {
            sessionManager.leave(userId)
        }
    }
}

// DTOs
data class PrivateChatRequest(
    val user1: String,
    val user2: String
)
data class GroupChatRequest(
    val name: String,
    val creator: String,
    val participants: List<String>
)