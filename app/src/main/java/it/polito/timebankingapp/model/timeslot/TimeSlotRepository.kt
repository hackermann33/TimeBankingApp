package it.polito.timebankingapp.model.timeslot

import android.app.Application
import androidx.lifecycle.LiveData
import it.polito.timebankingapp.model.TimeBankingDB

class TimeSlotRepository (application: Application){
    private val timeSlotDao = TimeBankingDB.getDatabase(application).timeSlotDao()

    fun addTimeSlot(ts: TimeSlot){
        timeSlotDao.addTimeSlot(ts)
    }

    fun editTimeSlot(ts: TimeSlot){
        timeSlotDao.updateTimeSlot(ts)
    }

    fun count(): LiveData<Int> = timeSlotDao.count()

    fun timeSlots(): LiveData<List<TimeSlot>> = timeSlotDao.findAll()

    fun clear() {
        timeSlotDao.removeAll()
    }
}