package it.polito.timebankingapp.ui.timeslots.timeslot_edit

import android.os.Bundle
import android.text.InputType
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import it.polito.timebankingapp.MainActivity
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import java.text.SimpleDateFormat
import java.util.*

/** TODO: When edit is confirmed, global view model should be updated (DB)
 **/

val DEBUG = false

class TimeSlotEditFragment : Fragment(R.layout.fragment_time_slot_edit) {

    private val vm by viewModels<TimeSlotsViewModel>()
    private var tsToEdit: TimeSlot = TimeSlot()

    private lateinit var v: View

    private lateinit var titleEditText: TextInputEditText
    private lateinit var dateEditText: TextInputEditText
    private lateinit var timeEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var restrictionsEditText: TextInputEditText

    private var addMode: Boolean = false
    private lateinit var calendar: Calendar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        addMode = arguments?.getSerializable("timeslot") == null
        (requireActivity() as MainActivity).supportActionBar?.title =
            if (addMode) "Add new time slot" else "Edit time slot"
        v = view

        setHasOptionsMenu(true)

        tsToEdit = if (!addMode) arguments?.getSerializable("timeslot") as TimeSlot
        else TimeSlot()

        calendar = tsToEdit.getCalendar()

        titleEditText = view.findViewById(R.id.edit_timeslot_Title)
        dateEditText = view.findViewById(R.id.edit_timeslot_Date)
        timeEditText = view.findViewById(R.id.edit_timeslot_Time)

        durationEditText = view.findViewById(R.id.edit_timeslot_Duration)
        locationEditText = view.findViewById(R.id.edit_timeslot_Location)
        descriptionEditText = view.findViewById(R.id.edit_timeslot_Description)
        restrictionsEditText = view.findViewById(R.id.edit_timeslot_Restrictions)

        buildDatePicker()
        buildTimePicker()

        showTimeSlot()
        val addButton = view.findViewById<Button>(R.id.addTimeSlotButton)
        addButton.isVisible = addMode

        if (!addMode) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                handleTimeSlotConfirmation()
            }

        } else {
            addButton.setOnClickListener {
                handleTimeSlotConfirmation()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (!addMode)
                    handleTimeSlotConfirmation()
                else {
                    findNavController().navigateUp()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleTimeSlotConfirmation() {

        retrieveTimeSlotData()
        if (tsToEdit.isValid()) {
            //tsToEdit = ts.copy()
            if (addMode) {
                vm.addTimeSlot(tsToEdit)
                setFragmentResult("timeSlot", bundleOf("timeSlotConfirm" to 1))
            } else {
                vm.editTimeSlot(tsToEdit)
                setFragmentResult("timeSlot", bundleOf("timeSlotConfirm" to 2))
            }

            findNavController().navigateUp()
        } else {
            val dialogTitle: String
            val dialogBody: String
            if (addMode) {
                dialogTitle = "TimeSlot not created!"
                dialogBody = "Your TimeSlot was not created. Make sure to not leave empty fields"
            } else {
                dialogTitle = "TimeSlot not edited!"
                dialogBody = "Your TimeSlot was not edited. Make sure to not leave empty fields"

            }
            AlertDialog.Builder(requireActivity())
                .setTitle(dialogTitle)
                .setMessage(dialogBody)
                .setPositiveButton("Ok") { _, _ ->
                    evidenceWrongFields()
                }
                .show()
        }
    }

    private fun showTimeSlot() {

        titleEditText.setText(tsToEdit.title)
        dateEditText.setText(tsToEdit.date)
        timeEditText.setText(tsToEdit.time)

        durationEditText.setText(tsToEdit.duration)

        locationEditText.setText(tsToEdit.location)

        descriptionEditText.setText(tsToEdit.description)

        restrictionsEditText.setText(tsToEdit.restrictions)

        if (addMode && DEBUG) {
            titleEditText.setText("titleTmp")
            dateEditText.setText("22/02/2022")
            timeEditText.setText("00:00")
            durationEditText.setText("1")
            locationEditText.setText("testLocation")
            descriptionEditText.setText("testDescription")
            restrictionsEditText.setText("testRestrictions")
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
            if (focus) {
                if (!timePicker.isVisible)
                    timePicker.show(parentFragmentManager, "timePicker")
            }
        }

        timeEditText.setOnClickListener {
            if (timeEditText.isFocused) {
                if (!timePicker.isVisible)
                    timePicker.show(parentFragmentManager, "timePicker")
            }
        }




        timePicker.addOnPositiveButtonClickListener {
            val t = String.format("%02d:%02d", timePicker.hour, timePicker.minute)
            timeEditText.setText(t)
        }

    }


    private fun buildDatePicker() {

        var datePicker: MaterialDatePicker<Long> = MaterialDatePicker.Builder.datePicker()
            .also { Log.d("date", calendar.toString()); it.setSelection(calendar.timeInMillis) }
            .setTitleText("Select date").build()


        /* this line is needed in order to prevent keyboard opens when datePicker has been shown */
        dateEditText.inputType = InputType.TYPE_NULL
        /*dateEditText.setOnFocusChangeListener { _, focus ->
            if (focus)
                datePicker.show(parentFragmentManager, "datePicker")
        }*/
        dateEditText.setOnFocusChangeListener { _, focus ->
            if (focus) {
                if (!datePicker.isVisible)
                    datePicker.show(parentFragmentManager, "datePicker")
            }
        }

        dateEditText.setOnClickListener {
            if (dateEditText.isFocused) {
                if (!datePicker.isVisible)
                    datePicker.show(parentFragmentManager, "datePicker")
            }
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
        if (titleEditText.text?.isEmpty() == true)
            titleLay.error = "Field cannot be empty!"
        else
            titleLay.error = null

        val dateLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DateLay)

        if (dateEditText.text?.isEmpty() == true)
            dateLay.error = "Field cannot be empty!"
        else
            dateLay.error = null

        val timeLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_TimeLay)

        if (timeEditText.text?.isEmpty() == true)
            timeLay.error = "Field cannot be empty!"
        else
            timeLay.error = null

        val durationLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DurationLay)

        if (durationEditText.text?.isEmpty() == true)
            durationLay.error = "Field cannot be empty!"
        else
            durationLay.error = null

        val locationLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_LocationLay)

        if (locationEditText.text?.isEmpty() == true)
            locationLay.error = "Field cannot be empty!"
        else
            locationLay.error = null

        val descriptionLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DescriptionLay)

        if (descriptionEditText.text?.isEmpty() == true)
            descriptionLay.error = "Field cannot be empty!"
        else
            descriptionLay.error = null

        val restrictionsLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_RestrictionsLay)

        if (restrictionsEditText.text?.isEmpty() == true)
            restrictionsLay.error = "Field cannot be empty!"
        else
            restrictionsLay.error = null

    }

    private fun retrieveTimeSlotData() {


        tsToEdit.title = titleEditText.text.toString()

        tsToEdit.date = dateEditText.text.toString()

        tsToEdit.time = timeEditText.text.toString()

        tsToEdit.duration = durationEditText.text.toString()

        tsToEdit.location = locationEditText.text.toString()

        tsToEdit.description = descriptionEditText.text.toString()

        tsToEdit.restrictions = restrictionsEditText.text.toString()

        return
    }
}


