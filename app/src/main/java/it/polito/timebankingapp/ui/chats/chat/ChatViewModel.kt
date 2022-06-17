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

        if (chat.status == Chat.STATUS_UNINTERESTED) { //first message
            reqDocRef.set(chat.copy(status = Chat.STATUS_INTERESTED)) //write chat/request for the first time
            updateChat(reqDocRef) //register listeners
        }

        msgsDocRef.set(message) //write message in db
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
            if (docSnapShot.exists())
                updateChat(chatRef) //update (without registering) chat + messages
            else {
                clearMessages()
                createUninterestedChat(timeSlot, requester)
            }
        }
    }

    /* Register two listeners for the current chat */
    private fun updateChat(chatRef: DocumentReference) {
        Log.d(TAG, "updateChat...")
        chatListener = chatRef.addSnapshotListener { v, e ->
            if (e == null) {
                if(!v!!.exists())
                    _chat.postValue(_chat.value!!.apply { toStatus(Chat.STATUS_UNINTERESTED) })
                else {
                    _chat.postValue(v.toObject<Chat>())
                    Log.d(TAG,"updateChat v: $v")
                }
            } else
                _chat.postValue(Chat())
        }

        messagesListener =
            chatRef.collection("messages").orderBy("timestamp").addSnapshotListener { v2, e2 ->
                if (e2 == null) {
                    _chatMessages.postValue(v2!!.mapNotNull { d -> d.toChatMessage() })
                } else
                    _chatMessages.postValue(emptyList())
            }
    }

    fun selectChatFromChatList(chat: Chat) {
        /* remember to register listener */
        updateChat(db.collection("requests").document(chat.requestId))
    }

    /* TODO(When UNINTERESTED LiveUpdate are not available. Substitute this two paramenters with 2 id's
        and add 2 db queries in order to implement it.)*/

    /* Create an uninterested chat given a timeSlot and the current user */
    fun createUninterestedChat(
        timeSlot: TimeSlot,
        currentUser: CompactUser,
    ) {

        val tmpChat = Chat(
            timeSlot = timeSlot,
            offerer = timeSlot.offerer,
            requester = currentUser,
            status = Chat.STATUS_UNINTERESTED
        )

        Log.d(TAG, "Update chat info...")
        _chat.postValue(tmpChat) /* When a chat is uninterested, is not written in db yet.*/
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

        val requestRef = db.collection("requests").document(chat.requestId)

        val interestedChat: Chat = chat.apply { toStatus(Chat.STATUS_INTERESTED) }

        updateChat(requestRef) //Update all the listeners

        requestRef.set(interestedChat)

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


    fun acceptRequestAndUpdate(currentChat: Chat): Task<Boolean> {
        /* query the db */
        val currentChatId = currentChat.requestId
        val otherTimeSlotsChats =
            db.collection("requests")
                .whereEqualTo("timeSlot.id", Helper.extractTimeSlotId(currentChatId))
                .whereNotEqualTo(
                    "requester.id", currentChat.requester.id
                )
        val currentChatDocRef = db.collection("requests").document(currentChatId)
        val currentTimeSlotDocRef = db.collection("timeSlots").document(currentChat.timeSlot.id)
        val offererDocRef = db.collection("users").document(currentChat.offerer.id)
        val requesterDocRef = db.collection("users").document(currentChat.requester.id)

        /* update status and timeSlot(ref) in Chat */
        val acceptedChat = currentChat.toStatus(Chat.STATUS_ACCEPTED)

        /* 1. Set to Accept */
        acceptRequestAndUpdate(
            currentChatDocRef,
            currentTimeSlotDocRef,
            currentChat,
            otherTimeSlotsChats
        ).addOnSuccessListener { Log.d(TAG,"acceptRequestAndUpdateSuccess") }.addOnFailureListener{ Log.d(TAG, "acceptRequestAndUpdateFailure")}

        return db.runTransaction { transaction ->
            val snapshot = transaction.get(currentChatDocRef)
            val duration = snapshot.getString("timeSlot.duration")?.toInt()!!
            val newBalance = snapshot.getLong("requester.balance")!! - duration

            if (newBalance < 0) { //Not enough balance => Backtrack
                /*transaction.update(reqDocRef, "status", Chat.STATUS_INTERESTED)
            */
                /* Delete requests and update*/
                acceptRequestAndUpdate(
                    currentChatDocRef,
                    currentTimeSlotDocRef,
                    currentChat,
                    otherTimeSlotsChats,
                    true
                )


                /*val updatedChat = currentChat.copy(
                    status = Chat.STATUS_INTERESTED,
                    timeSlot = currentChat.timeSlot.copy(status = TimeSlot.TIME_SLOT_STATUS_AVAILABLE)
                )
                transaction.update(currentChatDocRef, "status",  Chat.STATUS_INTERESTED)
                transaction.update(currentChatDocRef, "timeSlot.status",  TimeSlot.TIME_SLOT_STATUS_AVAILABLE)
                transaction.update(currentChatDocRef, "timeSlot.assignedTo",  CompactUser())

                transaction.update(currentTimeSlotDocRef, "assignedTo", CompactUser())
                transaction.update(
                    currentTimeSlotDocRef,
                    "status",
                    TimeSlot.TIME_SLOT_STATUS_AVAILABLE
                )
*/
                //_chat.postValue(chat.copy(status = Chat.STATUS_INTERESTED))
                false
                //newBalance
            } else { //Balance is ok => transfer credit => update references
                Log.d(TAG, "balance is okay: $newBalance")

                /*//transaction.update(reqDocRef,"status", Chat.STATUS_ACCEPTED)
                transaction.update(
                    currentChatDocRef,
                    "timeSlot.status",
                    TimeSlot.TIME_SLOT_STATUS_ASSIGNED
                )
                transaction.update(
                    currentChatDocRef,
                    "timeSlot.assignedTo",
                    currentChat.requester
                )


                transaction.update(
                    currentTimeSlotDocRef,
                    "status",
                    TimeSlot.TIME_SLOT_STATUS_ASSIGNED
                )
                transaction.update(currentTimeSlotDocRef, "assignedTo", currentChat.requester)*/


                transaction.update(
                    currentChatDocRef,
                    "requester.balance",
                    newBalance
                ) //TODO(Delete if u have time)
                transaction.update(currentChatDocRef, "timeSlot.requester.balance", newBalance)
                transaction.update(
                    currentChatDocRef,
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


            /*.addOnSuccessListener {
                    otherTimeSlotsChats.get()
                        .addOnSuccessListener { reqs -> //aggiorno copie di timeSlots all'interno di chats/requests
                            if (it == true) {
                                updateAllReferences(reqs, currentChatId, currentChat)
                            }

                        }
                }
    */
        }
    }


    private fun acceptRequestAndUpdate(
        currentChatDocRef: DocumentReference,
        currentTimeSlotDocRef: DocumentReference,
        currentChat: Chat,
        otherTimeSlotsChats: Query,
        undo: Boolean = false,
    ): Task<Void> {
        /* 1. Update current chat and current timeSlot */
        var newChatStatus: Int = Chat.STATUS_ACCEPTED
        var newTimeSlotStatus: Int = TimeSlot.TIME_SLOT_STATUS_ASSIGNED
        var newRequester = currentChat.requester
        var newOtherChatStatus = Chat.STATUS_DISCARDED
        if (undo) {
            newTimeSlotStatus = TimeSlot.TIME_SLOT_STATUS_AVAILABLE
            newRequester = CompactUser()
            newOtherChatStatus = Chat.STATUS_INTERESTED
        }
        return db.runBatch { batch ->

            if (!undo) {
                batch.update(currentChatDocRef, "status", newChatStatus)
                batch.update(currentChatDocRef, "timeSlot.status", newTimeSlotStatus)
                batch.update(currentChatDocRef, "timeSlot.assignedTo", newRequester)
            } else {

                batch.delete(currentChatDocRef)
            }
            /* Update timeSlot in timeSlot collection */
            batch.update(currentTimeSlotDocRef, "status", newTimeSlotStatus)
            batch.update(currentTimeSlotDocRef, "assignedTo", newRequester)

        }.addOnSuccessListener {
            currentChatDocRef.collection("messages").get().addOnSuccessListener {
                db.runBatch {
                        batch ->
                        it.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }
                }
            }

        }   /*2. Update references ( in table requests )*/
            .addOnSuccessListener {

                /* Update doc.timeSlot references collection requests */
                otherTimeSlotsChats.get().addOnSuccessListener {
                    db.runBatch { batch ->
                        for (doc in it.documents) {
                            batch.update(doc.reference, "status", newOtherChatStatus)
                            batch.update(
                                doc.reference,
                                "timeSlot.status",
                                newTimeSlotStatus
                            )
                            batch.update(
                                doc.reference,
                                "timeSlot.assignedTo",
                                newRequester
                            )
                        }
                    }
                }.addOnFailureListener {
                    Log.d(TAG, "$it")
                }
            }
    }

    private fun updateAllReferences(
        reqs: QuerySnapshot,
        chatId: String,
        chat: Chat
    ) {
        db.runBatch { batch ->
            for (doc in reqs.documents) {
                Log.d(TAG, "ci passo: ${doc.reference}")
                if (doc.get("requestId") != chatId) /* Se è diversa dalla chat corrente */
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

    fun discardRequest(chatId: String) {
        updateStatus(chatId, Chat.STATUS_DISCARDED)
        /* query the db */
    }

    fun getChat(chatId: String): Task<DocumentSnapshot> {
        return db.collection("requests").document(chatId).get()
    }

    private fun QueryDocumentSnapshot.toChatMessage(): ChatMessage? {
        return try {
            // messageId = get("messageId") as String
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
