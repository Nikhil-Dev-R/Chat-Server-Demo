package com.rudraksha

import com.rudraksha.model.WebSocketData
import com.rudraksha.model.WebSocketData.*
import com.rudraksha.model.webSocketDataModule
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.Frame.*
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

val fileChunks = mutableMapOf<String, MutableList<ByteArray>>() // Store chunks per file

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = true
    allowStructuredMapKeys = true
    encodeDefaults = true
    classDiscriminator = "type" // Use "type" as the discriminator
    serializersModule = webSocketDataModule
}

val mutex = Mutex()

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        webSocket("/chat/{username}/{password}") {
            val username = call.parameters["username"]

            if (username.isNullOrBlank()) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No username provided"))
                return@webSocket
            }

            try {
                // Add user to active list
                mutex.withLock { activeUsers[username] = this }

                val connectedMessage = WebSocketData.Message(
                    sender = "Server",
                    receivers = listOf(username),
                    chatId = "",
                    content = "Hello $username! You are now connected.",
                    timestamp = System.currentTimeMillis()
                )
                send(Frame.Text(json.encodeToString(WebSocketData.serializer(), connectedMessage)))

                println("User Connected: $username")

                incoming.consumeEach { frame ->
                    when (frame) {
                        is Frame.Text -> {
                            val receivedText = frame.readText()
                            println("📝 Received raw JSON: $receivedText") // Debugging line
                            try {
                                val data = json.decodeFromString<WebSocketData>(receivedText)
                                handleWebSocketData(data, username)
                            } catch (e: Exception) {
                                println("❌ Error deserializing message: ${e.localizedMessage}")
                                sendErrorMessage("Invalid message format", 400)
                            }
                        }

                        is Frame.Binary -> {
                            handleBinaryData(frame)
                        }

                        else -> {
                            send(Frame.Text("Unknown message type received"))
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ WebSocket connection error for user $username: ${e.localizedMessage}")
            } finally {
                activeUsers.remove(username)
                broadcastUserList()
            }
        }
    }
}

/**
 * Handles incoming WebSocket messages.
 */
suspend fun DefaultWebSocketServerSession.handleWebSocketData(data: WebSocketData, username: String) {
    when (data) {
        is WebSocketData.JoinRequest -> {
            val receiverSession = activeUsers[data.receiver]
            mutex.withLock { activeUsers[username] = this }

            if (receiverSession != null) {
                receiverSession.send(Text(json.encodeToString(data)))
            } else {
                sendErrorMessage("User ${data.receiver} is not online", 404)
            }
        }

        is WebSocketData.JoinResponse -> {
            val receiverSession = activeUsers[data.receiver]
            mutex.withLock { activeUsers[username] = this }

            receiverSession?.send(Text(json.encodeToString(data)))
                ?: sendErrorMessage("User ${data.receiver} is not online", 404)
        }

        is WebSocketData.Message -> {
            println("📨 Message Received: $data")

            data.receivers.forEach { user ->
                if (activeUsers.containsKey(user)) {
                    activeUsers[user]?.send(Text(json.encodeToString(data)))
                } else {
                    println("⚠️ User $user is not online")
                }
            }
        }

        is WebSocketData.GetUsers -> {
            val users = activeUsers.keys.toList()
            send(Text(json.encodeToString(UserList(users))))
        }

        is WebSocketData.TypingStatus -> {
            data.receivers.forEach { user ->
                activeUsers[user]?.send(Text(json.encodeToString(data)))
            }
        }

        is WebSocketData.Acknowledgment -> {
            println("📬 Message ${data.messageId} marked as ${data.status}")
        }

        is WebSocketData.Error -> {
            println("❌ Error received: ${data.errorMessage}")
        }

        is WebSocketData.UserList -> TODO()
        is WebSocketData.UserStatus -> TODO()
    }
}

/**
 * Handles file transfer via WebSocket binary frames.
 */
suspend fun DefaultWebSocketServerSession.handleBinaryData(frame: Frame.Binary) {
    val fileKey = "User1_somefile.jpg" // Get correct fileKey dynamically
    val chunkList = fileChunks[fileKey]

    if (chunkList != null) {
        chunkList.add(frame.data)
        println("📥 Received chunk ${chunkList.size}")

        if (chunkList.size == 10) { // Replace 10 with actual totalChunks
            val completeFile = chunkList.reduce { acc, bytes -> acc + bytes }
            val fileName = "uploads/$fileKey"
            File(fileName).writeBytes(completeFile)
            println("✅ File saved: $fileName")
        }
    }
}

/**
 * Sends an error message back to the client.
 */
suspend fun DefaultWebSocketServerSession.sendErrorMessage(message: String, errorCode: Int) {
    val errorResponse = WebSocketData.Error(errorCode, message)
    send(Frame.Text(json.encodeToString(errorResponse)))
}

