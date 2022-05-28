package it.polito.timebankingapp.model.chat

import java.io.Serializable

data class ChatsListItem(
    var chatId: String,
    var userId: String,
    var timeSlotId: String,
    var timeSlotTitle: String,
//    var userId: String = "",
    var userName: String,
    var userPic: String,
    var lastMessageText: String,
    var lastMessageTime: String,
    val nUnreadMsg: Int
) : Serializable {




    override fun toString(): String = "timeslotId:$timeSlotId, userName: $userName, userPic: $userPic"
}