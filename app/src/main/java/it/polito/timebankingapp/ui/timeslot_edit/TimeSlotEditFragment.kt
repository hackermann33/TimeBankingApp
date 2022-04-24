package it.polito.timebankingapp.ui.timeslot_edit

import android.os.Bundle
import android.text.InputType
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
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

val DEBUG = false

class TimeSlotEditFragment : Fragment(R.layout.fragment_time_slot_edit) {

    private val vm by viewModels<TimeSlotsListViewModel>()
    private var tsToEdit: TimeSlot = TimeSlot()

    private lateinit var v : View

    private lateinit var titleEditText: TextInputEditText
    private lateinit var dateEditText: TextInputEditText
    private lateinit var timeEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText

    private var addMode : Boolean = false

    private lateinit var calendar: Calendar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        addMode = arguments?.getSerializable("timeslot") == null
        v = view


        tsToEdit = if(!addMode) arguments?.getSerializable("timeslot") as TimeSlot
        else TimeSlot()

        calendar = tsToEdit.getCalendar()

        titleEditText = view.findViewById(R.id.edit_timeslot_Title)
        dateEditText = view.findViewById(R.id.edit_timeslot_Date)
        timeEditText = view.findViewById(R.id.edit_timeslot_Time)

        durationEditText = view.findViewById(R.id.edit_timeslot_Duration)
        locationEditText = view.findViewById(R.id.edit_timeslot_Location)
        descriptionEditText = view.findViewById(R.id.edit_timeslot_Description)

        buildDatePicker()
        buildTimePicker()

        showTimeSlot()
        val addButton = view.findViewById<Button>(R.id.addTimeSlotButton)
        addButton.isVisible = addMode

        if(!addMode) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                handleTimeSlotConfirmation()
                }
        }else {
            addButton.setOnClickListener {
                handleTimeSlotConfirmation()
            }
        }
    }

    private fun handleTimeSlotConfirmation() {

        val ts = retrieveTimeSlotData()
        if(ts.isValid()) {
            tsToEdit.clone(ts)
            if(addMode)
                vm.addTimeSlot(tsToEdit)
            else
                vm.editTimeSlot(tsToEdit)
            findNavController().navigateUp()
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

    private fun showTimeSlot() {

        titleEditText.setText(tsToEdit.title)
        dateEditText.setText(tsToEdit.date )
        timeEditText.setText(tsToEdit.time)

        durationEditText.setText(tsToEdit.duration )

        locationEditText.setText(tsToEdit.location )

        descriptionEditText.setText(tsToEdit.description )

        if(addMode && DEBUG) {
            titleEditText.setText("titleTmp")
            dateEditText.setText("22/02/2022")
            timeEditText.setText("00:00")
            durationEditText.setText("1")
            locationEditText.setText("testLocation")
            descriptionEditText.setText("testDescription")
        }

    }

    private fun buildTimePicker() {
        val isSystem24Hour = is24HourFormat(activity)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val timeFormatter = SimpleDateFormat("hh:mm", Locale.getDefault())

        /* If the Item is a newItem, the hour shown in the timePicker will be that one of today.*/
        val hour: Int = calendar[Calendar.HOUR]
        val minute: Int = calendar[Calendar.MINUTE]


        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(hour)
            .setMinute(
                minute
            )
            .setTitleText("Select slot hour").build()

        timeEditText.inputType = InputType.TYPE_NULL
        timeEditText.setOnFocusChangeListener { _, focus ->
            if (focus)
                timePicker.show(parentFragmentManager, "time picker")
        }

        timeEditText.setOnClickListener {
            timePicker.show(parentFragmentManager, "time picker")
        }


        timePicker.addOnPositiveButtonClickListener {
            val t = String.format("%02d:%02d", timePicker.hour,timePicker.minute)
            timeEditText.setText(t)
        }
    }

    private fun buildDatePicker() {

        val datePicker: MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
            .setSelection(calendar.timeInMillis)
            .setTitleText("Select date").build()


        /* this line is needed in order to prevent keyboard opens when datePicker has been shown */
        dateEditText.inputType = InputType.TYPE_NULL
        dateEditText.setOnFocusChangeListener { _, focus ->
            if (focus)
                datePicker.show(parentFragmentManager, "datePicker")
        }
        dateEditText.setOnClickListener {
            datePicker.show(parentFragmentManager, "datePicker")
        }

        datePicker.addOnPositiveButtonClickListener {
            calendar.timeInMillis = datePicker.selection!!

            val dateFormatToShow = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            dateEditText.setText(dateFormatToShow.format(calendar.time))
        }

    }


    /*override fun onDetach() {
        if(arguments?.getSerializable("timeslot") != null) {
            //was in edit mode, not creation
            val ts = retrieveTimeSlotData()
            if (ts.isValid()) {
                tsToEdit?.clone(ts)
                vm.editTimeSlot(tsToEdit!!)
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
    }*/

    private fun evidenceWrongFields() {

        val titleLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_TitleLay)
        if(titleEditText.text?.isEmpty() == true)
            titleLay.error = "Field cannot be empty!"
        else
            titleLay.error = null

        val dateLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DateLay)

        if(dateEditText.text?.isEmpty() == true)
            dateLay.error = "Field cannot be empty!"
        else
            dateLay.error = null

        val timeLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_TimeLay)

        if(timeEditText.text?.isEmpty() == true)
            timeLay.error = "Field cannot be empty!"
        else
            timeLay.error = null

        val durationLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DurationLay)

        if(durationEditText.text?.isEmpty() == true)
            durationLay.error = "Field cannot be empty!"
        else
            durationLay.error = null

        val locationLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_LocationLay)

        if(locationEditText.text?.isEmpty() == true)
            locationLay.error = "Field cannot be empty!"
        else
            locationLay.error = null

        val descriptionLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DescriptionLay)

        if(descriptionEditText.text?.isEmpty() == true)
            descriptionLay.error = "Field cannot be empty!"
        else
            descriptionLay.error = null

    }

    private fun retrieveTimeSlotData() : TimeSlot{

        val ts = TimeSlot()

        ts.title = titleEditText.text.toString()

        ts.date = dateEditText.text.toString()

        ts.time = timeEditText.text.toString()

        ts.duration = durationEditText.text.toString()

        ts.location = locationEditText.text.toString()

        ts.description = descriptionEditText.text.toString()

        return ts
    }
}


