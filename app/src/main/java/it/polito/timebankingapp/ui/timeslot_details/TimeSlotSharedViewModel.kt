package it.polito.timebankingapp.ui.timeslot_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.timebankingapp.model.timeslot.TimeSlot

class TimeSlotSharedViewModel : ViewModel() {
    val selected = MutableLiveData<TimeSlot>()

    fun select(ts: TimeSlot) {
        selected.value = ts
    }
}