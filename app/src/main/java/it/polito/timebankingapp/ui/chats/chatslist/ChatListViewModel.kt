package it.polito.timebankingapp.ui.chats.chatslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.Helper.Companion.fromRequestToChat
import it.polito.timebankingapp.model.Chat


class ChatListViewModel(application: Application): AndroidViewModel(application) {


    private val _allChatList = MutableLiveData<List<Chat>>()
    val allChatList : LiveData<List<Chat>> = _allChatList

    private val _timeSlotChatList = MutableLiveData<List<Chat>>()
    val timeSlotChatList : LiveData<List<Chat>> = _timeSlotChatList


    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _unreadChats = MutableLiveData<Int>()
    val unreadChats : LiveData<Int> = _unreadChats

    private lateinit var allChatsListener: ListenerRegistration
    private lateinit var timeSlotChatsListener: ListenerRegistration

    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading : LiveData<Boolean> = _isLoading




    /*
    private val _hasChatsListBeenCleared = MutableLiveData<Boolean?>()
    var hasChatsListBeenCleared: LiveData<Boolean?> = _hasChatsListBeenCleared


    init {
        _hasChatsListBeenCleared.value = false
    }

    fun setIsClearedFlag(value: Boolean) {
        _hasChatsListBeenCleared.value = value
    }
*/
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

        allChatsListener.remove()
        if(this::timeSlotChatsListener.isInitialized)
            timeSlotChatsListener.remove()
    }

    fun updateAllChats() {
            _isLoading.postValue(true)
           Log.d("User", Firebase.auth.uid.toString())
            val currentId = Firebase.auth.uid.toString()
            allChatsListener = db.collection("requests").whereArrayContains("users","${Firebase.auth.uid}")
                .orderBy("lastMessage.timestamp", Query.Direction.DESCENDING).addSnapshotListener{ v, e ->
                    if(e == null){
                        _allChatList.value = v!!.mapNotNull {  d -> d.toObject<Chat>() }
                        Log.d("chatList", "chatListALL: ${_allChatList.value!!.mapNotNull { it.timeSlot.title }}")
                        _isLoading.postValue(false)
                        var cnt = 0
                        _allChatList.value?.forEach { chat ->
                            if(chat.offerer.id == Firebase.auth.uid && chat.timeSlot.offererUnreadChats > 0) /* I am the offerer of this chat*/
                                cnt++
                            if(chat.requester.id == Firebase.auth.uid && chat.timeSlot.requesterUnreadChats > 0) /* I am the requester of this chat */
                                cnt++
                        }
                        _unreadChats.postValue(cnt)
                        Log.d("chatsListValue", "success")
                    } else{
                        _allChatList.value = emptyList()
                        _isLoading.postValue(false)
                        Log.d("chatsListValue", "failed")
                    }
                }
        }


    /* Download all the chat related to a specific offer that current user has published */
    fun downloadTimeSlotChats(tsId: String) {
        _isLoading.postValue(true)
//        Log.d("showRequests", "Arrived at ViewModel $tsId")
        Log.d("User", Firebase.auth.uid.toString())
        timeSlotChatsListener = db.collection("requests").whereEqualTo("offerer.id",Firebase.auth.uid.toString()).whereEqualTo("timeSlot.id", tsId)
            .addSnapshotListener{v,e ->
            if(e == null){
                val requests = v!!.mapNotNull {  d -> d.toObject<Chat>()  }
                _timeSlotChatList.value = requests
                _isLoading.postValue(false)
                Log.d("chatList", "chatListTS: ${_timeSlotChatList.value!!.mapNotNull { it.timeSlot.title }}")
                Log.d("chatsListValue", "success")
            } else{
                _timeSlotChatList.value = emptyList()
                Log.d("chatsListValue", "failed")
                _isLoading.postValue(false)
            }
        }
    }

    fun clearTimeSlotChatList() {
        _timeSlotChatList.value = listOf()
    }


    fun clearChatList() {
        _allChatList.value = listOf()
        _timeSlotChatList.value = listOf()
    }



}



