package com.rudraksha.model

import java.util.UUID

data class Chat(
    val id: String = UUID.randomUUID().toString(), // Chat id
    val name: String? = null, // Chat name
    val type: ChatType = ChatType.PRIVATE, //
    val participants: List<String> = listOf(), // All members id
    val createdBy: String, // Creator
    val createdAt: Long = System.currentTimeMillis(), // Time
    val messages: List<Message> = emptyList() // Messages
)