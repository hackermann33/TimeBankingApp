package it.polito.timebankingapp.ui.timeslots

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.model.timeslot.TimeSlot


class TimeSlotsViewModel(application: Application): AndroidViewModel(application) {

    private val _personalTimeSlots = MutableLiveData<List<TimeSlot>>()
    val personalTimeSlots: LiveData<List<TimeSlot>> = _personalTimeSlots

    private val _globalTimeSlots = MutableLiveData<List<TimeSlot>>()
    val globalTimeSlots: LiveData<List<TimeSlot>> = _globalTimeSlots

    private var l:ListenerRegistration
    private var l2:ListenerRegistration


    private val db: FirebaseFirestore

    init {
        db = FirebaseFirestore.getInstance()
        l = db.collection("timeSlots").whereEqualTo("userId", Firebase.auth.uid).addSnapshotListener{v,e ->
            if(e == null){
                _personalTimeSlots.value = v!!.mapNotNull { d -> d.toTimeSlot() }
            } else _personalTimeSlots.value = emptyList()
        }

        l2 = db.collection("timeSlots").addSnapshotListener{v,e ->
            if(e == null){
                _globalTimeSlots.value = v!!.mapNotNull { d -> d.toTimeSlot() }
            } else _globalTimeSlots.value = emptyList()
        }
    }

    /*val repo = TimeSlotRepository(application)

    //NOTA: la ViewModel dovrebbe essere strutturata in modo che essa ritorni dati ad entrambe TimeSlotDetails e TimeSlotEdit
    //cioè forse sostituire tutto il sistema del passaggio del bundle, idk (da investigare meglio

    val timeSlotsNumber: LiveData<Int> = repo.count()
    val timeSlots: LiveData<List<TimeSlot>> = repo.timeSlots()
    */
    val selectedTimeSlot =  MutableLiveData<TimeSlot>()

    fun addTimeSlot(ts: TimeSlot) {

        val tsId: String = db.collection("timeSlots").document().id

        ts.id = tsId //imposta id generato da firebase
        ts.userId = Firebase.auth.currentUser?.uid ?: ""

        db.collection("timeSlots").document(tsId).set(ts)
        .addOnSuccessListener{
            Log.d("timeSlots_add","Successfully added")
        }.addOnFailureListener{Log.d("timeSlots_add", "Error on adding")}

        /*db.collection("timeSlots").document().set(ts).addOnSuccessListener {
            Log.d("timeSlots_add","Successfully added")
        }.addOnFailureListener{Log.d("timeSlots_add", "Error on adding")}*/

    }


    fun editTimeSlot(ts: TimeSlot){
        db.collection("timeSlots").document(ts.id).set(ts)
    }

    override fun onCleared() {
        super.onCleared()
        l.remove()
        l2.remove()
    }

    fun setSelectedTimeSlot(pos: Int){
        val ts = personalTimeSlots.value?.get(pos) ?: TimeSlot()
        selectedTimeSlot.value = ts
    }




    /*private val _privateTimeSlot = TimeSlot("test1","test2","test3","test4","test5","test6")

    private val _mutableTimeSlot = MutableLiveData<TimeSlot>().apply {
        value = _privateTimeSlot
    }

    val timeSlot: LiveData<TimeSlot> =_mutableTimeSlot

    fun saveEdits(newTimeSlot: TimeSlot) {
        _mutableTimeSlot.value = newTimeSlot
    }*/

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

        assert(userId == Firebase.auth.currentUser?.uid ?: false)
        TimeSlot(id, userId,title, desc, date, time, duration, location, restrictions, relatedSkill)
    } catch(e: Exception) {
        e.printStackTrace()
        null
    }

}