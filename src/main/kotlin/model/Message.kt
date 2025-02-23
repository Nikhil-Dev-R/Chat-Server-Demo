package com.rudraksha.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String,
    val receiversId: List<String> = listOf(),
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType,  // TEXT, IMAGE, VIDEO, etc.
    val content: String? = null, // For text messages
    val fileMetadata: FileMetadata? = null, // For files
    val chunkIndex: Int? = null, // If file is sent in chunks
    val totalChunks: Int? = null // Total number of chunks
)

@Serializable
data class FileMetadata(
    val fileName: String,
    val fileType: String,
    val fileSize: Long
)