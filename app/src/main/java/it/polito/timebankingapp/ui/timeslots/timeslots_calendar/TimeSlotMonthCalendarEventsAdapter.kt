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
import com.google.android.material.snackbar.Snackbar
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import java.time.LocalDate
import java.time.LocalDateTime

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
        private val tsUserNickname: TextView = mainView.findViewById(R.id.calendar_item_ts_nickname)
        private val tsUserNicknameLabel: TextView = mainView.findViewById(R.id.nickname_label)
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
                tsUserNickname.text = event.ts.assignedTo.nick
                tsUserNicknameLabel.text = "Requester nickname:"
            } else {
                tsType.text = "Request"
                tsType.setChipBackgroundColorResource(R.color.primary)
                tsUserNickname.text = event.ts.offerer.nick
                tsUserNicknameLabel.text = "Offerer nickname:"
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
            val dateValues = events[position].ts.date.split("/")
            val timeValues = events[position].ts.time.split(":")
            val duration = events[position].ts.duration.toInt()


            val expectedTsTimeStamp = LocalDateTime.of(dateValues[2].toInt(),dateValues[1].toInt(),
                dateValues[0].toInt(),timeValues[0].toInt()+duration,timeValues[0].toInt())

            if(expectedTsTimeStamp.isBefore(LocalDateTime.now()))
                Navigation.findNavController(it).navigate(
                    R.id.action_nav_timeSlotMonthCalendar_to_nav_markTimeSlotAsCompleted,
                    bundleOf("timeslot" to events[position].ts)
                )
            else {
                val snackBar = Snackbar.make(
                    viewHolder.itemView,
                    "You can mark it as completed only after ".plus(expectedTsTimeStamp.toString().replace("T", " ")),
                    Snackbar.LENGTH_LONG
                )
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            }
        })
    }

    override fun getItemCount(): Int = events.size
}
