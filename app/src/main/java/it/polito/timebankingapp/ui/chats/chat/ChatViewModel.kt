package it.polito.timebankingapp.ui.chats.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

import it.polito.timebankingapp.model.Helper.Companion.makeRequestId
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
    private lateinit var chatListener: ListenerRegistration




    override fun onCleared() {
        if (::messagesListener.isInitialized)
            messagesListener.remove()
        chatListener.remove()
        super.onCleared()
    }


    fun sendMessageAndUpdate(chat: Chat, message: ChatMessage) {

        val reqDocRef = db.collection("requests").document(chat.requestId)
        val msgsDocRef =
            db.collection("requests").document(chat.requestId).collection("messages").document()
        val timeSlotDocRef = db.collection("timeSlots").document(chat.timeSlot.id)

        if (chat.status == Chat.STATUS_UNINTERESTED){ //first message
            reqDocRef.set(chat.copy(status = Chat.STATUS_INTERESTED)) //write chat/request for the first time
            updateChat(reqDocRef) //register listeners
        }

        msgsDocRef.set(message) //write message in db
    }





    /* Coming from a chatListFragment -> download just the messages */
    fun registerMessagesListener(chat: Chat) {
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
    fun selectChatFromTimeSlot(timeSlot: TimeSlot, requester: CompactUser) {
        val chatId = makeRequestId(timeSlot.id, Firebase.auth.uid!!)
        val chatRef = db.collection("requests").document(chatId)

        Log.d(
            "selectChatFromTimeSlot",
            "timeSlot: $timeSlot requester: $requester, offerer: ${timeSlot.offerer}"
        )

        /* If the request already exists, download it, otherwise just download offerer profile and don't make the request. */
        chatRef.get().addOnSuccessListener { docSnapShot ->
            if (docSnapShot.exists()) {

                updateChat(chatRef) //update (without registering) chat + messages

                /*Download user profile *//*
                updateChatInfo(chatRef)

                *//* Download messages *//*
                messagesListener = chatRef.collection("messages").orderBy("timestamp")
                    .addSnapshotListener { v, e ->
                        if (e == null) {
                            _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
                        } else
                            _chatMessages.value = emptyList()
                    }*/
            } else {
                clearMessages()
                updateChatInfo(timeSlot, requester)
            }
        }
    }

    /* Register two listeners for the current chat */
    private fun updateChat(chatRef: DocumentReference) {
        Log.d(TAG, "updateChat...")
        chatListener = chatRef.addSnapshotListener {v,e ->
            if(e==null){
                _chat.postValue(v!!.toObject<Chat>())
                //_isLoading.postValue(false) /* ...line 115 */
                messagesListener = chatRef.collection("messages").orderBy("timestamp").addSnapshotListener { v2, e2 ->
                    if (e2 == null) {
                        _chatMessages.postValue(v2!!.mapNotNull { d -> d.toChatMessage() })
                    } else
                        _chatMessages.postValue(emptyList())
                }
            }
            else
                _chat.postValue(Chat())

        }
    }

    fun selectChatFromChatList(chat: Chat) {
        /* remember to register listener */
        //_chat.postValue(chat)
        updateChat(db.collection("requests").document(chat.requestId))
    }


    /* Update chatInfo (chat not yet created on db) from users table given a timeSlot*/
    fun updateChatInfo(
        timeSlot: TimeSlot,
        currentUser: CompactUser,
        requestService: Boolean = false
    ) {
        var user: User

        val chat = Chat(
            timeSlot = timeSlot,
            offerer = timeSlot.offerer,
            requester = currentUser,
            status = Chat.STATUS_UNINTERESTED
        )

        Log.d(TAG, "Update chat info...")
        _chat.postValue(chat)

//        Log.d("ChatViewModel", "chat: ${_chat.value!!}")
        if (requestService)
            requestService(chat)
    }

    fun clearChat() {
        _chat.postValue(Chat())
        _chatMessages.postValue(listOf())
    }

    fun clearMessages() {
        _chatMessages.value = listOf()
    }

    /* Function called when Request Service button is pressed */
    fun requestService(chat: Chat) {
//        Log.d("chatViewModel", _chat.value!!.toString())
        //_isLoading.postValue(true)
        val requestRef = db.collection("requests").document(chat.requestId)
        val interestedChat = chat.copy(
            status = Chat.STATUS_INTERESTED
        )
        updateChat(requestRef) //Update all the listeners

        requestRef.set(interestedChat).addOnSuccessListener { v ->
            /*_chat.postValue(interestedChat)
            _isLoading.postValue(false)

            registerChatListener(requestRef)
            registerMessagesListener(interestedChat)*/
            Log.d(TAG, "Status successfully changed to interested!")

        }
        val msg = ChatMessage(
            messageText = Helper.requestMessage(interestedChat),
            userId = interestedChat.requester.id,
            timestamp = Date()
        )
        sendMessageAndUpdate(interestedChat, msg)

    }

    /* Function that changes chat status*/
    fun updateStatus(chatId: String, newStatus: Int) {
        //_isLoading.postValue(true)
        val reqDocRef = db.collection("requests")

        reqDocRef.document(chatId).update(mapOf("status" to newStatus)).addOnSuccessListener {
            Log.d(TAG, "Status successfully changed to $newStatus")
        }
    }


    fun acceptRequest(chat: Chat): Task<Boolean> {
        /* query the db */
        val chatId = chat.requestId
        val reqTsDocs =
            db.collection("requests").whereEqualTo("timeSlot.id", Helper.extractTimeSlotId(chatId))
        val reqDocRef = db.collection("requests").document(chatId)
        val tsDocRef = db.collection("timeSlots").document(chat.timeSlot.id)
        val offererDocRef = db.collection("users").document(chat.offerer.id)
        val requesterDocRef = db.collection("users").document(chat.requester.id)

        val timeSlotsDocRef = db.collection("timeSlots")


        reqDocRef.update(mapOf("status" to Chat.STATUS_ACCEPTED))
        return db.runTransaction { transaction ->
            val snapshot = transaction.get(reqDocRef)
            val duration = snapshot.getString("timeSlot.duration")?.toInt()!!
            val newBalance = snapshot.getLong("requester.balance")!! - duration


            if (newBalance < 0) { //Not enough balance => Backtrack
                /*transaction.update(reqDocRef, "status", Chat.STATUS_INTERESTED)
                */
                val updatedChat = chat.copy(status=Chat.STATUS_INTERESTED, timeSlot=chat.timeSlot.copy(status=TimeSlot.TIME_SLOT_STATUS_AVAILABLE))
                transaction.set(reqDocRef, updatedChat)
                transaction.update(tsDocRef, "assignedTo", CompactUser())
                transaction.update(tsDocRef, "status", TimeSlot.TIME_SLOT_STATUS_AVAILABLE)
                //_chat.postValue(chat.copy(status = Chat.STATUS_INTERESTED))
                false
                //newBalance
            } else { //Balance is ok => transfer credit => update references
                Log.d(TAG, "balance is okay: $newBalance")

                //transaction.update(reqDocRef,"status", Chat.STATUS_ACCEPTED)
                transaction.update(reqDocRef,"timeSlot.status", TimeSlot.TIME_SLOT_STATUS_ASSIGNED)
                transaction.update(reqDocRef, "timeSlot.assignedTo", chat.requester)


                transaction.update(tsDocRef, "status", TimeSlot.TIME_SLOT_STATUS_ASSIGNED)
                transaction.update(tsDocRef, "assignedTo", chat.requester)


                transaction.update(
                    reqDocRef,
                    "requester.balance",
                    newBalance
                ) //TODO(Delete if u have time)
                transaction.update(reqDocRef, "timeSlot.requester.balance", newBalance)
                transaction.update(
                    reqDocRef,
                    "offerer.balance",
                    FieldValue.increment(duration.toLong())
                )
                transaction.update(
                    offererDocRef,
                    "balance",
                    FieldValue.increment(duration.toLong())
                )
                transaction.update(requesterDocRef, "balance", newBalance)


                /* !!!!tocheck ==> */
                true //newBalance
            }
            // commento
            }.addOnSuccessListener {
            reqTsDocs.get().addOnSuccessListener { reqs-> //aggiorno copie di timeSlots all'interno di chats/requests
                if(it == true) {
                    db.runBatch { batch ->
                        for (doc in reqs.documents) {
                            Log.d(TAG, "ci passo: ${doc.reference}")
                            if (doc.get("requestId") != chatId)
                                batch.update(
                                    doc.reference,
                                    mapOf(
                                        "status" to Chat.STATUS_DISCARDED,
                                        "assignedTo" to chat.requester
                                    )
                                )

                        }
                    }
                }

        } }
    }

    fun discardRequest(chatId: String) {
        updateStatus(chatId, Chat.STATUS_DISCARDED)
        /* query the db */
    }

    fun getChat(chatId: String): Task<DocumentSnapshot> {
        return db.collection("requests").document(chatId).get()
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


    companion object {
        const val TAG = "ChatViewModel"
    }

}
