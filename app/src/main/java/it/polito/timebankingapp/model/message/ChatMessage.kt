package it.polito.timebankingapp.model.message

import android.graphics.Bitmap
import java.io.Serializable


data class ChatMessage(
    var messageId: String = "",
    var userId: String = "",
    var messageText: String = "",
    var timestamp: String = ""
) : Serializable {

    override fun toString(): String = "messageId:$messageId, userId: $userId, timestamp: $timestamp, messageText: $messageText"

}