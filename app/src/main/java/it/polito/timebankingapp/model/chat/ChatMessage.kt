package it.polito.timebankingapp.model.chat

import android.util.Log
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


data class ChatMessage(
    var messageId: String = "",
    var userId: String = "",
    var messageText: String = "",
    var timestamp: String = ""
) : Serializable {

    override fun toString(): String = "messageId:$messageId, userId: $userId, timestamp: $timestamp, messageText: $messageText"


}