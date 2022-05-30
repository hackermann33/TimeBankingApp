package it.polito.timebankingapp.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Exclude
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.CompactUser
import java.util.*

data class Chat(
    val timeSlot: TimeSlot = TimeSlot(), val requester: CompactUser = CompactUser(), val offerer: CompactUser = CompactUser(),
    val lastMessageText: String = "",
    val lastMessageTime: Date = Date(),
    val status: Int = STATUS_UNINTERESTED,
    var unreadMsgs: Int = 1 //needed to check or condition (request is or as a requester or as an offerer)
) {


    val requestId: String = timeSlot.id + "_" + requester.id


    /* This could be useless, put just to be safe*/
    val timeSlotId: String = timeSlot.id

    val users = listOf(requester.id, offerer.id)


    fun incUnreadMsg(): Chat {
        return this.copy(unreadMsgs = unreadMsgs+1)
    }

    fun sendFirstMessage(cm: ChatMessage): Chat {
        return this.copy(status = STATUS_INTERESTED, lastMessageText = cm.messageText, lastMessageTime = cm.timestamp.time)
    }


    @Exclude
    fun getType(): Int {
        /* If the current user is the requester, the chat will be a chat to an offer*/
        return if(requester.id == Firebase.auth.uid) CHAT_TYPE_TO_OFFERER else CHAT_TYPE_TO_REQUESTER
    }

    companion object {
        const val CHAT_TYPE_TO_REQUESTER = 0
        const val CHAT_TYPE_TO_OFFERER = 1

        const val STATUS_UNINTERESTED = -1
        const val STATUS_INTERESTED = 0
        const val STATUS_ACCEPTED = 1
        const val STATUS_DISCARDED = 2
    }
}

