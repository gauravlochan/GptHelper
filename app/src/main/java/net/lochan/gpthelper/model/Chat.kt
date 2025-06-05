package net.lochan.gpthelper.model

import java.time.LocalDateTime

/**
 * Represents a ChatGPT conversation.
 */
data class Chat(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: LocalDateTime,
    val project: String? = null
) 