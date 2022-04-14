package it.polito.timebankingapp.ui.timeslot_details

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import it.polito.timebankingapp.R


class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    private val model: SharedViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.selected.observe(viewLifecycleOwner, Observer<TimeSlot> { ts ->
            // Update the UI
            showTimeSlot(view, ts)
        })
    }


    private fun showTimeSlot(view: View, ts: TimeSlot?) {
        view.findViewById<TextView>(R.id.time_slot_title).text = (ts?.title)
        view.findViewById<TextView>(R.id.time_slot_date).text = ts?.date
        view.findViewById<TextView>(R.id.time_slot_time).text = ts?.time
        view.findViewById<TextView>(R.id.time_slot_duration).text = ts?.duration.toString()
        view.findViewById<TextView>(R.id.time_slot_location).text = ts?.location
        view.findViewById<TextView>(R.id.time_slot_description).text = ts?.description

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
        val b = bundleOf("timeslot" to model.selected.value)
        findNavController().navigate(R.id.action_nav_timeSlotDetails_to_timeSlotEditFragment, b)
    }
}