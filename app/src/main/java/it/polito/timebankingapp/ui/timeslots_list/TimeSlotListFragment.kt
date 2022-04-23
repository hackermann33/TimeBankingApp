package it.polito.timebankingapp.ui.timeslots_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.polito.timebankingapp.R

/**
 * A fragment representing a list of Items.
 */
class TimeSlotListFragment : Fragment(R.layout.fragment_timeslots_list) {

    private var columnCount = 1 //credo sia da rimuovere

    val vm : TimeSlotsListViewModel by activityViewModels()
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

        val addTimeSlotButton = view.findViewById<FloatingActionButton>(R.id.addTimeSlotButton)
        var adTmp = TimeSlotAdapter(vm.timeSlots.value?.toMutableList() ?: mutableListOf(), ::selectTimeSlot)
        rv.adapter = adTmp
        vm.timeSlots.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                adTmp = TimeSlotAdapter(it.toMutableList(), ::selectTimeSlot)
                adTmp.data = it.toMutableList()
                rv.adapter = adTmp
            }
        }
        addTimeSlotButton.setOnClickListener { _ ->
/*            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)

 */             findNavController().navigate(R.id.action_timeSlotListFragment_to_nav_timeSlotEdit)
        }

        /*
        val adapter= TimeSlotAdapter(l)
        rv.adapter = adapter
        */
    }

    private fun selectTimeSlot(pos: Int) {
        vm.setSelectedTimeSlot(pos)

    }


}