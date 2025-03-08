package com.rudraksha.service

import com.rudraksha.database.Messages
import com.rudraksha.model.Message
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class MessageService {
    fun sendMessage(message: Message): Message = transaction {
//        Messages.insert {
//            it[id] = message.id
//            it[chatId] = message.chatId
//            it[senderId] = message.senderId
//            it[content] = message.text
//            it[timestamp] = message.timestamp
//            it[type] = message.type.toString()
//        }
        message
    }

    fun getChatHistory(chatId: String, limit: Int = 100): List<Message> = transaction {
        Messages.select { Messages.chatId eq chatId }
            .orderBy(Messages.timestamp to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                Message(
                    id = row[Messages.id],
//                    chatId = row[Messages.chatId],
                    senderId = row[Messages.senderId],
                    content = row[Messages.content],
                    timestamp = row[Messages.timestamp],
                    type = row[Messages.type]
                )
            }
    }
}