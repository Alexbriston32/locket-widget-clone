package com.locket.clone.data.model

data class LocketPhoto(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderProfilePicture: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = 0L,
    val recipients: List<String> = emptyList(),
    val reactions: Map<String, String> = emptyMap() // key: userId, value: emoji emoji
)
