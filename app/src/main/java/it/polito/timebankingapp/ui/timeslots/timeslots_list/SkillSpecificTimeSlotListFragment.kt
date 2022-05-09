package it.polito.timebankingapp.ui.timeslots.timeslots_list

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel

/**
 * A fragment representing a list of Items.
 */
class SkillSpecificTimeSlotListFragment : Fragment(R.layout.fragment_skill_specific_timeslots_list) {

    private var columnCount = 1 //credo sia da rimuovere

    val vm : TimeSlotsViewModel by activityViewModels()
    private lateinit var rv:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv = view.findViewById(R.id.time_slot_list)
        rv.layoutManager = LinearLayoutManager(context)

        val voidMessageImage = view.findViewById<ImageView>(R.id.time_slot_icon)
        val voidMessageText = view.findViewById<TextView>(R.id.emptyListMessage)
        val voidMessageSubText = view.findViewById<TextView>(R.id.empty_list_second_message)

        var adTmp = TimeSlotAdapter(vm.globalTimeSlots.value?.toMutableList() ?: mutableListOf(), ::selectTimeSlot, "skill_specific")
        rv.adapter = adTmp

        var skill = arguments?.getString("skill")

        vm.globalTimeSlots.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                voidMessageText.isVisible = false
                voidMessageImage.isVisible = false
                voidMessageSubText.isVisible = false

                adTmp = TimeSlotAdapter(it.filter{it.relatedSkill ==  skill || skill == null  }.toMutableList(), ::selectTimeSlot, "skill_specific")
                adTmp.data = it.toMutableList()
                rv.adapter = adTmp
            }
            else{
                voidMessageText.isVisible = true
                voidMessageImage.isVisible = true
                voidMessageSubText.isVisible = true

            }
        }


        /*
        val adapter= TimeSlotAdapter(l)
        rv.adapter = adapter
        */


        setFragmentResultListener("timeSlot") { _, bundle ->
            val result = bundle.getInt("timeSlotConfirm")

            if(result == 1){
                val snackBar = Snackbar.make(view, "New time slot successfully added.", Snackbar.LENGTH_LONG)
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            }
            else if(result == 2) {
                val snackBar =
                    Snackbar.make(view, "Time slot successfully edited.", Snackbar.LENGTH_LONG)
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            }
        }


    }

    private fun selectTimeSlot(ts: TimeSlot) {
        vm.setSelectedTimeSlot(ts)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_filter_and_sort, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_option -> {
                /* sort by something */
                true
            }
            R.id.filter_option -> {
                /* filter by something*/
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}