package it.polito.timebankingapp.ui.timeslots.timeslots_calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebankingapp.R
import java.time.LocalDate


data class Event(val id: String, val text: String, val date: LocalDate)

class TimeSlotMonthCalendarEventsAdapter(val onClick: (Event) -> Unit) :
    RecyclerView.Adapter<TimeSlotMonthCalendarEventsAdapter.ItemViewHolder>() {

    val events = mutableListOf<Event>()

    /* override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotMonthCalendarEventsViewHolder {

         return TimeSlotMonthCalendarEventsViewHolder(
             TimeSlotMonthCalendarEventItemBinding.inflate(parent.context.layoutInflater, parent, false)
         )
     }

     inner class TimeSlotMonthCalendarEventsViewHolder(private val binding: TimeSlotMonthCalendarEventItemBinding) :
         RecyclerView.ViewHolder(binding.root) {

         init {
             itemView.setOnClickListener {
                 onClick(events[bindingAdapterPosition])
             }
         }

         fun bind(event: Event) {
             binding.itemEventText.text = event.text
         }
     }
 */

    class ItemViewHolder(private val mainView: View) : RecyclerView.ViewHolder(mainView) {
        private val itemEventText: TextView = mainView.findViewById(R.id.itemEventText)

        fun bind(event: Event) {
            itemEventText.text = event.text
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
        viewHolder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size
}