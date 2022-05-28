package it.polito.timebankingapp.model.chat

import java.io.Serializable

data class ChatsListItem(
    var chatId: String = "" ,
    var userId: String = "",
    var timeSlotId: String = "",
    var timeSlotTitle: String = "",
    var otherUserName: String = "",
    var otherProfilePic: String = "",
    var avgReviews: Float = 0.0f,
    var nReviews: Int = 0,
    var lastMessageText: String = "",
    var lastMessageTime: String = "",
    var nUnreadMsg: Int = 0,
    val nTotMsg: Int = 0,
    val status: Int = 0,
    val type: Int = 0
) : Serializable {

    val messagesCounter : Int = 0



    override fun toString(): String = "timeslotId:$timeSlotId, userName: $otherUserName, userPic: $otherProfilePic"

    fun incUnreadMsg(): ChatsListItem {
        return this.copy(nUnreadMsg = nUnreadMsg+1)
    }
}