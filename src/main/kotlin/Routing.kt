package com.rudraksha

import com.rudraksha.model.FileMetadata
import com.rudraksha.model.Message
import com.rudraksha.model.WebSocketData
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
        webSocket("/ws") {
            call.respond("good")
        }

        webSocket("/chat/{username}") {
            val username = call.parameters["username"]
            if (username == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No username"))
                return@webSocket
            }

            users[username] = this

            val connectedMessage = Message(
                senderId = "Server",
                receiversId = username,
                content = "Hello $username! You are now connected.",
            )
            send(Frame.Text(Json.encodeToString(connectedMessage)))

            incoming.consumeEach { frame ->
                when (frame) {
                    is Frame.Text -> {
                        val message = Json.decodeFromString<Message>(frame.readText())
                        val receivedText = frame.readText()
                        val targetUsers = message.receiversId.split(",")

                        message.fileMetadata?.let { fileMeta ->
                            val fileMetadata = Json.decodeFromString<FileMetadata>(fileMeta)
                            val fileKey = "${message.senderId}_${fileMetadata.fileName}"

                            println("Receiving file: ${fileMetadata.fileName} (${fileMetadata.fileSize} bytes) from ${message.senderId}")

                            // Initialize chunk storage
                            fileChunks[fileKey] = mutableListOf()
                        }
                                targetUsers . forEach { user ->
                            users[user]?.send(Frame.Text(Json.encodeToString(message)))
                        }

                        /*val data = json.decodeFromString<WebSocketData>(receivedText)
                        when (data) {
                            is WebSocketData.JoinRequest -> {
                                val receiverSession = users[data.receiverUsername]
                                mutex.withLock { users[username] = this }
                                send(
                                    json.encodeToString(
                                        WebSocketData.JoinResponse(
                                            true,
                                            "Welcome, ${data.username}",
                                            data.userId
                                        )
                                    )
                                )
//                                broadcastUserList()
                            }

                            is WebSocketData.Message -> {
                                val receiverSession = users[data.receivers]
                                receiverSession?.send(json.encodeToString(data))
                            }

                            is WebSocketData.GetUsers -> {
                                val users = activeUsers.keys.toList()
                                send(json.encodeToString(WebSocketData.UserList(users)))
                            }

                            is WebSocketData.TypingStatus -> {
                                val receiverSession = activeUsers[data.receiver]
                                receiverSession?.send(json.encodeToString(data))
                            }

                            is WebSocketData.Acknowledgment -> {
                                println("Message ${data.messageId} marked as ${data.status}")
                            }

                            is WebSocketData.Error -> {
                                println("Error received: ${data.errorMessage}")
                            }

                            else -> Unit
                        }*/
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

            users.remove(username)
        }
    }
}
