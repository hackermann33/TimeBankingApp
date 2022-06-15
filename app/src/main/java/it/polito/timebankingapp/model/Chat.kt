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
    val requestId: String = timeSlot.id + "_" + requester.id
)
{




    /* This could be useless, put just to be safe*/
    val timeSlotId: String = timeSlot.id

    val users = listOf(requester.id, offerer.id)


    fun incUnreadMsg(): Chat {
        return this.copy(offererUnreadMsg = offererUnreadMsg+1)
    }

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

    fun sendMessage(message: ChatMessage) {
        if(status == STATUS_UNINTERESTED){ //FIRST MESSAGE, I am surely the Requester
            this.status = STATUS_INTERESTED
/*
            timeSlot.offererUnreadChats++
*/
            offererUnreadMsg = 1
        }
        else{ /* Existing Chat, I can be requester or offerer */
            if(this.lastMessage.userId != Firebase.auth.uid){ /* Have to update unreadChats */
                if(this.timeSlot.offerer.id == Firebase.auth.uid) { /* I am offerer, and I am answering */
/*
                    this.timeSlot.requesterUnreadChats++
*/
                    this.requesterUnreadMsg++
                }
                else{ /* I am requester, and I'm answering */
/*
                    this.timeSlot.offererUnreadChats++
*/
                    this.offererUnreadMsg++
                }
            }
            else{ /* don't have to update unreadChats, just unreadMsg */
                if(this.timeSlot.offerer.id == Firebase.auth.uid) /* I'm offerer */
                    this.requesterUnreadMsg++
                else
                    this.offererUnreadMsg++
            }

        }
        lastMessage = message
    }

    @Exclude
    fun isEmpty() = this.requestId == "_"

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

