package it.polito.timebankingapp.ui.timeslots

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.timeslot.TimeSlotRepository
import kotlin.concurrent.thread

class TimeSlotsViewModel(application: Application): AndroidViewModel(application) {


    val repo = TimeSlotRepository(application)

    //NOTA: la ViewModel dovrebbe essere strutturata in modo che essa ritorni dati ad entrambe TimeSlotDetails e TimeSlotEdit
    //cioè forse sostituire tutto il sistema del passaggio del bundle, idk (da investigare meglio

    val timeSlotsNumber: LiveData<Int> = repo.count()
    val timeSlots: LiveData<List<TimeSlot>> = repo.timeSlots()
    val selectedTimeSlot =  MutableLiveData<TimeSlot>()

    fun addTimeSlot(ts: TimeSlot) {
        thread {
            repo.addTimeSlot(ts)
        }
    }

    fun editTimeSlot(ts: TimeSlot){
        thread {
            repo.editTimeSlot(ts)
        }
    }

    fun clear() {
        thread {
            repo.clear()
        }
    }

    fun setSelectedTimeSlot(pos: Int){
        val ts = timeSlots.value?.get(pos) ?: TimeSlot()
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