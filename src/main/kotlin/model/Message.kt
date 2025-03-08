package com.rudraksha.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiversId: String = "", // For Room, store receiversId as a comma-separated string;
    val content: String? = null, // For text messages
    val type: MessageType = MessageType.TEXT,  // TEXT, IMAGE, VIDEO, etc.
    val timestamp: Long = System.currentTimeMillis(),
    val fileMetadata: String? = null, // For files
)

@Serializable
data class FileMetadata(
    val fileName: String,
    val fileType: String,
    val fileSize: Long,
    val totalChunks: Long // Total number of chunks
)