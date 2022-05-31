package it.polito.timebankingapp.ui.timeslots.timeslots_calendar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import java.time.LocalDate

data class Event(val id: String, val ts: TimeSlot, val date: LocalDate, val isOffered: Boolean)
//isOffered == true  --> offered
//          == false --> requested

class TimeSlotMonthCalendarEventsAdapter(/*val vm : TimeSlotsViewModel*/) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val events = mutableListOf<Event>()
    private var isEmpty: Boolean = events.isEmpty()

    lateinit var context: Context

    private val VIEW_TYPE_EVENT_LIST = 1
    private val VIEW_TYPE_EMPTY_MESSAGE = 2

    init {
        if(isEmpty)
            events.add(Event("", TimeSlot(), LocalDate.now(), false)) //singolo elemento == messaggio di lista vuota
    }

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

    class EmptyItemViewHolder(private val mainView: View ) : RecyclerView.ViewHolder(mainView) {
        fun bind() {
            //empty
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!isEmpty) {
            VIEW_TYPE_EVENT_LIST
        } else {
            VIEW_TYPE_EMPTY_MESSAGE
        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vg: View
        return if (viewType == VIEW_TYPE_EVENT_LIST) {
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.time_slot_month_calendar_event_item, parent, false)
            ItemViewHolder(vg)
        } else { // (viewType == VIEW_TYPE_EMPTY_MESSAGE) {
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.time_slot_month_calendar_event_empty_item, parent, false)
            EmptyItemViewHolder(vg)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            VIEW_TYPE_EVENT_LIST-> (viewHolder as ItemViewHolder).bind(events[position], completeTsAction = {
                Navigation.findNavController(it).navigate(
                    R.id.action_nav_timeSlotMonthCalendar_to_nav_markTimeSlotAsCompleted,
                    bundleOf("timeslot" to events[position].ts)
                )
            })
            VIEW_TYPE_EMPTY_MESSAGE -> (viewHolder as EmptyItemViewHolder).bind()
        }
    }

    override fun getItemCount(): Int = events.size
}