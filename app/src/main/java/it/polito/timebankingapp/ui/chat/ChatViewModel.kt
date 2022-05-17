package it.polito.timebankingapp.ui.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.message.ChatMessage
import it.polito.timebankingapp.model.timeslot.TimeSlot

class ChatViewModel(application: Application): AndroidViewModel(application) {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var l: ListenerRegistration

    fun retrieveChatMessages(timeslotId: String, requestorId: String ){
        l = db.collection("chats").document(timeslotId).collection(requestorId)
            .addSnapshotListener {
                v,e ->
                if(e == null){
                    _chatMessages.value = v!!.mapNotNull { d -> d.toChatMessage() }
                } else
                    _chatMessages.value = emptyList()
        }
    }

    fun addMessage(ts: TimeSlot) {
       /* val data = HashMap<String, Any>()

        val newTimeSlotRef = db.collection("timeSlots").document()

        ts.id = newTimeSlotRef.id //imposta id generato da firebase
        ts.userId = Firebase.auth.currentUser?.uid ?: ""

        newTimeSlotRef.set(ts).addOnSuccessListener{
            Log.d("timeSlots_add","Successfully added")
            val skillRef: DocumentReference = db.collection("skills").document(ts.relatedSkill)
            skillRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (!document.exists()) {
                        skillRef.set(data)
                    }
                } else {
                    Log.d("timeSlots_add", "Failed with: ", task.exception)
                }
            }
        }.addOnFailureListener{ Log.d("timeSlots_add", "Error on adding")}
*/
    }

    fun addNewChat() {
        //to-do
    }

    private fun QueryDocumentSnapshot.toChatMessage() : ChatMessage? {
        return try {
            val messageId = get("messageId") as String
            val userId = get("userId") as String
            val messageText = get("messageText") as String
            val date = get("date") as String
            val time = get("time") as String

            ChatMessage(messageId = messageId, userId=userId, messageText = messageText, date = date, time =time)
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }
}