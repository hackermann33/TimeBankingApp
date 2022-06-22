package it.polito.timebankingapp.ui.timeslots

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User


class TimeSlotsViewModel(application: Application) : AndroidViewModel(application) {

    private val _timeSlots = MutableLiveData<List<TimeSlot>?>(null)
    val timeSlots: LiveData<List<TimeSlot>?> = _timeSlots

    private val _skillList = MutableLiveData<List<String>>()
    val skillList: LiveData<List<String>> = _skillList

    private val _selectedTimeSlot = MutableLiveData<TimeSlot?>()
    val selectedTimeSlot: LiveData<TimeSlot?> = _selectedTimeSlot

    private val _unreadTimeSlotsMessages = MutableLiveData<Map<String, Int>>()
    val unreadTimeSlotsMessages: LiveData<Map<String, Int>> = _unreadTimeSlotsMessages

    lateinit var type: String
        private set

    private lateinit var l: ListenerRegistration

    private val _selectedSkill = MutableLiveData<String?>()
    var selectedSkill: LiveData<String?> = _selectedSkill

    private lateinit var selectedTimeSlotListener: ListenerRegistration
    private lateinit var l2: ListenerRegistration
    private lateinit var l3: ListenerRegistration

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    //var justUpdated = false
    private val _isLoading = MutableLiveData<Boolean>(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData<Boolean?>()
    var isEmpty: LiveData<Boolean?> = _isEmpty

    init {
        Firebase.auth.addAuthStateListener {
            if (it.currentUser != null) {
                //updatePersonalTimeSlots()
                /*updateSkillSpecificTimeSlots(skill)*/
                retrieveSkillList()
            }
        }
    }

    fun setIsEmptyFlag(value: Boolean) {
        _isEmpty.value = value
    }

    fun updateSkillSpecificTimeSlots(skill: String) {
        _isLoading.postValue(true)
        clearTimeSlots()

        Log.d("selectedSkill", "updateSkillSpecificTimeSlos: selectedSkill: ${skill}")

        l = db.collection("timeSlots").whereNotEqualTo("userId", Firebase.auth.uid)
            .whereEqualTo("relatedSkill", skill)
            .whereEqualTo("status", TimeSlot.TIME_SLOT_STATUS_AVAILABLE)
            .addSnapshotListener { v, e ->
                if (e == null) {
                    _timeSlots.value = v!!.mapNotNull { d -> d.toObject<TimeSlot>() }
                    _isLoading.postValue(false)
                    _isEmpty.value = _timeSlots.value!!.isEmpty()
                } else {
                    _timeSlots.value = emptyList()
                    _isLoading.postValue(false)
                    _isEmpty.value = true
                }
                //justUpdated = true
            }
    }


    fun updatePersonalTimeSlots() {
        l = db.collection("timeSlots").whereEqualTo("userId", Firebase.auth.uid)
            .addSnapshotListener { v, e ->
                if (e == null) {
                    _timeSlots.postValue(v!!.mapNotNull { d -> d.toObject<TimeSlot>() })
                } else {
                    _timeSlots.postValue( listOf())
                }
            }
    }



    fun updateInterestingTimeSlots() {
        val myUid = Firebase.auth.uid!!
        db.collection("requests").whereEqualTo("requester.id", myUid)
            .whereEqualTo("status", Chat.STATUS_INTERESTED).addSnapshotListener { v, e ->
                if (e == null) {
                    val req = v!!.mapNotNull { d -> d.toObject<Chat>() }
                    _timeSlots.postValue( req.map { r -> r.timeSlot } )
                } else {
                    _timeSlots.value = emptyList()
                }
            }
    }
    // db.collection("requests").whereArrayContains("users", myuid).whereEqualTo("status", Chat.STATUS_COMPLETED)

    fun updateCompletedTimeSlots() {
        val myUid = Firebase.auth.uid!!
        l = db.collection("timeSlots").whereArrayContains("users", myUid)
            .whereEqualTo("status", TimeSlot.TIME_SLOT_STATUS_COMPLETED).addSnapshotListener { v, e ->
                if (e == null) {
                    val assignedTimeSlots = v!!.mapNotNull { d -> d.toObject<TimeSlot>() }
                    _timeSlots.postValue(assignedTimeSlots)
                } else {
                    Log.d("TimeSlotsViewModel", "$e")
                    _timeSlots.value = emptyList()
                }
            }
    }

    fun updateAssignedTimeSlots() {
        _isLoading.postValue(true)
        val myUid = Firebase.auth.uid!!
        l = db.collection("timeSlots").whereArrayContains("users", myUid)
            .whereEqualTo("status", TimeSlot.TIME_SLOT_STATUS_ASSIGNED).addSnapshotListener { v, e ->
                if (e == null) {
                    Log.d("TimeSlotsViewModel", "$v")
                    val assignedTimeSlots = v!!.mapNotNull { d -> d.toObject<TimeSlot>() }
                    _timeSlots.postValue(assignedTimeSlots)
                } else {
                    Log.d("TimeSlotsViewModel", "$e")
                    _timeSlots.value = emptyList()
                }
            }
    }

    /* Move to skillsListViewModel */
    fun retrieveSkillList() {
        l = db.collection("skills").addSnapshotListener { v, e ->
            if (e == null) {
                _skillList.value = v!!.mapNotNull { d -> d.id }
            } else _skillList.value = emptyList()
        }
        /*.addOnSuccessListener { result ->
            for (document in result) {
                //Log.d(TAG, "${document.id} => ${document.data}")
                list.add(document.id)
            }
            _skillList.value = list
        }
        .addOnFailureListener { exception ->
            Log.d("skill_list", "Error getting documents: ", exception)
        }*/
    }

    fun clearTimeSlots() {
        _timeSlots.postValue(null)
    }


    fun addTimeSlot(ts: TimeSlot): Task<Void> {
        val data = HashMap<String, Any>()

        val newTimeSlotRef = db.collection("timeSlots").document()

        ts.id = newTimeSlotRef.id //imposta id generato da firebase
        ts.userId = Firebase.auth.currentUser?.uid ?: ""

        return newTimeSlotRef.set(ts)

        /*db.collection("timeSlots").document().set(ts).addOnSuccessListener {
            Log.d("timeSlots_add","Successfully added")
        }.addOnFailureListener{Log.d("timeSlots_add", "Error on adding")}*/

    }


    fun editTimeSlot(ts: TimeSlot): Task<QuerySnapshot> {
        val tsReqDocRef = db.collection("timeSlots").document(ts.id)
        val requests = db.collection("requests").whereEqualTo("timeSlot.id", ts.id)

        return requests.get().addOnSuccessListener {
            db.runBatch { batch ->
                batch.set(tsReqDocRef, ts)

                Log.d("edit", "$it")
                for (doc in it.documents)
                    batch.update(doc.reference, mapOf("timeSlot" to ts))
            }
        }.addOnFailureListener {
            Log.d("edit", "$it")
        }
    }

    override fun onCleared() {
        l.remove()
        super.onCleared()
    }


    fun setSelectedTimeSlot(timeSlot: TimeSlot) {
        _selectedTimeSlot.postValue(timeSlot)
        selectedTimeSlotListener =
            db.collection("timeSlots").document(timeSlot.id).addSnapshotListener { v, e ->
                if (e == null) {
                    _selectedTimeSlot.postValue(v!!.toObject<TimeSlot>())
                } else {
                    _selectedTimeSlot.postValue(TimeSlot())
                }
            }
    }

    fun setFilteringSkill(skill: String) {

        Log.d("selectedSkill", "setFilteringSkill: $skill")
        if (skill != _selectedSkill.value) {
            clearTimeSlots()
        }
        _selectedSkill.postValue(skill)
        updateSkillSpecificTimeSlots(skill)
    }

    /* This is used to create a request to the offerer through chat */
    fun makeTimeSlotRequest(ts: TimeSlot, currentUser: User): Task<Void> {
        val chatId = Helper.makeRequestId(ts.id, currentUser.id)
        val req = Chat(
            timeSlot = ts, requester = currentUser.toCompactUser(),
            offerer = ts.offerer, status = Chat.STATUS_INTERESTED, offererUnreadMsg = 0
        )

        /*
            UPDATE UNREAD CHATS
            .addOnSuccessListener { doc ->
                if(!doc.exists())  //if doc is created now
                    ts.unreadChats = ts.unreadChats+1
                db.collection("timeSlots").document(ts.id).set(ts)
                db.collection("requests").document(chatId).set(req)
                selectChat(chatId)
//                ci andrebbe selectChat
            }*/
//        ts.unreadChats = ts.unreadChats+1
//        db.collection("requests").document(chatId).set(req)
//        db.collection("timeSlots").document(ts.id).set(ts)
//            .addOnSuccessListener { Log.d("unreadChats", "success") }
//            .addOnFailureListener { Log.d("unreadChats", "fail") }

        return db.collection("requests").document(chatId).set(req)

    }

    /* Set the timeSlot as completed (and update all the requests) */
    fun setTimeSlotAsCompleted(ts: TimeSlot) {
        db.collection("timeSlots").document(ts.id)
            .update("status", TimeSlot.TIME_SLOT_STATUS_COMPLETED).addOnSuccessListener {

                /* Update timeSlot references in requests*/
                db.collection("requests").get().addOnSuccessListener {
                    db.runBatch { batch ->

                        it.forEach { doc -> batch.update(doc.reference, "timeSlot.status", TimeSlot.TIME_SLOT_STATUS_COMPLETED);
                            batch.update(doc.reference, "status", Chat.STATUS_COMPLETED)
                        }
                    }
                }
                Log.d("timeSlot_completed", "success")
            }.addOnFailureListener{
                Log.d("timeslot_completed", it.stackTraceToString())
            }
    }

    fun addNewSkill(skillStr: String): Task<Void> {
        return db.collection("skills").document(skillStr).set(mapOf<String, Any>())
    }

    fun clearSelectedTimeSlot() {
        _selectedTimeSlot.postValue(null)
    }
}

