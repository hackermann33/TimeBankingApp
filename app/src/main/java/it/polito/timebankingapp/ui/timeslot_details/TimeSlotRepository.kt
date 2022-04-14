package it.polito.timebankingapp.ui.timeslot_details

import android.app.Application
import androidx.lifecycle.LiveData

class TimeSlotRepository (application: Application){
    private val timeSlotDao = TimeSlotDB.getDatabase(application).timeSlotDao()

    fun addTimeSlot(ts: TimeSlot){
        timeSlotDao.addTimeSlot(ts)
    }

    fun count(): LiveData<Int> = timeSlotDao.count()

    fun timeSlots(): LiveData<List<TimeSlot>> = timeSlotDao.findAll()

    fun clear() {
        timeSlotDao.removeAll()
    }
}