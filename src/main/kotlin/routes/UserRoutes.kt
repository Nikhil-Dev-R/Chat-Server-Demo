package com.rudraksha.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.rudraksha.model.User
import com.rudraksha.service.UserService
import io.ktor.http.HttpStatusCode

fun Route.userRoutes() {
    val userService = UserService()
    route("/users") {
        post {
            val user = call.receive<User>()
            userService.createUser(user)
            call.respond(user)
        }

        get("/{userId}") {
            val userId = call.parameters["userId"] ?: error("No user ID provided")
            val user = userService.getUser(userId)
            if (user != null) {
                call.respond(user)
            } else {
                call.respondText("User not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}