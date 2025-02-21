package com.rudraksha

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
import io.ktor.websocket.send
import kotlinx.coroutines.channels.consumeEach

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
            send(Frame.Text("Hello $username! You are now connected."))

            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    val parts = message.split(": ", limit = 2)
                    if (parts.size == 2) {
                        val targetUser = parts[0]
                        val msg = parts[1]
                        users[targetUser]?.send("$username: $msg")
                    } else {
                        send(Frame.Text("Not a valid username: $username"))
                    }
                }
            }

            users.remove(username)
        }
    }
}
