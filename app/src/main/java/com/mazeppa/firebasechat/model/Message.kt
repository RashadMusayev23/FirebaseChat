package com.mazeppa.firebasechat.model

/**
 * Rashad Musayev on 5/1/2023 - 16:38
 */
data class Message(
    var messageId: String? = null,
    var message: String? = null,
    var senderId: String? = null,
    var imageUrl: String? = null,
    var time: String? = null,
)
