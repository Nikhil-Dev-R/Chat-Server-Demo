package com.rudraksha

import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

val users = ConcurrentHashMap<String, DefaultWebSocketSession>()
const val HOST = "0.0.0.0"
//const val HOST = "192.168.15.96"
//const val HOST = "192.168.43.63"

fun main(
    args: Array<String>
) {
//    io.ktor.server.netty.EngineMain.main(args)
    embeddedServer(
        Netty,
        host = HOST,
        port = System.getenv("PORT")?.toInt() ?: 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureSockets()
    configureRouting()
    configureMonitoring()
}
