package it.polito.timebankingapp.model.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.Serializable

data class ChatsListItem(
    var timeslotId: String = "",
//    var userId: String = "",
    var userName: String = "",
    var userPic: String = "",
    var chatMessages: List<ChatMessage> = listOf(),
    var chatId : String = ""
) : Serializable {

//    override fun toString(): String = "timeslotId:$timeslotId, userId: $userId, chatMessages: $chatMessages"
    override fun toString(): String = "timeslotId:$timeslotId, userName: $userName, userPic: $userPic, chatMessages: $chatMessages"
}