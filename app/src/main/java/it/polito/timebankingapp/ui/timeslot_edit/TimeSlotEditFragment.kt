package it.polito.timebankingapp.ui.timeslot_edit

import android.os.Bundle
import android.text.InputType
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.timeslots_list.TimeSlotsListViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.*

/** TODO: When edit is confirmed, global view model should be updated (DB)
 **/


class TimeSlotEditFragment : Fragment(R.layout.fragment_time_slot_edit) {

    private val vm by viewModels<TimeSlotsListViewModel>()
    private var temp: TimeSlot? = TimeSlot()

    private lateinit var v : View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Inflate the layout for this fragment

        /*
        istanzia prima i diversi components con findViewById
        val titleET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Title)
        ...
        
        vm.timeSlot.observe(this) {
            titleET.setText(it.title) //popola i components con i dati della TimeSlotViewModel
            ...
        }

         */

        //da fixare la prossima volta appena si aggiunge la shared activity viewmodel
        //val temp: TimeSlot = arguments?.getInt("id")?.let { vm.timeSlots.value?.elementAt(it) }!!

        v = view

        temp = arguments?.getSerializable("timeslot") as TimeSlot? ?: TimeSlot()


        val titleET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Title)
        titleET.setText(temp?.title ?: "")


        val dateET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Date)
        dateET.setText(temp?.date ?: "")

        var date: Date?


        val datePicker: MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTitleText("Select date").build()

        /* Added this line, to prevent keyboard opens */
        dateET.inputType = InputType.TYPE_NULL
        dateET.setOnFocusChangeListener { _, focus ->
            if (focus)
                datePicker.show(parentFragmentManager, "datePicker")
        }
        dateET.setOnClickListener {
            datePicker.show(parentFragmentManager, "datePicker")
        }

        datePicker.addOnPositiveButtonClickListener {
            date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).parse(datePicker.headerText)
            val df = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())

            dateET.setText(df.format(date!!))
            Log.d("Date picked = ", "Saved date: $date")
        }

        val timeET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Time)
        timeET.setText(temp?.time ?: "")

        val isSystem24Hour = is24HourFormat(activity)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val timeFormatter = SimpleDateFormat("hh:mm", Locale.getDefault())


        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(timeFormatter.format(System.currentTimeMillis()).split(":").first().toInt())
            .setMinute(
                timeFormatter.format(System.currentTimeMillis()).split(":").elementAt(1).toInt()
            )
            .setTitleText("Select slot hour").build()

        timeET.inputType = InputType.TYPE_NULL
        timeET.setOnFocusChangeListener { _, focus ->
            if (focus)
                timePicker.show(parentFragmentManager, "time picker")
        }

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
        durationET.setText(temp?.duration ?: "")

        val locationET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Location)
        locationET.setText(temp?.location ?: "")

        val descriptionET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Description)
        descriptionET.setText(temp?.description ?: "")

        val addButton = view.findViewById<Button>(R.id.addTimeSlotButton)
        addButton.isVisible = arguments?.getSerializable("timeslot") == null
        addButton.setOnClickListener {
            val ts = retrieveTimeSlotData()
            if(ts.isValid()) {
                temp?.clone(ts)
                vm.addTimeSlot(temp!!)
                AlertDialog.Builder(requireActivity())
                    .setTitle("TimeSlot correctly created!")
                    .setMessage("Your TimeSlot was correctly created. You can now find it with the others in your list!")
                    .setPositiveButton("Ok"){ _, _ ->
                        cleanFields()
                    }
                    .show()
            }
            else{
                AlertDialog.Builder(requireActivity())
                    .setTitle("TimeSlot not created!")
                    .setMessage("Your TimeSlot was not created. Make sure to not leave empty fields")
                    .setPositiveButton("Ok") { _, _ ->
                        evidenceWrongFields()
                    }
                    .show()
            }
        }
    }

    private fun cleanFields() {
        val titleET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Title)
        titleET.setText("")

        val dateET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Date)
        dateET.setText("")

        val timeET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Time)
        timeET.setText("")

        val durationET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Duration)
        durationET.setText("")

        val locationET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Location)
        locationET.setText("")

        val descriptionET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Description)
        descriptionET.setText("")


    }


    override fun onDetach() {
        if(arguments?.getSerializable("timeslot") != null) {
            //was in edit mode, not creation
            val ts = retrieveTimeSlotData()
            if (ts.isValid()) {
                temp?.clone(ts)
                vm.editTimeSlot(temp!!)
            } else {
                AlertDialog.Builder(requireActivity())
                    .setTitle("TimeSlot not modified!")
                    .setMessage("Your TimeSlot was not modified. Make sure to not leave empty fields.")
                    .setPositiveButton("Ok") { _, _ ->
                        evidenceWrongFields()
                    }
                    .show()
            }
        }
        super.onDetach()
    }

    private fun evidenceWrongFields() {
        val titleLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_TitleLay)
        val titleET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Title)
        if(titleET.text?.isEmpty() == true)
            titleLay.error = "Field cannot be empty!"
        else
            titleLay.error = null

        val dateLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DateLay)
        val dateET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Date)
        if(dateET.text?.isEmpty() == true)
            dateLay.error = "Field cannot be empty!"
        else
            dateLay.error = null

        val timeLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_TimeLay)
        val timeET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Time)
        if(timeET.text?.isEmpty() == true)
            timeLay.error = "Field cannot be empty!"
        else
            timeLay.error = null

        val durationLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DurationLay)
        val durationET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Duration)
        if(durationET.text?.isEmpty() == true)
            durationLay.error = "Field cannot be empty!"
        else
            durationLay.error = null

        val locationLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_LocationLay)
        val locationET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Location)
        if(locationET.text?.isEmpty() == true)
            locationLay.error = "Field cannot be empty!"
        else
            locationLay.error = null

        val descriptionLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DescriptionLay)
        val descriptionET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Description)
        if(descriptionET.text?.isEmpty() == true)
            descriptionLay.error = "Field cannot be empty!"
        else
            descriptionLay.error = null

    }

    private fun retrieveTimeSlotData() : TimeSlot{

        val ts = TimeSlot()

        val titleET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Title)
        ts?.title = titleET.text.toString()

        val dateET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Date)
        ts?.date = dateET.text.toString()

        val timeET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Time)
        ts?.time = timeET.text.toString()

        val durationET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Duration)
        ts?.duration = durationET.text.toString()

        val locationET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Location)
        ts?.location = locationET.text.toString()

        val descriptionET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Description)
        ts?.description = descriptionET.text.toString()

        return ts
    }
}