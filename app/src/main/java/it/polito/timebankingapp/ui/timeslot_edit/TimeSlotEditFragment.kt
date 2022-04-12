package it.polito.timebankingapp.ui.timeslot_edit

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import it.polito.timebankingapp.R
import it.polito.timebankingapp.ui.timeslot_details.TimeSlot


class TimeSlotEditFragment : Fragment() {


    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        /*
        val temp: TimeSlot = savedInstanceState?.get("timeslot") as TimeSlot
        val titleET = view?.findViewById<EditText>(R.id.edit_timeslot_Title)
        titleET!!.text= temp.title as Editable
        */
        return inflater.inflate(R.layout.fragment_time_slot_edit, container, false)

    }

}