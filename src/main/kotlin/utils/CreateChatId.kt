package com.rudraksha.utils

fun createChatId(usernames: List<String>): String {
    return usernames.sorted().joinToString(separator = "^")
}

fun isUserInChat(chatId: String, username: String): Boolean {
    return chatId.split("^").contains(username)
}

fun getChatParticipants(chatId: String): List<String> {
    return chatId.split("^")
}

fun getReceivers(chatId: String, username: String): String {
    val list = chatId.split("^").toMutableList()
    if (list.size > 1) list.remove(username)
    return list.joinToString(",")
}