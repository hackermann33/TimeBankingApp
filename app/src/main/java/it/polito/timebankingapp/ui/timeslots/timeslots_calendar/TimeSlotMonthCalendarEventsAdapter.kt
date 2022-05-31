package it.polito.timebankingapp.ui.timeslots.timeslots_calendar

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import java.time.LocalDate

data class Event(val id: String, val ts: TimeSlot, val date: LocalDate, val isOffered: Boolean)
//isOffered == true  --> offered
//          == false --> requested

class TimeSlotMonthCalendarEventsAdapter(/*val vm : TimeSlotsViewModel*/) :
    RecyclerView.Adapter<TimeSlotMonthCalendarEventsAdapter.ItemViewHolder>() {

    val events = mutableListOf<Event>()
    lateinit var context: Context

    class ItemViewHolder(private val mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val tsEstimatedTime: TextView = mainView.findViewById(R.id.calendar_item_ts_duration)
        private val tsTitle: TextView = mainView.findViewById(R.id.calendar_item_ts_title)
        private val tsLocation: TextView = mainView.findViewById(R.id.calendar_item_ts_location)
        private val tsTimestamp: TextView = mainView.findViewById(R.id.calendar_item_ts_timestamp)
        private val tsType: Chip = mainView.findViewById(R.id.calendar_item_ts_type)
        private lateinit var completeButton: ImageView

        fun bind(event: Event, completeTsAction: (v: View) -> Unit,) {
            tsTitle.text = event.ts.title
            tsLocation.text = event.ts.location
            tsTimestamp.text = event.ts.date.plus(" - ").plus(event.ts.time)
            tsEstimatedTime.text = event.ts.duration.plus(" hours")

            completeButton= mainView.findViewById(R.id.calendar_item_ts_complete)
            completeButton.setOnClickListener(completeTsAction)

            if(event.isOffered) {
                tsType.text = "My Offer"
                tsType.setChipBackgroundColorResource(R.color.accent)
            } else {
                tsType.text = "Request"
                tsType.setChipBackgroundColorResource(R.color.primary)
            }
        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val destination =  R.layout.time_slot_month_calendar_event_item

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(destination, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return ItemViewHolder(vg)
    }

    override fun onBindViewHolder(viewHolder: ItemViewHolder, position: Int) {
        viewHolder.bind(events[position], completeTsAction = {
            Navigation.findNavController(it).navigate(
                R.id.action_nav_timeSlotMonthCalendar_to_nav_markTimeSlotAsCompleted,
                bundleOf("timeslot" to events[position].ts)
            )
        })
    }

    override fun getItemCount(): Int = events.size
}