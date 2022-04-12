package it.polito.timebankingapp.ui.timeslot_details

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.polito.timebankingapp.R


class TimeSlotDetailsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_slot_details, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    lateinit var activity_context: Context ;

    override fun onAttach(context: Context) {
        this.activity_context = context
        super.onAttach(context)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_editpencil, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                Toast.makeText(
                    activity_context, "Edit TimeSlot",
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
        findNavController().navigate(R.id.action_nav_timeSlotDetails_to_timeSlotEditFragment, )
    }
}