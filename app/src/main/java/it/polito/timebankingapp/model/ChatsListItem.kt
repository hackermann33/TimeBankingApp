package it.polito.timebankingapp.model

import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.timeslot.CompactTimeSlot
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.CompactUser
import it.polito.timebankingapp.model.user.User
import java.util.*

data class ChatsListItem(
    val timeSlot: TimeSlot = TimeSlot(), val requester: CompactUser = CompactUser(), val offerer: CompactUser = CompactUser(),
    val lastMessageText: String = "",
    val lastMessageTime: Date = Date(),
    val status: Int = STATUS_UNINTERESTED,
    val type: Int = CHAT_TYPE_TO_OFFERER,
    var users: List<String> = listOf(),
    var nUnreadMsgs: Int = 1 //needed to check or condition (request is or as a requester or as an offerer)
) {
    val requestId: String = timeSlot.id + "_" + requester.id


    /* This could be useless, put just to be safe*/
    val timeSlotId: String = timeSlot.id


    init {
        users = listOf(requester.id, offerer.id)

}


    fun incUnreadMsg(): ChatsListItem {
        return this.copy(nUnreadMsgs = nUnreadMsgs+1)
    }

    fun sendFirstMessage(cm: ChatMessage): ChatsListItem {
        return this.copy(type = STATUS_INTERESTED, lastMessageText = cm.messageText, lastMessageTime = cm.timestamp.time)
    }

    companion object {

        const val CHAT_TYPE_TO_REQUESTER = 0
        const val CHAT_TYPE_TO_OFFERER = 1
        const val STATUS_UNINTERESTED = -1
        const val STATUS_INTERESTED = 0
        const val STATUS_ACCEPTED = 1
    }
}

