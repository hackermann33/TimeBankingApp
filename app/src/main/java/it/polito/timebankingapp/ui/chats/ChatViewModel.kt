package it.polito.timebankingapp.ui.chats

import android.app.Application
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
import it.polito.timebankingapp.model.chat.ChatMessage

class ChatViewModel(application: Application): AndroidViewModel(application) {
    private val _chatId = MutableLiveData<String>()
    val chatId : LiveData<String>  = _chatId


    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var l: ListenerRegistration

    fun retrieveChatMessages(timeslotId: String, requesterId: String ){
        l = db.collection("chats").document(timeslotId).collection(requesterId).orderBy("timestamp")
            .addSnapshotListener {
                v,e ->
                if(e == null){
                    _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
                } else
                    _chatMessages.value = emptyList()
        }
    }

    fun addNewMessage(timeslotId: String, requesterId: String, cm : ChatMessage) {
        //se la chat non esiste ancora, creane una nuova automaticamente
        val newChatRef = db.collection("chats").document(timeslotId).collection(requesterId).document()

        cm.messageId =newChatRef.id  //imposta id generato da firebase
        cm.userId = Firebase.auth.currentUser?.uid ?: ""

        newChatRef.set(cm).addOnSuccessListener{
            Log.d("chat_create","Successfully added")
        }.addOnFailureListener{
            Log.d("timeSlots_add", "Error on adding")
        }
    }

    private fun QueryDocumentSnapshot.toChatMessage() : ChatMessage? {
        return try {
            val messageId = get("messageId") as String
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
}