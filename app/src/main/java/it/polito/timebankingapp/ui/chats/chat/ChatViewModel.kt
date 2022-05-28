package it.polito.timebankingapp.ui.chats.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.Helper.Companion.toUser
import it.polito.timebankingapp.model.Request
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.chat.ChatsListItem
import it.polito.timebankingapp.model.user.User
import java.util.*

class ChatViewModel(application: Application): AndroidViewModel(application)  {

    private val _status = MutableLiveData<Int>()
    val status: LiveData<Int> = _status

    private val _chatId = MutableLiveData<String>()
    val chatId : LiveData<String> = _chatId

    private val _otherUserName = MutableLiveData<String>()
    val otherUserName: LiveData<String> = _otherUserName

    private val _otherProfilePic = MutableLiveData<String>()
    val otherProfilePic: LiveData<String> = _otherProfilePic

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _avgReviews = MutableLiveData<Float>()
    val avgReviews: LiveData<Float> = _avgReviews

    private val _nReviews = MutableLiveData<Int>()
    val nReviews: LiveData<Int> = _nReviews

    private val _chat = MutableLiveData<String>()
    val chat: LiveData<String> = _chat

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var messagesListener: ListenerRegistration
    private lateinit var requestListener: ListenerRegistration


    fun sendMessage(message: ChatMessage) {
        val requestRef = db.collection("requests").document(chatId.value!!)
        requestRef.collection("messages").document().set( mapOf(
            "messageText" to message.messageText,
            "timestamp" to message.timestamp.time,
            "userId" to message.userId,
        )).addOnSuccessListener {
            requestRef.update(mapOf ("lastMessageText" to message.messageText, "lastMessageTime" to message.timestamp.time))
            Log.d("sendMessage", "success")

        }.addOnFailureListener { Log.d("sendMessage", "failure")}
    }

    private fun QueryDocumentSnapshot.toChatMessage() : ChatMessage? {
        return try {
            //val messageId = get("messageId") as String
            val userId = get("userId") as String
            val messageText = get("messageText") as String
            val timestamp = get("timestamp") as Timestamp


            val cal = Calendar.getInstance()
            cal.time = timestamp.toDate()

            ChatMessage(userId,messageText, cal)
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /* Coming from a chatListFragment -> download just the messages */
    fun selectChat(chat: ChatsListItem) {
        _otherUserName.postValue(chat.userName)
        _otherProfilePic.postValue(chat.userPic)
        chat.timeSlotTitle

        messagesListener = db.collection("requests").document(chat.chatId).collection("messages").orderBy("timestamp")
            .addSnapshotListener { v, e ->
                if (e == null) {
                    _chatId.value = chat.chatId
                    _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
                } else
                    _chatMessages.value = emptyList()
            }
        }


    /* Coming from TimeSlotDetail, check if requests exists, otherwise download just the userProfile */
    fun selectChat(chatId: String) {
        _chatId.value = chatId

        val chatRef = db.collection("requests").document(chatId)

        /* If the chat already exists, download it, otherwise just download offerer profile. */
        chatRef.get().addOnSuccessListener { docSnapShot ->
            if(docSnapShot.exists()) {
                /*Download user profile */
                updateUserInfo(chatRef, chatId)

                /* Download messages */
                messagesListener = chatRef.collection("messages").orderBy("timestamp")
                    .addSnapshotListener{
                            v,e ->
                        if(e == null){
                            _chatId.value = chatId
                            _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
                        } else
                            _chatMessages.value = emptyList()
                    }
            }
            else
                updateUserInfo(Helper.extractRequesterId(chatId))
        }
    }


    fun updateUserInfo(userId: String) {
        var user: User?
        requestListener = db.collection("users").document(userId)
            .addSnapshotListener { v, e ->
                if (e == null) {
                    if (v != null) {
                        user = v.toUser()
                        _otherProfilePic.postValue(user?.pic)
                        _otherUserName.postValue(user?.fullName)
                    }
                }
            }
    }

    fun updateUserInfo(chatRef: DocumentReference, chatId: String) {
        requestListener = chatRef.addSnapshotListener { v, e ->
            if (e == null) {
                val req = v!!.toObject<Request>()
                if (req != null) {
                    val otherUser = Helper.getOtherUser(req)
                    _otherUserName.postValue(otherUser.fullName)
                    _otherProfilePic.postValue(otherUser.pic)
                    _status.postValue(req.status)
                } else {
                    Log.d("selectChat", "this should not happen")
                    throw Exception("chat not found in the DB!!!")
                }
            }
        }
    }

    fun clearChat(){
        _otherProfilePic.postValue("")
        _chatMessages.value = listOf()
    }



}
