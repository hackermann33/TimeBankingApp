package it.polito.timebankingapp.ui.timeslot_edit

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.util.Pair
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import it.polito.timebankingapp.R
import it.polito.timebankingapp.ui.timeslot_details.TimeSlot
import java.util.*


class TimeSlotEditFragment : Fragment() {


    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val temp: TimeSlot = arguments?.getSerializable("timeslot") as TimeSlot

        val titleET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Title)
        titleET.setText(temp.title)

        val dateET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Date)
        dateET.setText(temp.date)

        var datePicker : MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker().setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTitleText("Select date").build()

        dateET.setOnClickListener {
            datePicker.show(parentFragmentManager, "datePicker");
        }

        datePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.


        }

        // now handle the positive button click from the
        // material design date picker
        datePicker.addOnPositiveButtonClickListener {  }

        val timeET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Time)
        timeET.setText(temp.time)

        val durationET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Duration)
        durationET.setText(temp.duration)

        val locationET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Location)
        locationET.setText(temp.location)

        val descriptionET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Description)
        descriptionET.setText(temp.description)
    }

}