package it.polito.timebankingapp.ui.timeslots

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.Request
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User


class TimeSlotsViewModel(application: Application): AndroidViewModel(application) {

    private val _timeSlots = MutableLiveData<List<TimeSlot>>()
    val timeSlots: LiveData<List<TimeSlot>> = _timeSlots

    private val _skillList = MutableLiveData<List<String>>()
    val skillList: LiveData<List<String>> = _skillList

    private val _selectedTimeSlot =  MutableLiveData<TimeSlot>()
    val selectedTimeSlot: LiveData<TimeSlot> = _selectedTimeSlot

    lateinit var type: String
    private set

    private lateinit var l:ListenerRegistration

    private val _selectedSkill = MutableLiveData<String?>()
    var selectedSkill: LiveData<String?> = _selectedSkill

    private lateinit var l2:ListenerRegistration
    private lateinit var l3:ListenerRegistration

    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        Firebase.auth.addAuthStateListener {
            if (it.currentUser != null) {
                /*updatePersonalTimeSlots()
                updateSkillSpecificTimeSlots(skill)*/
                retrieveSkillList()
            }
        }
    }

    fun updateSkillSpecificTimeSlots(skill: String) {
        Log.d("selectedSkill", "updateSkillSpecificTimeSlos: selectedSkill: ${skill}")
        l = db.collection("timeSlots").whereNotEqualTo("userId", Firebase.auth.uid).whereEqualTo("relatedSkill",skill).addSnapshotListener{v,e ->
            if(e == null){
                _timeSlots.value = v!!.mapNotNull { d -> d.toObject<TimeSlot>() }
            } else _timeSlots.value = emptyList()
        }
    }


    fun updatePersonalTimeSlots() {
        l = db.collection("timeSlots").whereEqualTo("userId", Firebase.auth.uid).addSnapshotListener{v,e ->
            if(e == null){
                _timeSlots.value = v!!.mapNotNull { d -> d.toObject<TimeSlot>() }
            } else _timeSlots.value = emptyList()
        }
    }







    fun updateInterestingTimeSlots() {
        val myUid = Firebase.auth.uid!!
        db.collection("requests").whereEqualTo("requester.id", myUid)
            .whereEqualTo("status", Request.STATUS_INTERESTED).addSnapshotListener{ v, e ->
                if(e == null){
                    val req = v!!.mapNotNull {  d -> d.toObject<Request>()  }
                    _timeSlots.value = req.mapNotNull {  r -> r.timeSlot }
                } else {
                    _timeSlots.value = emptyList()
                }
            }
        }

    /* Move to skillsListViewModel */
    fun retrieveSkillList(){
        l = db.collection("skills").addSnapshotListener {
            v,e ->
            if(e == null){
                _skillList.value = v!!.mapNotNull { d -> d.id}
            }else _skillList.value = emptyList()
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
        _timeSlots.postValue(listOf())
    }


    fun addTimeSlot(ts: TimeSlot) {
        val data = HashMap<String, Any>()

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
        }.addOnFailureListener{Log.d("timeSlots_add", "Error on adding")}

        /*db.collection("timeSlots").document().set(ts).addOnSuccessListener {
            Log.d("timeSlots_add","Successfully added")
        }.addOnFailureListener{Log.d("timeSlots_add", "Error on adding")}*/

    }


    fun editTimeSlot(ts: TimeSlot){
        db.collection("timeSlots").document(ts.id).set(ts)
    }

    override fun onCleared() {
        l.remove()
        super.onCleared()
    }

    fun setSelectedTimeSlot(ts: TimeSlot){
        _selectedTimeSlot.value = ts
    }

    fun setFilteringSkill(skill: String) {

        Log.d("selectedSkill", "setFilteringSkill: $skill")
        _selectedSkill.postValue(skill)
        updateSkillSpecificTimeSlots(skill)
    }

    /* This is used to contact the offerer through chat */
    fun requestTimeSlot(ts: TimeSlot, requester: User, offerer: User, selectChat: (String) -> Unit) : String {
        val chatId = ts.id + "_" + requester.id
        val  req = Request(timeSlot = ts, requester = requester, offerer = offerer, unreadMsg = 0)
        /* if chat does not already exists, then it is a new chat */
        db.collection("requests").document(chatId).get()
            .addOnSuccessListener { doc ->
                if(!doc.exists())  //if doc is created now
                    ts.unreadChats = ts.unreadChats+1
                db.collection("timeSlots").document(ts.id).set(ts)
                db.collection("requests").document(chatId).set(req)
                selectChat(chatId)
//                ci andrebbe selectChat
            }
//        ts.unreadChats = ts.unreadChats+1
//        db.collection("requests").document(chatId).set(req)
//        db.collection("timeSlots").document(ts.id).set(ts)
//            .addOnSuccessListener { Log.d("unreadChats", "success") }
//            .addOnFailureListener { Log.d("unreadChats", "fail") }

        return chatId
    }


}
