package it.polito.timebankingapp.ui.chats.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

import it.polito.timebankingapp.model.Helper.Companion.makeRequestId
import it.polito.timebankingapp.model.Helper.Companion.toUser
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.CompactUser
import it.polito.timebankingapp.model.user.User
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _chat = MutableLiveData<Chat>()
    val chat: LiveData<Chat> = _chat


    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var messagesListener: ListenerRegistration
    private lateinit var otherUserListener: ListenerRegistration


    /* Function invoked when first message is sent ( and the request isn't been created)*/
    fun sendFirstMessage(message: ChatMessage) {
        val req = _chat.value!!.copy(status = Chat.STATUS_INTERESTED)
        val requestRef = db.collection("requests").document(chat.value!!.requestId)
        requestRef.set(req).addOnSuccessListener {
            assert(_chat.value!!.status == Chat.STATUS_UNINTERESTED)

            requestRef.collection("messages").document().set(
                message
            ).addOnSuccessListener {
                /*update unreadChats*/
                db.collection("timeSlots").document(chat.value!!.timeSlotId).update("unreadChats", FieldValue.increment(1))

                _chatMessages.postValue(listOf(message))
                registerMessagesListener(req)
            }
        }.addOnFailureListener {
            Log.d("sendMessages", "$it")
        }
    }


    override fun onCleared() {
        if (::messagesListener.isInitialized)
            messagesListener.remove()
        otherUserListener.remove()
        super.onCleared()
    }


    fun sendMessageAndUpdate(message: ChatMessage){
        val cid = chat.value!!
        val reqDocRef =  db.collection("requests").document(cid.requestId)
        val msgsDocRef =  db.collection("requests").document(cid.requestId).collection("messages").document()
        val timeSlotDocRef = db.collection("timeSlots").document(cid.timeSlotId)

        cid.sendMessage(message)

        db.runBatch { batch ->
            batch.set(reqDocRef, cid)
            batch.set(msgsDocRef, message)
            batch.update(timeSlotDocRef,"unreadChats", FieldValue.increment(1))
        }.addOnSuccessListener {

            if(!::messagesListener.isInitialized)
                registerMessagesListener(cid)
            Log.d("sendMessageAndUpdate","Everything updated")
        }.addOnFailureListener{
            Log.d("sendMessageAndUpdate","Oh, no")
        }
    }


    fun sendMessage(message: ChatMessage) {
        val requestRef = db.collection("requests").document(chat.value!!.requestId)
        requestRef.get().addOnSuccessListener { /* Chat correctly taken -> sendTheMessage */
            /*_chat.value =
                chat.value!!.copy(unreadMsgs = it.getLong("unreadMsgs")?.toInt() ?: 0)
                Log.d("sendMessage", "{$_chat.value}")
*/
            requestRef.collection("messages").document().set(
                mapOf(
                    "messageText" to message.messageText,
                    "timestamp" to message.timestamp.time,
                    "userId" to message.userId,
                )
            ).addOnSuccessListener { //Message Correctly sent -> updateLastMessageField

                requestRef.update(
                    mapOf(
                        "lastMessageText" to message.messageText,
                        "lastMessageTime" to message.timestamp.time,
                        "unreadMsgs" to chat.value!!.unreadMsgs + 1,
                    )
                ).addOnSuccessListener { //Field correctly updated -> update View Model
                    //_chat.value = chat.value!!.incUnreadMsg()
                    _chat.value!!.incUnreadMsg()

                    Log.d("sendMessage", "success")
                }
            }.addOnFailureListener { Log.d("sendMessage", "failure") }
        }
    }

    private fun QueryDocumentSnapshot.toChatMessage(): ChatMessage? {
        return try {
            //val messageId = get("messageId") as String
            val userId = get("userId") as String
            val messageText = get("messageText") as String
            val timestamp = get("timestamp") as Timestamp


            val cal = Calendar.getInstance()
            cal.time = timestamp.toDate()

            ChatMessage(userId, messageText/*, cal*/)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /* Coming from a chatListFragment -> download just the messages */
    fun registerMessagesListener(chat: Chat) {
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
    fun selectChatFromTimeSlot(timeSlot: TimeSlot, otherUser: CompactUser) {
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
                updateChatInfo(timeSlot, otherUser)
            }
        }
    }

    /* Update chatInfo from users table given a timeSlot*/
    fun updateChatInfo(timeSlot: TimeSlot, otherUser: CompactUser) {
        var user: User
        otherUserListener = db.collection("users").document(timeSlot.userId)
            .addSnapshotListener { v, e ->
                if (e == null) {
                    if (v != null) {
                        user =
                            v.toUser()!! /* If u get an exception here, something is not updated in db */

                        _chat.value = Chat().copy(
                            timeSlot = timeSlot,
                            offerer = user.toCompactUser(),
                            requester = otherUser,
                            status = Chat.STATUS_UNINTERESTED,
                        )
                    }
                }
            }
    }

    /* Update userInfo from the requests table */
    fun updateChatInfo(chatRef: DocumentReference) {
        otherUserListener = chatRef.addSnapshotListener { v, e ->
            if (e == null) {
                val req = v!!.toObject<Chat>()
                if (req != null) {
                    _chat.value = (req)

                } else {
                    Log.d("selectChat", "this should not happen")
                    throw Exception("chat not found in the DB!!!")
                }
            }
        }
    }

    fun clearChat() {
        _chat.postValue(Chat())
        _chatMessages.value = listOf()
    }

    fun clearMessages() {
        _chatMessages.value = listOf()
    }

    /* Function to call to get Interested */
    fun requestService() {
        val requestRef = db.collection("requests").document(chat.value!!.requestId)
        val cli = _chat.value!!
        val req = cli.copy(
            status = Chat.STATUS_INTERESTED
        )

        requestRef.set(req).addOnSuccessListener{ v ->
            sendMessageAndUpdate(ChatMessage(messageText = Helper.requestMessage(cli), userId = cli.requester.id, timestamp = Date()))
        }
    }

    fun acceptRequest() {
        _chat.value = chat.value?.copy(status=Chat.STATUS_ACCEPTED)
    }

    fun discardRequest() {
        _chat.value = chat.value?.copy(status=Chat.STATUS_DISCARDED)
    }

}
