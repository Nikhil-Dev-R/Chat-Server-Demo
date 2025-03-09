package com.rudraksha.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Message(
    val messageId: String = UUID.randomUUID().toString(),
    val senderId: String,
    val chatId: String = "",
    val receiversId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT,  // TEXT, IMAGE, VIDEO, etc.
    val content: String? = null, // For text messages
    val fileMetadata: String? = null // For files (if any)
)

@Serializable
data class FileMetadata(
    val fileName: String,
    val fileType: String,
    val fileSize: Long,
    val totalChunks: Long // Total number of chunks
)