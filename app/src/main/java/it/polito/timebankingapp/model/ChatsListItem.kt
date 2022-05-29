package it.polito.timebankingapp.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Exclude
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.CompactUser
import java.util.*

data class ChatsListItem(
    val timeSlot: TimeSlot = TimeSlot(), val requester: CompactUser = CompactUser(), val offerer: CompactUser = CompactUser(),
    val lastMessageText: String = "",
    val lastMessageTime: Date = Date(),
    val status: Int = STATUS_UNINTERESTED,
    var users: List<String> = listOf(),
    var unreadMsgs: Int = 1 //needed to check or condition (request is or as a requester or as an offerer)
) {
    @Exclude
    val type: Int = if(requester.id == Firebase.auth.uid) CHAT_TYPE_TO_OFFERER else CHAT_TYPE_TO_REQUESTER

    val requestId: String = timeSlot.id + "_" + requester.id


    /* This could be useless, put just to be safe*/
    val timeSlotId: String = timeSlot.id


    init {
        users = listOf(requester.id, offerer.id)

}


    fun incUnreadMsg(): ChatsListItem {
        return this.copy(unreadMsgs = unreadMsgs+1)
    }

    fun sendFirstMessage(cm: ChatMessage): ChatsListItem {
        return this.copy(status = STATUS_INTERESTED, lastMessageText = cm.messageText, lastMessageTime = cm.timestamp.time)
    }

    companion object {

        const val CHAT_TYPE_TO_REQUESTER = 0
        const val CHAT_TYPE_TO_OFFERER = 1
        const val STATUS_UNINTERESTED = -1
        const val STATUS_INTERESTED = 0
        const val STATUS_ACCEPTED = 1
    }
}

