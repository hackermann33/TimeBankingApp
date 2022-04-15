package it.polito.timebankingapp.ui.timeslots_list

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.view.View
import android.widget.ImageView

import androidx.core.os.bundleOf
import androidx.navigation.Navigation

import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.R


class TimeSlotAdapter(
    private val data: MutableList<TimeSlot>
    ) : RecyclerView.Adapter<TimeSlotAdapter.ItemViewHolder>() {

    //private var filter: Boolean = false
    private var displayData = data.toMutableList()



    class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.time_slots_item_title)
        private val location: TextView = v.findViewById(R.id.time_slots_item_location)
        private val start: TextView = v.findViewById(R.id.time_slots_item_start)
        private val duration: TextView = v.findViewById(R.id.time_slots_item_duration)
        private val editButton: ImageView = v.findViewById(R.id.time_slots_edit_button)

        fun bind(ts: TimeSlot, action: (v: View) -> Unit) {
            title.text = ts.title
            location.text = ts.location
            start.text = ts.date +" "+ts.time
            duration.text = ts.duration
            editButton.setOnClickListener(action)
        }

        fun unbind() {
            editButton.setOnClickListener(null)
        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.timeslots_item_layout, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return ItemViewHolder(vg)
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //holder.name.text = data[position].name
        //holder.role.text = data[position].role

        val item = displayData[position]
        holder.bind(item) {//1:17:00
            val pos = data.indexOf(item)
            if (pos != -1) {
                //click su bottone

                Navigation.findNavController(it).navigate(
                    R.id.action_timeSlotListFragment_to_nav_timeSlotEdit,
                    //bundleOf( Pair("id",item.id)) //da fixare la prossima volta appena si aggiunge la shared activity viewmodel
                    bundleOf("timeslot" to item) //temp
                )
            }
        }
        //click generico su cardview
        holder.itemView.setOnClickListener( Navigation.createNavigateOnClickListener(R.id.action_timeSlotListFragment_to_nav_timeSlotDetails) )
    }

    //how many items?
    override fun getItemCount(): Int = displayData.size
}




class MyDiffCallback(private val old: List<TimeSlot>, private val new: List<TimeSlot>): DiffUtil.Callback() {
    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] === new[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}






/*

data class Item(val id: Int, val name:String, val role:String )



//Presented items are managed by an Adapter , which is responsible for
//providing and filling views with relevant content from the data set

//Each item in the data set is presented in a recyclable visual hierarchy
//which is managed by a ViewHolder


class ItemAdapter(val data:MutableList<Item>): RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {
    var filter: Boolean = false
    var displayData = data.toMutableList()


    class ItemViewHolder(v:View): RecyclerView.ViewHolder(v) {
        private val name: TextView = v.findViewById(R.id.name)
        private val role: TextView = v.findViewById(R.id.role)
        private val delete: ImageView = v.findViewById(R.id.delete)

        fun bind(item:Item, action: (v:View)->Unit) {
            name.text = item.name
            role.text = item.role
            delete.setOnClickListener(action)
        }
        fun unbind() {
            delete.setOnClickListener(null)
        }
    }

    var vhCount = 0


    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        vhCount++ // max 14 or 15 ViewHolders while executing
        Log.d("ItemAdapter", "$vhCount")
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_layout,parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the viewholder tree of components (could be a ghost item)

        return ItemViewHolder(vg)
    }

    //populate data for each infalted viewholder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //holder.name.text = data[position].name
        //holder.role.text = data[position].role

        val item = displayData[position]
        holder.bind(item) {//1:17:00
            val pos = data.indexOf(item)
            if (pos!=-1) {
                data.removeAt(pos)
                val pos1 = displayData.indexOf(item)
                if (pos1!= -1) {
                    displayData.removeAt(pos1)
                    notifyItemRemoved(pos1)
                }
            }
        }
    }

    //how many items?
    override fun getItemCount(): Int = displayData.size

    fun addFilter(on: Boolean) {
        filter = on
        val newData = if (filter) {
            data.filter { it.id % 2 == 0 }.toMutableList()
        } else
            data.toMutableList()
        val diffs = DiffUtil.calculateDiff(MyDiffCallback(displayData, newData))
        displayData = newData
        diffs.dispatchUpdatesTo(this)
    }
}

class MyDiffCallback(val old: List<Item>, val new: List<Item>): DiffUtil.Callback() {
    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] === new[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}
*/