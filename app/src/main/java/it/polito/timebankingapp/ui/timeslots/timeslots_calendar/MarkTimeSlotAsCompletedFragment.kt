package it.polito.timebankingapp.ui.timeslots.timeslots_calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.polito.timebankingapp.R

class MarkTimeSlotAsCompletedFragment : Fragment(R.layout.fragment_mark_time_slot_as_completed) {

    lateinit var v: View;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view


    }


}