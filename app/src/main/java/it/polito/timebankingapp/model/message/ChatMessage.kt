package it.polito.timebankingapp.model.message

import android.graphics.Bitmap
import java.io.Serializable


data class ChatMessage(
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var messageText: String = "",
    var date: String = "",
    var time: String = "",
    var profilePic: Bitmap
) : Serializable {

    override fun toString(): String = "id:$id, userId: $userId, name: $name, date: $date, time: $time, messageText: $messageText"

}