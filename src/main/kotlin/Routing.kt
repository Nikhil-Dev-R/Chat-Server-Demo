package com.rudraksha

import com.rudraksha.model.FileMetadata
import com.rudraksha.model.Message
import com.rudraksha.model.WebSocketData
import com.rudraksha.utils.createChatId
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

const val code = "!@#$%^&*()_+1234567890-=],.'/"
val fileChunks = mutableMapOf<String, MutableList<ByteArray>>() // Store chunks per file

val json = Json { ignoreUnknownKeys = true }
val mutex = Mutex()

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        webSocket("/chat/{username}") {
            val username = call.parameters["username"]
            if (username == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No username"))
                return@webSocket
            }

            activeUsers[username] = this

            val connectedMessage = Message(
                senderId = "Server",
                receiversId = username,
                chatId = createChatId(listOf(username)),
                content = "Hello $username! You are now connected.",
            )
            send(Frame.Text(json.encodeToString(connectedMessage)))
            println(connectedMessage)

            incoming.consumeEach { frame ->
                when (frame) {
                    is Frame.Text -> {
                        val message = json.decodeFromString<Message>(frame.readText())
                        val targetUsers = message.receiversId.split(",")

                        message.fileMetadata?.let { fileMeta ->
                            val fileMetadata = json.decodeFromString<FileMetadata>(fileMeta)
                            val fileKey = "${message.senderId}_${fileMetadata.fileName}"

                            println("Receiving file: ${fileMetadata.fileName} (${fileMetadata.fileSize} bytes) from ${message.senderId}")

                            // Initialize chunk storage
                            fileChunks[fileKey] = mutableListOf()
                        }
                        targetUsers.forEach { user ->
                            activeUsers[user]?.send(Frame.Text(json.encodeToString(message)))
                        }
                        val receivedText = frame.readText()
                        val data = json.decodeFromString<WebSocketData>(receivedText)
                        when (data) {
                            is WebSocketData.JoinRequest -> {
                                val receiverSession = activeUsers[data.receiverUsername]
                                mutex.withLock { activeUsers[username] = this }
                                receiverSession?.send(
                                    Frame.Text(
                                        json.encodeToString<WebSocketData.JoinRequest>(
                                            data
                                        )
                                    )
                                )
                            }

                            is WebSocketData.JoinResponse -> {
                                val receiverSession = activeUsers[data.receiverUsername]
                                mutex.withLock { activeUsers[username] = this }
                                receiverSession?.send(
                                    Frame.Text(
                                        json.encodeToString<WebSocketData.JoinResponse>(
                                            data
                                        )
                                    )
                                )
                            }

                            is WebSocketData.Message -> {
                                data.receivers.forEach { user ->
                                    if (user in activeUsers.map { it.key }) {
                                        activeUsers[user]?.send(Frame.Text(json.encodeToString(
                                            data
                                        )))
                                    }
                                }
                            }

                            is WebSocketData.GetUsers -> {
                                val users = activeUsers.keys.toList()
                                send(Frame.Text(json.encodeToString(WebSocketData.UserList(users))))
                            }

                            is WebSocketData.TypingStatus -> {
                                data.receivers.forEach { user ->
                                    if (user in activeUsers.map { it.key }) {
                                        activeUsers[user]?.send(Frame.Text(json.encodeToString(
                                            data
                                        )))
                                    }
                                }
                            }

                            is WebSocketData.Acknowledgment -> {
                                println("Message ${data.messageId} marked as ${data.status}")
                            }

                            is WebSocketData.Error -> {
                                println("Error received: ${data.errorMessage}")
                            }

                            else -> Unit
                        }
                    }

                    is Frame.Binary -> {
                        val fileKey = "User1_somefile.jpg" // Get correct fileKey dynamically
                        val chunkList = fileChunks[fileKey]

                        if (chunkList != null) {
                            chunkList.add(frame.data)
                            println("📥 Received chunk ${chunkList.size}")

                            // When all chunks are received, save the file
                            if (chunkList.size == 10) { // Replace 10 with actual `totalChunks`
                                val completeFile = chunkList.reduce { acc, bytes -> acc + bytes }
                                val fileName = "uploads/$fileKey"
                                File(fileName).writeBytes(completeFile)
                                println("✅ File saved: $fileName")
                            }
                        }
                    }

                    else -> {
                        send(Frame.Text("using else"))
                    }
                }
            }

            activeUsers.remove(username)
            broadcastUserList()
        }
    }
}
