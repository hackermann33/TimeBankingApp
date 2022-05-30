package it.polito.timebankingapp.model.chat

import android.util.Log
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


data class ChatMessage(
    var userId: String = "",
    var messageText: String = "",
    var timestamp: Date= Date() /*Calendar*/

    ) : Serializable {

    override fun toString(): String = "userId: $userId, timestamp: $timestamp, messageText: $messageText"
    fun toJson(): HashMap<String, Any> {
        return hashMapOf(
            "messageText" to this.messageText,
            "timestamp" to this.timestamp.time,
            "userId" to this.userId,
        )
    }


}