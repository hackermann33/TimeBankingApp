package it.polito.timebankingapp.ui.timeslots_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import androidx.fragment.app.viewModels
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot

/**
 * A fragment representing a list of Items.
 */
class TimeSlotListFragment : Fragment(R.layout.fragment_timeslots_list) {

    private var columnCount = 1 //credo sia da rimuovere

    val vm by viewModels<TimeSlotsListViewModel>()


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val rv = view.findViewById<RecyclerView>(R.id.time_slot_list)
        rv.layoutManager = LinearLayoutManager(context)

        val adapter= TimeSlotAdapter(createItems(100))
        rv.adapter = adapter
    }


}