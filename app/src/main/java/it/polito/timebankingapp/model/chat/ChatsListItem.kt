package it.polito.timebankingapp.model.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.Serializable
import java.util.*

data class ChatsListItem(
    var chatId: String,
    var userId: String,
    var timeslotId: String ,
//    var userId: String = "",
    var userName: String ,
    var userPic: String ,
    var lastMessageText: String ,
    var lastMessageTime: String
) : Serializable {



    override fun toString(): String = "timeslotId:$timeslotId, userName: $userName, userPic: $userPic"
}