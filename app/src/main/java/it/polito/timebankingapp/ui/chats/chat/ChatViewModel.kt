package it.polito.timebankingapp.ui.chats.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.Helper.Companion.fromRequestToChat

import it.polito.timebankingapp.model.Helper.Companion.makeRequestId
import it.polito.timebankingapp.model.Helper.Companion.toUser
import it.polito.timebankingapp.model.ChatsListItem
import it.polito.timebankingapp.model.ChatsListItem.Companion.STATUS_INTERESTED
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.timeslot.CompactTimeSlot
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.CompactUser
import it.polito.timebankingapp.model.user.User
import java.util.*

class ChatViewModel(application: Application): AndroidViewModel(application) {

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _chat = MutableLiveData<ChatsListItem>()
    val chat: LiveData<ChatsListItem> = _chat


    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var messagesListener: ListenerRegistration
    private lateinit var requestListener: ListenerRegistration


    /* Function invoked when first message is sent ( and the request isn't been created)*/
    fun sendFirstMessage(message: ChatMessage) {
        val requestRef = db.collection("requests").document(chat.value!!.requestId)
        requestRef.addSnapshotListener { v, e ->
            if (e == null) {
                if (!v!!.exists()) {/* Creation of the request (status interested) because of a message*/
                    assert(_chat.value!!.status == ChatsListItem.STATUS_UNINTERESTED)

                    val req = _chat.value!!

                    requestRef.set(req).addOnSuccessListener {
                        _chat.value = fromRequestToChat(req)

                        requestRef.collection("messages").document().set(
                            mapOf(
                                "messageText" to message.messageText,
                                "timestamp" to message.timestamp.time,
                                "userId" to message.userId,
                            )
                        )
                    }.addOnFailureListener {
                        Log.d("sendMessages", "$it")
                    }
                }
            }
        }
    }

    fun sendMessage(message: ChatMessage) {

        val requestRef = db.collection("requests").document(chat.value!!.requestId)
        requestRef.addSnapshotListener { v, e ->
            if (e == null) {
                _chat.value = chat.value!!.copy(nUnreadMsgs = v!!.getLong("unreadMsgs")?.toInt() ?: 0)
            }
        }

        requestRef.collection("messages").document().set(
            mapOf(
                "messageText" to message.messageText,
                "timestamp" to message.timestamp.time,
                "userId" to message.userId,
            )
        ).addOnSuccessListener {

            /*  TODO(Check this nUnreadMsg increment: How can you distinguish
                 between yout unreadmsg and the otherUnreadMsg?)*/
            requestRef.update(
                mapOf(
                    "lastMessageText" to message.messageText,
                    "lastMessageTime" to message.timestamp.time,
                    "unreadMsg" to chat.value!!.nUnreadMsgs + 1,
                )
            )
            _chat.value = chat.value!!.incUnreadMsg()
            Log.d("sendMessage", "success")

        }.addOnFailureListener { Log.d("sendMessage", "failure") }
    }

    private fun QueryDocumentSnapshot.toChatMessage(): ChatMessage? {
        return try {
            //val messageId = get("messageId") as String
            val userId = get("userId") as String
            val messageText = get("messageText") as String
            val timestamp = get("timestamp") as Timestamp


            val cal = Calendar.getInstance()
            cal.time = timestamp.toDate()

            ChatMessage(userId, messageText, cal)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /* Coming from a chatListFragment -> download just the messages */
    fun selectChat(chat: ChatsListItem) {
        _chat.value = chat

        messagesListener = db.collection("requests").document(chat.requestId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { v, e ->
                if (e == null) {
                    _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
                } else
                    _chatMessages.value = emptyList()
            }
    }


    /* Coming from TimeSlotDetail or TimeSlotList, check if requests from current user to timeSlot exists, otherwise download just the userProfile */
    fun selectChatFromTimeSlot(timeSlot: TimeSlot, currentUser: CompactUser) {
        val chatId = makeRequestId(timeSlot.id, Firebase.auth.uid!!)
        val chatRef = db.collection("requests").document(chatId)

        /* If the request already exists, download it, otherwise just download offerer profile and don't make the request. */
        chatRef.get().addOnSuccessListener { docSnapShot ->
            if (docSnapShot.exists()) {
                /*Download user profile */
                updateChatInfo(chatRef)

                /* Download messages */
                messagesListener = chatRef.collection("messages").orderBy("timestamp")
                    .addSnapshotListener { v, e ->
                        if (e == null) {
                            _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
                        } else
                            _chatMessages.value = emptyList()
                    }
            } else {
                clearMessages()
                updateChatInfo(timeSlot, currentUser)
            }
        }
    }

    /* Update chatInfo from users table given a timeSlot*/

    /*TODO( Quando metteremo le recensioni nello user qui dovranno essere aggiunte)*/
    fun updateChatInfo(timeSlot: TimeSlot, currentUser: CompactUser) {
        var user: User
        requestListener = db.collection("users").document(timeSlot.userId)
            .addSnapshotListener { v, e ->
                if (e == null) {
                    if (v != null) {
                        user = v.toUser()!! /* If u get an exception here, something is not updated in db */

                        _chat.value = ChatsListItem().copy(
                            timeSlot = timeSlot,
                            offerer = user.toCompactUser(),
                            requester = currentUser,
                            status = ChatsListItem.STATUS_UNINTERESTED,
                            type = ChatsListItem.CHAT_TYPE_TO_OFFERER
                        )
                    }
                }
            }
    }

    /* Update userInfo from the requests table */
    fun updateChatInfo(chatRef: DocumentReference) {
        requestListener = chatRef.addSnapshotListener { v, e ->
            if (e == null) {
                val req = v!!.toObject<ChatsListItem>()
                if (req != null) {
                    //Helper.getChatItem(req)
                    val otherUser = Helper.getOtherUser(req)

                    _chat.value = Helper.fromRequestToChat(req)


                } else {
                    Log.d("selectChat", "this should not happen")
                    throw Exception("chat not found in the DB!!!")
                }
            }
        }
    }

    fun clearChat() {
        _chat.postValue(ChatsListItem())
        _chatMessages.value = listOf()
    }

    fun clearMessages() {
        _chatMessages.value = listOf()
    }

    fun requestService() {
        val requestRef = db.collection("requests").document(chat.value!!.requestId)

        db.collection("requests").document(chat.value!!.requestId).addSnapshotListener() { v, e ->
            assert(_chat.value!!.status == ChatsListItem.STATUS_UNINTERESTED)
            val cli = _chat.value!!

            val req = cli.copy(
                status = ChatsListItem.STATUS_INTERESTED
            )

            requestRef.set(req).addOnSuccessListener {
                _chat.value = fromRequestToChat(req)
            }
        }
    }
}
