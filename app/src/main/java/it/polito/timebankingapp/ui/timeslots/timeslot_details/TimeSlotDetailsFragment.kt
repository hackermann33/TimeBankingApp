package it.polito.timebankingapp.ui.timeslots.timeslot_details

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel


class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    val globalModel : TimeSlotsViewModel by activityViewModels()
    private lateinit var timeSlotToEdit: TimeSlot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ts = globalModel.selectedTimeSlot.value
        globalModel.selectedTimeSlot.observe(viewLifecycleOwner) {
            timeSlotToEdit = ts ?: TimeSlot()
            /*if (ts != null) {
                if(ts.date.isNotEmpty()) ts.date.replace("_", "/")
            }*/
            showTimeSlot(view, it)
        }

        //showTimeSlot(view, arguments?.getSerializable("timeslot") as TimeSlot?) //temp
        /* da decommentare quando si userà decentemente la viewmodel
        model.selected.observe(viewLifecycleOwner, Observer<TimeSlot> { ts ->
            // Update the UI
            showTimeSlot(view, ts)
        })
        */

        setFragmentResultListener("timeSlot") { _, bundle ->
            val result = bundle.getInt("timeSlotConfirm")

            if(result == 2) {
                val snackBar =
                    Snackbar.make(view, "Time slot successfully edited.", Snackbar.LENGTH_LONG)
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            }
        }
    }


    private fun showTimeSlot(view: View, ts: TimeSlot?) {
        view.findViewById<TextView>(R.id.time_slot_title).text = (ts?.title)
        view.findViewById<TextView>(R.id.time_slot_date).text = ts?.date
        view.findViewById<TextView>(R.id.time_slot_time).text = ts?.time
        view.findViewById<TextView>(R.id.time_slot_duration).text = ts?.duration.toString()
        view.findViewById<TextView>(R.id.time_slot_location).text = ts?.location
        view.findViewById<TextView>(R.id.time_slot_description).text = ts?.description
        view.findViewById<TextView>(R.id.time_slot_restrictions).text = ts?.restrictions

        val chipGroup = view.findViewById<ChipGroup>(R.id.time_slot_skillsGroup)

        chipGroup.removeAllViews()
        val chip = layoutInflater.inflate(
            R.layout.chip_layout_showprofile,
            chipGroup!!.parent.parent as ViewGroup,
            false
        ) as Chip
        chip.text = ts?.relatedSkill
        chipGroup.addView(chip)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editpencil, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                Toast.makeText(
                    context, "Edit TimeSlot",
                    Toast.LENGTH_SHORT
                ).show()
                editTimeslot() //evoked when the pencil button is pressed
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editTimeslot() {
        //launch edit timeslot fragment
        /*
        var temp: TimeSlot = TimeSlot("title_temp", "wow", "12/01/2022", "13:43","32", "Palermo")
        var bundle: Bundle = bundleOf("timeslot" to temp)
        */
        /*val ts = TimeSlot().also {
            it.title = "TitleTrial"; it.description = "Descr trial"; it.date = "2022/12/18"; it.time = "14:15"; it.duration = "56"; it.location = "Turin"
        }*/
        val b = bundleOf("timeslot" to timeSlotToEdit)
        findNavController().navigate(R.id.action_nav_timeSlotDetails_to_timeSlotEditFragment, b)
    }
}