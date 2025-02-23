package com.rudraksha.model

import java.util.UUID

data class Chat(
    val id: String = UUID.randomUUID().toString(), // Chat id
    val name: String?, // Chat name
    val type: ChatType, //
    val participants: List<String>, // All members
    val createdBy: String, // Creator
    val createdAt: Long = System.currentTimeMillis(), // Time
    val messages: List<Message> = emptyList() // Messages
)