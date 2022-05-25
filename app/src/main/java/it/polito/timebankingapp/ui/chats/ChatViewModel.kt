package it.polito.timebankingapp.ui.chats

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.chat.ChatsListItem
import it.polito.timebankingapp.model.timeslot.TimeSlot
import java.util.*

class ChatViewModel(application: Application): AndroidViewModel(application) {
    private val _chatId = MutableLiveData<String>()
    val chatId : LiveData<String>  = _chatId


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
        db.collection("chats").document(chatId.value!!).collection("messages").add(mapOf(
            "messageText" to message.messageText,
            "timestamp" to message.timestamp.time,
            "userId" to message.userId,
        )).addOnSuccessListener {
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
    }

    fun cleanChats(){
        _chatMessages.value = listOf()
    }

    fun selectChat(chatId: String) {
        l = db.collection("chats").document(chatId).collection("messages").orderBy("timestamp")
            .addSnapshotListener{
                    v,e ->
                if(e == null){
                    _chatId.value = chatId
                    _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
                } else
                    _chatMessages.value = emptyList()
            }
    }

    fun showRequests(tsId: String) {
//        Log.d("showRequests", "Arrived at ViewModel $tsId")
        Log.d("User", Firebase.auth.uid.toString())
        l = db.collection("rooms").document(Firebase.auth.uid!!.toString()).collection("userRooms")
            .addSnapshotListener{v,e ->
            if(e == null){
                _chatsList.value = v!!.filter{c -> c.id.contains(tsId)}.mapNotNull { c -> c.toChatListItem(tsId, c.id) }
                Log.d("chatsListValue", "success")
            } else{
                _chatsList.value = emptyList()
                Log.d("chatsListValue", "failed")
            }
        }

    }
}

private fun QueryDocumentSnapshot.toChatListItem(tsId: String, chatId: String) : ChatsListItem? {
    return try {
        val userName = get("fullName") as String
        val userPic = get("profilePic") as String
        //salva anche la bitmap vera e propria



        ChatsListItem(tsId, userName, chatId = chatId, userPic=userPic)
//        val userId = get("userId") as String
//        val title = get("title") as String
//        val desc = get("description") as String
//        val date = get("date") as String
//        val time = get("time") as String
//        val duration = get("duration") as String
//        val location = get("location") as String
//        val restrictions = get("restrictions") as String
//        val relatedSkill = get("relatedSkill") as String
//
//        //assert(userId == Firebase.auth.currentUser?.uid ?: false)
//        TimeSlot(id, userId,title, desc, date, time, duration, location, restrictions, relatedSkill)
    } catch(e: Exception) {
        e.printStackTrace()
        null
    }

}
