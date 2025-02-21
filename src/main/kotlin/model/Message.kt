package com.rudraksha.model

import kotlinx.serialization.Serializable
import java.util.*

//@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val senderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "TEXT" // TEXT, IMAGE, VIDEO, etc.
)