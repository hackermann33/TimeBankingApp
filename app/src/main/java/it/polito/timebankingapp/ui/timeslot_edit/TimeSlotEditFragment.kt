package it.polito.timebankingapp.ui.timeslot_edit

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.timebankingapp.R
import it.polito.timebankingapp.ui.timeslot_details.TimeSlot
import java.util.*


class TimeSlotEditFragment : Fragment(R.layout.fragment_time_slot_edit) {


    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }*/

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?) {
        // Inflate the layout for this fragment

        val temp: TimeSlot = arguments?.getSerializable("timeslot") as TimeSlot

        val titleET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Title)
        titleET.setText(temp.title)

        val dateET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Date)
        dateET.setText(temp.date)

        val datePicker : MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTitleText("Select date").build()

        dateET.setOnClickListener {
            datePicker.show(parentFragmentManager, "datePicker")
        }

        datePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.


        }

        // now handle the positive button click from the
        // material design date picker
        datePicker.addOnPositiveButtonClickListener {  }

        val timeET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Time)
        timeET.setText(temp.time)

        val isSystem24Hour = is24HourFormat(activity)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val timeFormatter = SimpleDateFormat("hh:mm", Locale.getDefault())


        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(timeFormatter.format(System.currentTimeMillis()).split(":").first().toInt())
            .setMinute(timeFormatter.format(System.currentTimeMillis()).split(":").elementAt(1).toInt())
            .setTitleText("Select slot hour").build()

        timeET.setOnClickListener {
            timePicker.show(parentFragmentManager, "time picker")
        }

        timePicker.addOnPositiveButtonClickListener {
            val t = "${timePicker.hour}:${timePicker.minute}"
            date = SimpleDateFormat("hh:mm", Locale.getDefault()).parse(t)
            val dt = SimpleDateFormat("hh : mm", Locale.getDefault())

            timeET.setText(dt.format(date!!))
        }

        val durationET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Duration)
        durationET.setText(temp.duration)

        val locationET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Location)
        locationET.setText(temp.location)

        val descriptionET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Description)
        descriptionET.setText(temp.description)
    }

}