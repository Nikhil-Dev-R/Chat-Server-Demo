package com.rudraksha.model

data class Chat(
    val id: String,
    val name: String?,
    val type: ChatType,
    val participants: List<String>,
    val createdBy: String,
    val createdAt: Long
)