package it.polito.timebankingapp.model.chat

import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.Request
import java.io.Serializable

data class ChatsListItem(
    var chatId: String = "",
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
    val status: Int = Request.STATUS_UNINTERESTED,
    val type: Int = 0
) : Serializable {

    val messagesCounter : Int = 0



    override fun toString(): String = "timeslotId:$timeSlotId, userName: $otherUserName, userPic: $otherProfilePic"

    fun incUnreadMsg(): ChatsListItem {
        return this.copy(nUnreadMsg = nUnreadMsg+1)
    }

    fun sendFirstMessage(cm: ChatMessage): ChatsListItem{
        return this.copy(type = Request.STATUS_INTERESTED, nTotMsg = 1, lastMessageText = cm.messageText, lastMessageTime = Helper.dateToDisplayString(cm.timestamp.time))
    }

companion object {
    const val CHAT_TYPE_TO_REQUESTER = 0
    const val CHAT_TYPE_TO_OFFERER = 1
}
}