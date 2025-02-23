package com.rudraksha.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val senderId: String,
    val receiversId: List<String> = listOf(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT // TEXT, IMAGE, VIDEO, etc.
)