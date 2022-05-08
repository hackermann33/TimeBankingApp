package it.polito.timebankingapp.ui.timeslots.timeslots_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.google.android.material.snackbar.Snackbar
import it.polito.timebankingapp.R
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel

/**
 * A fragment representing a list of Items.
 */
class SkillSpecificTimeSlotListFragment : Fragment(R.layout.fragment_skill_specific_timeslots_list) {

    private var columnCount = 1 //credo sia da rimuovere

    val vm : TimeSlotsViewModel by activityViewModels()
    private lateinit var rv:RecyclerView

    /*
    private fun createItems(n: Int): MutableList<TimeSlot> {
        val l = mutableListOf<TimeSlot>()
        for (i in 1..n) {
            TimeSlot()
            val ts = TimeSlot().also{
                it.title = "TitleTrial $i";
                it.description= "Descr trial $i";
                it.date = "2022/12/18";
                it.time = "14:15";
                it.duration = "56";
                it.location = "Turin";
            }
            l.add(ts)
            vm.addTimeSlot(ts)
        }
        return l
    }

     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv = view.findViewById<RecyclerView>(R.id.time_slot_list)
        rv.layoutManager = LinearLayoutManager(context)

        val voidMessageImage = view.findViewById<ImageView>(R.id.time_slot_icon)
        val voidMessageText = view.findViewById<TextView>(R.id.emptyListMessage)
        val voidMessageSubText = view.findViewById<TextView>(R.id.empty_list_second_message)

        var adTmp = TimeSlotAdapter(vm.personalTimeSlots.value?.toMutableList() ?: mutableListOf(), ::selectTimeSlot, "skill_specific")
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

    private fun selectTimeSlot(pos: Int) {
        vm.setSelectedTimeSlot(pos)

    }


}