package it.polito.timebankingapp.ui.chats

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
import it.polito.timebankingapp.model.Request
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.chat.ChatsListItem
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
            "userName" to message.userName
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
            val userName = get("userName") as String


            val cal = Calendar.getInstance()
            cal.time = timestamp.toDate()

            ChatMessage(userId,messageText, cal, userName)
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        l.remove()
    }

    fun cleanChats(){
        _chatMessages.value = listOf()
    }

    fun selectChat(chatId: String) {
        _chatId.value = chatId
        val chatItem : ChatsListItem?
        val chatRef = db.collection("requests").document(chatId)


        /* This means that I've already downloaded some chats so try to found that
        * chat here */
        if(chatsList.value != null){
            chatItem = chatsList.value!!.find { it.chatId == chatId }
            if (chatItem != null) {
                _otherUserName.postValue( chatItem.userName)
                _otherProfilePic.postValue(chatItem.userPic)
                /* Here update also reviews*/
            }
            else {
                downloadChat(chatId)
            }
        }
        else {
            downloadChat(chatId)
        }

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

    fun downloadChat(chatId: String) {
        val chatRef = db.collection("requests").document(chatId)
        chatRef.addSnapshotListener {
                v,e ->
            if(e == null) {
                val req = v!!.toObject<Request>()
                if(req != null) {
                    val otherUser = Helper.getOtherUser(req)
                    _otherUserName.postValue(otherUser.fullName)
                    _otherProfilePic.postValue(otherUser.pic)
                }
                else {
                    Log.d("selectChat", "this should not happen")
                    throw Exception("chat not found in the DB!!!")
                }
            }
        }
    }

//    not used
    fun updateAllChats() {
           Log.d("User", Firebase.auth.uid.toString())
            val currentId = Firebase.auth.uid.toString()
            l = db.collection("requests").whereArrayContains("users","${Firebase.auth.uid}")
                .addSnapshotListener{v,e ->
                    if(e == null){
                        Log.d("chatList", "chatList: ${_chatsList.value}")
                        val requests = v!!.mapNotNull {  d -> d.toObject<Request>()  }
                        _chatsList.value = requests.mapNotNull {  r ->
                            val otherUser = Helper.getOtherUser(r)
                            val timeStr = r.lastMessageTime.toDisplayString()
                            val userId = Firebase.auth.uid.toString()
                            ChatsListItem(r.requestId, userId,  r.timeSlot.id, otherUser.fullName, otherUser.pic, r.lastMessageText, timeStr )}
                        Log.d("chatsListValue", "success")
                    } else{
                        _chatsList.value = emptyList()
                        Log.d("chatsListValue", "failed")
                    }
                }
        }

    fun showRequests(tsId: String) {
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
                    ChatsListItem(r.requestId, userId, r.timeSlot.id, r.requester.fullName, r.requester.pic, r.lastMessageText, timeStr)
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

    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    return sdf.format(this)

}
