package it.polito.timebankingapp.ui.timeslot_edit

import android.os.Bundle
import android.text.InputType
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
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

val DEBUG = true

class TimeSlotEditFragment : Fragment(R.layout.fragment_time_slot_edit) {

    private val vm by viewModels<TimeSlotsListViewModel>()
    private var tsTmp: TimeSlot? = TimeSlot()

    private lateinit var v : View
    private var isNew : Boolean = false

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
        isNew = arguments?.getSerializable("timeslot") == null
        v = view

        tsTmp = arguments?.getSerializable("timeslot") as TimeSlot? ?: TimeSlot()

        val titleET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Title)
        titleET.setText(tsTmp?.title ?: "")

        val dateET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Date)
        dateET.setText(tsTmp?.date ?: "")

        var dateFromPicker: Date?

        val datePicker: MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTitleText("Select date").build()

        /* this line is needed in order to prevent keyboard opens when datePicker has been shown */
        dateET.inputType = InputType.TYPE_NULL
        dateET.setOnFocusChangeListener { _, focus ->
            if (focus)
                datePicker.show(parentFragmentManager, "datePicker")
        }
        dateET.setOnClickListener {
            datePicker.show(parentFragmentManager, "datePicker")
        }

        datePicker.addOnPositiveButtonClickListener {
            dateFromPicker = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).parse(datePicker.headerText)
            val dateEdit = SimpleDateFormat("dd / MM / yyyy", Locale.getDefault())

            dateET.setText(dateEdit.format(dateFromPicker!!))
            Log.d("Date picked = ", "Saved date: $dateFromPicker")
        }

        val timeET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Time)
        timeET.setText(tsTmp?.time ?: "")

        val isSystem24Hour = is24HourFormat(activity)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val timeFormatter = SimpleDateFormat("hh:mm", Locale.getDefault())

        /* If the Item is a newItem, the hour shown in the timePicker will be that one of today.*/
        val hour: Int = if(!isNew) tsTmp!!.time.split(":")[0].trim().toInt()
        else timeFormatter.format(System.currentTimeMillis()).split(":")[0].toInt()
        val minute: Int = if(!isNew) tsTmp!!.time.split(":")[1].trim().toInt()
                        else timeFormatter.format(System.currentTimeMillis()).split(":")[1].toInt()

        /*val hour =
            timeFormatter.format(System.currentTimeMillis()).split(":").first().toInt()
            else timeFormatter.format(temp?.time).split(":").first().toInt()
        */

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(hour)
            .setMinute(
                minute
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
            dateFromPicker = SimpleDateFormat("hh:mm", Locale.getDefault()).parse(t)
            val dt = SimpleDateFormat("hh : mm", Locale.getDefault())

            timeET.setText(dt.format(dateFromPicker!!))
        }

        val durationET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Duration)
        durationET.setText(tsTmp?.duration ?: "")

        val locationET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Location)
        locationET.setText(tsTmp?.location ?: "")

        val descriptionET = view.findViewById<TextInputEditText>(R.id.edit_timeslot_Description)
        descriptionET.setText(tsTmp?.description ?: "")

        if(isNew && DEBUG) {
            titleET.setText("titleTmp")
            dateET.setText("22 / 02 / 2022")
            timeET.setText("00:00")
            durationET.setText("1")
            locationET.setText("testLocation")
            descriptionET.setText("testDescription")
        }


        val addButton = view.findViewById<Button>(R.id.addTimeSlotButton)
        addButton.isVisible = isNew
        addButton.setOnClickListener {
            val ts = retrieveTimeSlotData()
            if(ts.isValid()) {
                tsTmp?.clone(ts)
                vm.addTimeSlot(tsTmp!!)
                findNavController().navigateUp()
                //parentFragmentManager.popBackStackImmediate("",0)
                /*AlertDialog.Builder(requireActivity())
                    .setTitle("TimeSlot correctly created!")
                    .setMessage("Your TimeSlot was correctly created. You can now find it with the others in your list!")
                    .setPositiveButton("Ok"){ _, _ ->
                        cleanFields()
                    }
                    .show()*/
                //getActivity().getFragmentManager().popBackStack();
                //val id = parentFragmentManager.getBackStackEntryAt(R.id.nav_timeSlotEdit).id
            //findNavController().navigate(R.id.action_nav_timeSlotEdit_to_nav_timeSlotsList)
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
                tsTmp?.clone(ts)
                vm.editTimeSlot(tsTmp!!)
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
        ts.title = titleET.text.toString()

        val dateET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Date)
        ts.date = dateET.text.toString()

        val timeET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Time)
        ts.time = timeET.text.toString()

        val durationET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Duration)
        ts.duration = durationET.text.toString()

        val locationET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Location)
        ts.location = locationET.text.toString()

        val descriptionET = v.findViewById<TextInputEditText>(R.id.edit_timeslot_Description)
        ts.description = descriptionET.text.toString()

        return ts
    }
}