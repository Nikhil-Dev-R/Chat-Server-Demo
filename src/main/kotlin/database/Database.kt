package com.rudraksha.database

import com.rudraksha.model.ChatType
import com.rudraksha.model.MessageType
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:h2:file:./data/chatdb;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )

        transaction {
            SchemaUtils.create(Users, Chats, Participants, Messages)
        }
    }
}

// Tables
object Users : Table() {
    val id = varchar("id", 50)
    val name = varchar("name", 100)
    val online = bool("online")

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(id)
}

object Chats : Table() {
    val id = varchar("id", 50)
    val name = varchar("name", 100).nullable()
    val type = enumeration("type", ChatType::class)
    val createdBy = varchar("created_by", 50).references(Users.id)
    val createdAt = long("created_at")

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(id)
}

object Participants : Table() {
    val chatId = varchar("chat_id", 50).references(Chats.id)
    val userId = varchar("user_id", 50).references(Users.id)

    override val primaryKey = PrimaryKey(chatId, userId)
}

object Messages : Table() {
    val id = varchar("id", 50)
    val chatId = varchar("chat_id", 50).references(Chats.id)
    val senderId = varchar("sender_id", 50).references(Users.id)
    val content = text("content")
    val timestamp = long("timestamp")
    val type = enumeration("type", MessageType::class)

    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(id)
}
