package it.polito.timebankingapp.ui.chat

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot

import it.polito.timebankingapp.ui.chat.placeholder.PlaceholderContent.PlaceholderItem
//import it.polito.timebankingapp.ui.chat.databinding.FragmentChatBinding
import it.polito.timebankingapp.ui.timeslots.timeslots_list.TimeSlotAdapter
import kotlin.reflect.KFunction1

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */

class ChatViewAdapter(
    var data: MutableList<TimeSlot>,
    val selectTimeSlot: KFunction1<TimeSlot, Unit>,
    val type: String
) : RecyclerView.Adapter<TimeSlotAdapter.ItemViewHolder>() {

    class ItemViewHolder(val mainView: View, val type:String) : RecyclerView.ViewHolder(mainView) {



        fun bind(ts: TimeSlot, editAction: (v: View) -> Unit, detailAction: (v: View) -> Unit) {

        }

        fun unbind() {

        }
    }
    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotAdapter.ItemViewHolder {
        val destination =  R.layout.timeslot_item_layout

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(destination, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return TimeSlotAdapter.ItemViewHolder(vg, type)
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: TimeSlotAdapter.ItemViewHolder, position: Int) {
        //holder.name.text = data[position].name
        //holder.role.text = data[position].role

        /*val item = displayData[position]
        holder.bind(item, editAction =  {//1:17:00
            val pos = data.indexOf(item)
            if (pos != -1) {
                //click on edit button
                if(type != "skill_specific") {
                    Navigation.findNavController(it).navigate(
                        R.id.action_nav_skillSpecificTimeSlotList_to_nav_timeSlotEdit,
                        //bundleOf( Pair("id",item.id)) //da fixare la prossima volta appena si aggiunge la shared activity viewmodel
                        bundleOf("timeslot" to item, "position" to position) //temp
                    )
                }
            }
        }, detailAction = {
            val destination =
                R.id.action_skillSpecificTimeSlotListFragment_to_nav_timeSlotDetails


            selectTimeSlot(item)
            Navigation.findNavController(it).navigate(
                destination,
                bundleOf("point_of_origin" to type, "userId" to item.userId)
            )
        });*/



        //Navigation.createNavigateOnClickListener(R.id.action_timeSlotListFragment_to_nav_timeSlotDetails, bundleOf("timeslot" to item)) )
    }

    override fun getItemCount(): Int = 2 /*displayData.size*/

}

/*
class ChatViewAdapter(
    private val values: List<PlaceholderItem>
) : RecyclerView.Adapter<ChatViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentChatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.contentView.text = item.content
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentChatBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}*/