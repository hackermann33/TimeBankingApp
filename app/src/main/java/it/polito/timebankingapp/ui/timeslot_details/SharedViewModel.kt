package it.polito.timebankingapp.ui.timeslot_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val selected = MutableLiveData<TimeSlot>()

    fun select(ts: TimeSlot) {
        selected.value = ts
    }
}