package it.polito.timebankingapp.ui.chats.chatslist

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


class ChatListViewModel(application: Application): AndroidViewModel(application) {


    private val _chatsList = MutableLiveData<List<ChatsListItem>>()
    val chatsList : LiveData<List<ChatsListItem>> = _chatsList


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


    override fun onCleared() {
        super.onCleared()

        l.remove()
        if(this::l2.isInitialized)
            l2.remove()
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
                            ChatsListItem(r.requestId, userId,  r.timeSlot.id, r.timeSlot.title,  otherUser.fullName, otherUser.pic, r.lastMessageText, timeStr, r.unreadMsg)}
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

                    ChatsListItem(r.requestId, userId, r.timeSlot.id, r.timeSlot.title, r.requester.fullName, r.requester.pic, r.lastMessageText, timeStr, r.unreadMsg)
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
