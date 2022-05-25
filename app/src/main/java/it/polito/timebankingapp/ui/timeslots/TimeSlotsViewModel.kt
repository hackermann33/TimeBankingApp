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
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User


class TimeSlotsViewModel(application: Application): AndroidViewModel(application) {

    private val _timeSlots = MutableLiveData<List<TimeSlot>>()
    val timeSlots: LiveData<List<TimeSlot>> = _timeSlots

    /*  publicTimeSlots is excluding that one of the current user! */
    private val _publicTimeSlots = MutableLiveData<List<TimeSlot>>()
    val publicTimeSlots: LiveData<List<TimeSlot>> = _publicTimeSlots

    private val _perSkillTimeSlots = MutableLiveData<List<TimeSlot>>()
    val perSkillTimeSlots: LiveData<List<TimeSlot>> = _perSkillTimeSlots

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




    /*val repo = TimeSlotRepository(application)

    //NOTA: la ViewModel dovrebbe essere strutturata in modo che essa ritorni dati ad entrambe TimeSlotDetails e TimeSlotEdit
    //cioè forse sostituire tutto il sistema del passaggio del bundle, idk (da investigare meglio

    val timeSlotsNumber: LiveData<Int> = repo.count()
    val timeSlots: LiveData<List<TimeSlot>> = repo.timeSlots()
    */


    fun updateSkillSpecificTimeSlots(skill: String) {
        Log.d("selectedSkill", "updateSkillSpecificTimeSlos: selectedSkill: ${skill}")
        l = db.collection("timeSlots").whereNotEqualTo("userId", Firebase.auth.uid).whereEqualTo("relatedSkill",skill).addSnapshotListener{v,e ->
            if(e == null){
                _timeSlots.value = v!!.mapNotNull { d -> d.toTimeSlot() }
            } else _timeSlots.value = emptyList()
        }
    }


    fun updatePersonalTimeSlots() {
        l = db.collection("timeSlots").whereEqualTo("userId", Firebase.auth.uid).addSnapshotListener{v,e ->
            if(e == null){
                _timeSlots.value = v!!.mapNotNull { d -> d.toTimeSlot() }
            } else _timeSlots.value = emptyList()
        }
    }


    /* Move to skillsListViewModel */
    fun retrieveSkillList(){
        //val list = mutableListOf<String>()
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

    //This should not be used anymore, use updateSkillSpecificTimeSlots
    fun setFilteringSkill(skill: String) {
        type = "skill"

        Log.d("selectedSkill", "setFilteringSkill: $skill")
        _selectedSkill.postValue(skill)
        updateSkillSpecificTimeSlots(skill)
       // _perSkillTimeSlots.value = publicTimeSlots.value?.filter{ skill == null || it.relatedSkill == skill }
    }

    /* This is used to contact the offerer through chat */
    fun requestTimeSlot(requester: User, ts: TimeSlot) : String {
        val myUid = Firebase.auth.uid!!
        val chatId = ts.id + "_" + myUid

        /* Look for existing chat otherwise create it*/
        db.collection("rooms").document(myUid).collection("userRooms").document(chatId)
            .set (mapOf(
                "fullName" to ts.userId,
                "status" to STATUS_INTERESTED,
                "requesterId" to Firebase.auth.uid
            )).addOnSuccessListener { Log.d("requestTimeSlot", "success") }.addOnFailureListener { Log.d("requestTimeSlot", "failure")}

        db.collection("rooms").document(ts.userId).collection("userRooms").document(chatId)
            .set (mapOf(
                "fullName" to requester.fullName,
                "profilePic" to requester.pic,
                "status" to STATUS_INTERESTED,
                "requesterId" to Firebase.auth.uid
            )).addOnSuccessListener {Log.d("requestTimeSlot", "success")}.addOnFailureListener{Log.d("requestTimeSLot", "failure")}

        return chatId
    }

    fun setType(type: String) {
        this.type = type

        when(type) {
            "personal" -> updatePersonalTimeSlots()
            "skill" ->{
                /*updateSkillSpecificTimeSlots()*/
            }
            "interesting" -> updateInterestingTimeSlots()

        }
    }


    private fun updateInterestingTimeSlots() {

        val myUid = Firebase.auth.uid!!
        l = db.collection("timeSlots").whereEqualTo("userId", Firebase.auth.uid).addSnapshotListener{v,e ->
            if(e == null){
                _timeSlots.value = v!!.mapNotNull { d -> d.toTimeSlot() }
            } else _timeSlots.value = emptyList()
        }
    }

    /*private val _privateTimeSlot = TimeSlot("test1","test2","test3","test4","test5","test6")

    private val _mutableTimeSlot = MutableLiveData<TimeSlot>().apply {
        value = _privateTimeSlot
    }

    val timeSlot: LiveData<TimeSlot> =_mutableTimeSlot

    fun saveEdits(newTimeSlot: TimeSlot) {
        _mutableTimeSlot.value = newTimeSlot
    }*/

    companion object {
        const val STATUS_INTERESTED = 0
    }

}



private fun QueryDocumentSnapshot.toTimeSlot() : TimeSlot? {
    return try {
        val userId = get("userId") as String
        val title = get("title") as String
        val desc = get("description") as String
        val date = get("date") as String
        val time = get("time") as String
        val duration = get("duration") as String
        val location = get("location") as String
        val restrictions = get("restrictions") as String
        val relatedSkill = get("relatedSkill") as String

        //assert(userId == Firebase.auth.currentUser?.uid ?: false)
        TimeSlot(id, userId,title, desc, date, time, duration, location, restrictions, relatedSkill)
    } catch(e: Exception) {
        e.printStackTrace()
        null
    }

}