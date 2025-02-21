package com.rudraksha.service

import com.rudraksha.database.Users
import com.rudraksha.model.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class UserService {
    fun createUser(user: User): User = transaction {
        if (Users.select { Users.id eq user.id }.empty()) {
            Users.insert {
                it[id] = user.id
                it[name] = user.name
            }
        }
        user
    }

    fun getUser(userId: String): User? = transaction {
        Users.select { Users.id eq userId }
            .singleOrNull()
            ?.let { User(it[Users.id], it[Users.name]) }
    }
}