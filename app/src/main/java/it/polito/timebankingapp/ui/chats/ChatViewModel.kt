package it.polito.timebankingapp.ui.chats

import android.app.Application
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.Helper.Companion.toUser
import it.polito.timebankingapp.model.Request
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.chat.ChatsListItem
import it.polito.timebankingapp.model.user.User
import java.text.SimpleDateFormat
import java.util.*


class ChatViewModel(application: Application): AndroidViewModel(application) {
    private val _chatId = MutableLiveData<String>()
    val chatId : LiveData<String>  = _chatId

    private val _otherUserName = MutableLiveData<String>()
    val otherUserName: LiveData<String> = _otherUserName

    private val _otherProfilePic = MutableLiveData<String>()
    val otherProfilePic: LiveData<String> = _otherProfilePic

    private val _chatsList = MutableLiveData<List<ChatsListItem>>()
    val chatsList : LiveData<List<ChatsListItem>> = _chatsList

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _avgReviews = MutableLiveData<Float>()
    val avgReviews: LiveData<Float> = _avgReviews

    private val _nReviews = MutableLiveData<Int>()
    val nReviews: LiveData<Int> = _nReviews

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var l: ListenerRegistration
    private lateinit var l2: ListenerRegistration

//    fun retrieveChatMessages(timeslotId: String, requesterId: String ){
//        l = db.collection("chats").document(timeslotId).collection(requesterId).orderBy("timestamp")
//            .addSnapshotListener {
//                v,e ->
//                if(e == null){
//                    _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
//                } else
//                    _chatMessages.value = emptyList()
//        }
//    }


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

//    fun addNewMessage(timeslotId: String, requesterId: String, cm : ChatMessage) {
//        //se la chat non esiste ancora, creane una nuova automaticamente
//        val newChatRef = db.collection("chats").document(timeslotId).collection(requesterId).document()
//
//        cm.messageId =newChatRef.id  //imposta id generato da firebase
//        cm.userId = Firebase.auth.currentUser?.uid ?: ""
//
//        newChatRef.set(cm).addOnSuccessListener{
//            Log.d("chat_create","Successfully added")
//        }.addOnFailureListener{
//            Log.d("timeSlots_add", "Error on adding")
//        }
//    }

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

    override fun onCleared() {
        super.onCleared()

        l.remove()
        if(this::l2.isInitialized)
            l2.remove()
    }

    fun cleanChats(){
        _chatMessages.value = listOf()
    }

    fun downloadChat() {

    }


    fun selectChat(chatId: String) {
        _chatId.value = chatId

        val chatRef = db.collection("requests").document(chatId)

        /* If the chat already exists, download it, otherwise just download offerer profile*/
        chatRef.get().addOnSuccessListener { docSnapShot ->
            if(docSnapShot.exists()) {
                /*Download user profile */
                updateUserInfo(chatRef, chatId)

                /* Download messages */
                l = chatRef.collection("messages").orderBy("timestamp")
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
        l2 = db.collection("users").document(userId)
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
        val chatItem: ChatsListItem? = _chatsList.value?.first { it -> it.chatId == chatId }
        if (chatItem != null) {
            _otherUserName.postValue(chatItem.userName)
            _otherProfilePic.postValue(chatItem.userPic)
        }
        else {
            l2 = chatRef.addSnapshotListener { v, e ->
                if (e == null) {
                    val req = v!!.toObject<Request>()
                    if (req != null) {
                        val otherUser = Helper.getOtherUser(req)
                        _otherUserName.postValue(otherUser.fullName)
                        _otherProfilePic.postValue(otherUser.pic)
                    } else {
                        Log.d("selectChat", "this should not happen")
                        throw Exception("chat not found in the DB!!!")
                    }
                }
            }

        }
    }



    fun updateAllChats() {
           Log.d("User", Firebase.auth.uid.toString())
            val currentId = Firebase.auth.uid.toString()
            l = db.collection("requests").whereArrayContains("users","${Firebase.auth.uid}")
                .orderBy("lastMessageTime", Query.Direction.DESCENDING).addSnapshotListener{ v, e ->
                    if(e == null){
                        Log.d("chatList", "chatList: ${_chatsList.value}")
                        val requests = v!!.mapNotNull {  d -> d.toObject<Request>()  }
                        _chatsList.value = requests.mapNotNull {  r ->
                            val otherUser = Helper.getOtherUser(r)
                            val timeStr = r.lastMessageTime.toDisplayString()
                            val userId = Firebase.auth.uid.toString()
                            ChatsListItem(r.requestId, userId,  r.timeSlot.id, r.timeSlot.title,  otherUser.fullName, otherUser.pic, r.lastMessageText, timeStr )}
                        Log.d("chatsListValue", "success")
                    } else{
                        _chatsList.value = emptyList()
                        Log.d("chatsListValue", "failed")
                    }
                }
        }


    /* Download all the chat related to a specific offer that current user has published */
    fun downloadTimeSlotChats(tsId: String) {
//        Log.d("showRequests", "Arrived at ViewModel $tsId")
        Log.d("User", Firebase.auth.uid.toString())
        l = db.collection("requests").whereEqualTo("offerer.id",Firebase.auth.uid.toString()).whereEqualTo("timeSlot.id", tsId)
            .addSnapshotListener{v,e ->
            if(e == null){
                Log.d("chatList", "chatList: ${_chatsList.value}")
                val req = v!!.mapNotNull {  d -> d.toObject<Request>()  }
                _chatsList.value = req.mapNotNull { r ->
                    val timeStr = r.lastMessageTime.toDisplayString()
                    val userId = Firebase.auth.uid.toString()

                    ChatsListItem(r.requestId, userId, r.timeSlot.id, r.timeSlot.title, r.requester.fullName, r.requester.pic, r.lastMessageText, timeStr)
                }
                Log.d("chatsListValue", "chatList: ${_chatsList.value}")
            } else{
                _chatsList.value = emptyList()
                Log.d("chatsListValue", "failed")
            }
        }
    }
}


private fun Date.toDisplayString(): String {

    var pattern: String = when {
        Helper.isYesterday(this) -> return "yesterday"
        DateUtils.isToday(this.time) -> "HH:mm"
        else -> "dd/MM/yy"
    }

    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(this)

}
