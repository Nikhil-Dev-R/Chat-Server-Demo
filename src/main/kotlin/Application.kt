package com.rudraksha

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

val users = ConcurrentHashMap<String, DefaultWebSocketSession>()

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureSockets()
    configureRouting()
    configureMonitoring()
}
