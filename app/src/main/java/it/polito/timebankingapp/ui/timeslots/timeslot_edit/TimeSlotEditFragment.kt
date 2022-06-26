package it.polito.timebankingapp.ui.timeslots.timeslot_edit

import android.os.Bundle
import android.text.InputType
import android.text.format.DateFormat.is24HourFormat
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.MainActivity
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

/** TODO: When edit is confirmed, global view model should be updated (DB)
 **/

val DEBUG = false //DA DISATTIVARE QUANDO SI CONSEGNA IL LAB



class TimeSlotEditFragment : Fragment(R.layout.fragment_time_slot_edit) {

    private val vm by viewModels<TimeSlotsViewModel>()
    private val usrVm by activityViewModels<ProfileViewModel>()

    private var tsToEdit: TimeSlot = TimeSlot()

    private lateinit var v: View

    private lateinit var titleEditText: TextInputEditText
    private lateinit var dateEditText: TextInputEditText
    private lateinit var timeEditText: TextInputEditText
    private lateinit var durationEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var restrictionsEditText: TextInputEditText

    private lateinit var skillsGroup: ChipGroup

    private lateinit var newSkillView: AutoCompleteTextView

    private lateinit var permittedSkills: List<String>



    private var addMode: Boolean = false
    private lateinit var calendar: Calendar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        addMode = arguments?.getSerializable("timeslot") == null
        (requireActivity() as MainActivity).supportActionBar?.title =
            if (addMode) "Add new time slot" else "Edit time slot"
        v = view

        setHasOptionsMenu(true)

        tsToEdit = if (!addMode) arguments?.getSerializable("timeslot") as TimeSlot
        else TimeSlot(offerer = usrVm.user.value!!.toCompactUser())

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


        val addButton = view.findViewById<Button>(R.id.addTimeSlotButton)
        addButton.isVisible = addMode

        if (!addMode) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                handleTimeSlotConfirmation()?.addOnSuccessListener {
                    setFragmentResult("timeSlot", bundleOf("timeSlotConfirm" to 2))
                    findNavController().navigateUp()
                }?.addOnFailureListener{
                    setFragmentResult("timeSlot", bundleOf("timeSlotConfirm" to 3))
                    findNavController().navigateUp()
                }
            }

        } else {
            addButton.setOnClickListener {
                handleTimeSlotConfirmation()
            }
        }

        //skills
        val addSkillButton = view.findViewById<Button>(R.id.addSkillButton)
        skillsGroup = view.findViewById(R.id.edit_timeslot_SkillsGroup)

        permittedSkills  = (usrVm.user.value?.skills ?: listOf())

        usrVm.user.observe(viewLifecycleOwner) {
            if(it != null) {
                permittedSkills = it.skills
            }
        }

        newSkillView = v.findViewById<View>(R.id.edit_timeslot_RelatedSkill) as AutoCompleteTextView
        updateSkillsHints()

        addSkillButton.setOnClickListener {
            var skillStr = newSkillView.text.toString()
            skillStr = skillStr.lowercase()
                .replace("\n", " ")
                .trim()
                .replaceFirstChar { it.uppercase() }
                .replace("\\s+".toRegex(), " ")
                .replaceFirstChar { it.uppercase() }

            if (skillStr.isNotEmpty()) {

                if (tsToEdit.relatedSkill != skillStr && usrVm.user.value?.skills?.contains(skillStr)!! ) {
                    tsToEdit.relatedSkill = skillStr
                    addSkillChip(skillStr)
                }
                else {
                    val relatedSkillLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_RelatedSkillLay)
                    relatedSkillLay.error = "Warning! To add a skill, be sure that you have added it in your profile first!"

                }
                newSkillView.text.clear()
                updateSkillsHints()
            }

        }

        addSkillButton.textSize = (4 * resources.displayMetrics.density)

        showTimeSlot()
    }

    private fun addSkillChip(text: String) {
        val chip = layoutInflater.inflate(
            R.layout.chip_layout_edit,
            skillsGroup.parent.parent as ViewGroup,
            false
        ) as Chip
        chip.text = text
        chip.setOnCloseIconClickListener {
            val ch = it as Chip
            tsToEdit.relatedSkill = ""
            skillsGroup.removeView(ch)
            updateSkillsHints()
        }
        skillsGroup.removeAllViews()
        skillsGroup.addView(chip)
    }

    private fun updateSkillsHints() {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this.requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            permittedSkills.filter { sk -> !tsToEdit.relatedSkill.contains(sk) }
        )
        newSkillView.setAdapter(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (!addMode) {
                    handleTimeSlotConfirmation()?.addOnSuccessListener {
                        setFragmentResult("timeSlot", bundleOf("timeSlotConfirm" to 2))
                        findNavController().navigateUp()
                    }?.addOnFailureListener {
                        setFragmentResult("timeSlot", bundleOf("timeSlotConfirm" to 3))
                        findNavController().navigateUp()
                    }
                }
                else {
                    findNavController().navigateUp()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleTimeSlotConfirmation(): Task<QuerySnapshot>? {

        retrieveTimeSlotData()
        if (tsToEdit.isValid()) {
            //tsToEdit = ts.copy()
            if (addMode) {
                vm.addTimeSlot(tsToEdit).addOnSuccessListener {
                    setFragmentResult("timeSlot", bundleOf("timeSlotConfirm" to 1))
                    findNavController().navigateUp()
                }.addOnFailureListener{
                    setFragmentResult("timeSlot", bundleOf("timeSlotConfirm" to 3))
                    findNavController().navigateUp()
                }
            } else {
                return vm.editTimeSlot(tsToEdit)
            }
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
        return null
    }

    private fun showTimeSlot() {

        titleEditText.setText(tsToEdit.title)
        dateEditText.setText(tsToEdit.date)
        timeEditText.setText(tsToEdit.time)

        durationEditText.setText(tsToEdit.duration)

        locationEditText.setText(tsToEdit.location)

        descriptionEditText.setText(tsToEdit.description)

        restrictionsEditText.setText(tsToEdit.restrictions)

        if(tsToEdit.relatedSkill != "")
            addSkillChip(tsToEdit.relatedSkill)

        if (addMode && DEBUG) {
            titleEditText.setText("titleTmp")
            dateEditText.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString())
            timeEditText.setText("00:00")
            durationEditText.setText("1")
            locationEditText.setText("testLocation")
            descriptionEditText.setText("testDescription")
            restrictionsEditText.setText("testRestrictions")
            tsToEdit.relatedSkill = "Gardening"
            addSkillChip("Gardening")
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
        when {
            titleEditText.text?.isEmpty() == true -> titleLay.error = "Field cannot be empty!"
            titleEditText.text?.length ?: 0 > 30 -> titleLay.error = "Required field too long!"
            else -> titleLay.error = null
        }

        val dateLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DateLay)

        if (dateEditText.text?.isEmpty() == true)
            dateLay.error = "Field cannot be empty!"
        else {
            val todayDate = LocalDate.now()
            val fields = dateEditText.text!!.split("/")
            val thisDate = LocalDate.of(fields[2].toInt(),fields[1].toInt(),fields[0].toInt())
            if (thisDate.isBefore(todayDate))
                dateLay.error = "Chosen date is too old!"
            else
                dateLay.error = null
        }

        val timeLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_TimeLay)

        if (timeEditText.text?.isEmpty() == true)
            timeLay.error = "Field cannot be empty!"
        else
            timeLay.error = null

        val durationLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DurationLay)
        var durationFlag = true
        val duration = durationEditText.text?.toString() ?: "0"
        if (duration.length <= 2){
            if(duration.toInt() < 24)
                durationFlag = false
        }
        when {
            durationEditText.text?.isEmpty() == true -> durationLay.error = "Field cannot be empty!"
            durationFlag -> durationLay.error = "Can't exceed 24 hours!"
            else -> durationLay.error = null
        }

        val locationLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_LocationLay)

        when {
            locationEditText.text?.isEmpty() == true -> locationLay.error = "Field cannot be empty!"
            locationEditText.text?.length ?: 0 > 50 -> locationLay.error = "Required field too long!"
            else -> locationLay.error = null
        }

        val descriptionLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_DescriptionLay)

        when {
            descriptionEditText.text?.isEmpty() == true -> descriptionLay.error = "Field cannot be empty!"
            descriptionEditText.text?.length ?: 0 > 200 -> descriptionLay.error = "Required field too long!"
            else -> descriptionLay.error = null
        }

        val restrictionsLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_RestrictionsLay)

        when {
            restrictionsEditText.text?.isEmpty() == true -> restrictionsLay.error = "Field cannot be empty!"
            restrictionsEditText.text?.length ?: 0 > 100 -> restrictionsLay.error = "Required field too long!"
            else -> restrictionsLay.error = null
        }

        val relatedSkillLay = v.findViewById<TextInputLayout>(R.id.edit_timeslot_RelatedSkillLay)

        when {
            tsToEdit.relatedSkill == "" -> relatedSkillLay.error = "Timeslot needs a related skill!"
            tsToEdit.relatedSkill.length > 30 -> relatedSkillLay.error = "Required field too long!"
            else -> relatedSkillLay.error = null
        }

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


