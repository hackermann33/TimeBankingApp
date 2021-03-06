package it.polito.timebankingapp.model

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Exclude
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.CompactUser

data class Chat(
    val timeSlot: TimeSlot = TimeSlot(), val requester: CompactUser = CompactUser(), val offerer: CompactUser = CompactUser(),
    var lastMessage: ChatMessage = ChatMessage(),
    var status: Int = STATUS_UNINTERESTED,
    var offererUnreadMsg: Int = 0, var requesterUnreadMsg: Int = 0, //needed to check or condition (request is or as a requester or as an offerer)
    var unreadMsgs: Int = 0,
    val requestId: String = timeSlot.id + "_" + requester.id
)
{

    /* This could be useless, put just to be safe*/
    val timeSlotId: String = timeSlot.id

    val users = listOf(requester.id, offerer.id)
    /*val users = mapOf(requester.id to true, offerer.id to true)*/


    fun sendFirstMessage(cm: ChatMessage) {
        this.status = STATUS_INTERESTED
        lastMessage = cm

/*
        timeSlot.offererUnreadChats++
*/
        offererUnreadMsg = 1
    }


    @Exclude
    fun getType(): Int {
        /* If the current user is the requester, the chat will be a chat to an offer*/
        return if(requester.id == Firebase.auth.uid) CHAT_TYPE_TO_OFFERER else CHAT_TYPE_TO_REQUESTER
    }


    @Exclude
    fun isEmpty() = this.requestId == "_"


    fun toStatus(new_status: Int) {
        this.status = new_status

        when(new_status) {
            STATUS_UNINTERESTED -> { timeSlot.status = TimeSlot.TIME_SLOT_STATUS_AVAILABLE; timeSlot.assignedTo = CompactUser()}
            STATUS_ACCEPTED -> { timeSlot.status = TimeSlot.TIME_SLOT_STATUS_ASSIGNED; timeSlot.assignedTo = requester}
            STATUS_COMPLETED -> timeSlot.status = TimeSlot.TIME_SLOT_STATUS_COMPLETED
            else -> timeSlot.status = TimeSlot.TIME_SLOT_STATUS_AVAILABLE
        }
    }

    companion object {
        const val CHAT_TYPE_TO_REQUESTER = 0
        const val CHAT_TYPE_TO_OFFERER = 1

        const val STATUS_UNINTERESTED = -1
        const val STATUS_INTERESTED = 0
        const val STATUS_ACCEPTED = 1
        const val STATUS_DISCARDED = 2
        const val STATUS_COMPLETED = 3
    }
}

