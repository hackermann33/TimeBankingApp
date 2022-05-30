package it.polito.timebankingapp.ui.timeslots.timeslots_list

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User
import ru.nikartm.support.ImageBadgeView


class TimeSlotAdapter(
    var data: MutableList<TimeSlot>,
    val selectTimeSlot: (t: TimeSlot) ->Unit,
    val requestTimeSlot: ((t: TimeSlot) -> Unit?)?,
    val showRequests: ((t: TimeSlot) -> Unit?)?,
    val type: String,
    val userProfile: User?
) : RecyclerView.Adapter<TimeSlotAdapter.ItemViewHolder>() {

    //private var filter: Boolean = false
    private var displayData = data.toMutableList()
    private var filterKeywords = ""
    private var filterParameter = ""


    class ItemViewHolder(val mainView: View, val type:String, val userProfile: User?) : RecyclerView.ViewHolder(mainView) {
        private val title: TextView = mainView.findViewById(R.id.time_slots_item_title)
        private val location: TextView = mainView.findViewById(R.id.time_slots_item_location)
        private val start: TextView = mainView.findViewById(R.id.time_slots_item_start)
        private val duration: TextView = mainView.findViewById(R.id.time_slots_item_duration)
        private val typeLabel: TextView = mainView.findViewById(R.id.ts_type_label)
        private val typeChip: Chip = mainView.findViewById(R.id.timeslot_item_ts_type)
        private lateinit var editButton: ImageView
        private lateinit var chatButton: ImageBadgeView
        private lateinit var addReviewButton: ImageView


        fun bind(ts: TimeSlot,
                 editAction: (v: View) -> Unit,
                 detailAction: (v: View) -> Unit,
                 requestAction: (v: View) -> Unit,
                 showRequestsAction: (v: View) -> Unit,
                 showAddReviewFrag: (v: View) -> Unit){
            title.text = ts.title
            location.text = ts.location
            start.text = ts.date.plus(" ").plus(ts.time)
            duration.text = ts.duration.plus(" hour(s)")
            editButton = mainView.findViewById(R.id.time_slots_edit_button)
            chatButton = mainView.findViewById(R.id.imageView3)
            addReviewButton = mainView.findViewById(R.id.time_slots_review_button)

            if(type == "personal") {
                editButton.setOnClickListener(editAction)

                /* Set badge options */
                chatButton.setBadgePadding(2)
                chatButton.badgeValue = ts.unreadChats
                chatButton.setOnClickListener(showRequestsAction)
            }
            else if (type == "completed") {
                editButton.visibility = View.GONE
                chatButton.visibility = View.GONE
                addReviewButton.visibility = View.VISIBLE
                typeLabel.visibility = View.VISIBLE
                typeChip.visibility = View.VISIBLE
                if(ts.userId != userProfile?.id ?: ""){
                    typeChip.text = "Requested"
                    typeChip.setChipBackgroundColorResource(R.color.primary_dark)
                }else {
                    typeChip.text = "Offered"
                    typeChip.setChipBackgroundColorResource(R.color.accent)
                }
                addReviewButton.setOnClickListener(showAddReviewFrag)
            }
            else {
                editButton.visibility = View.GONE


                /* remove badge from chat icon when requester */
                chatButton.badgeColor = R.color.background
                chatButton.clearBadge()
                chatButton.setOnClickListener(requestAction)
            }

            this.mainView.setOnClickListener(detailAction)
        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val destination =  R.layout.timeslot_item_layout

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(destination, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return ItemViewHolder(vg, type, userProfile)
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        //holder.name.text = data[position].name
        //holder.role.text = data[position].role

        val item = displayData[position]
        holder.bind(item, editAction =  {//1:17:00
            val pos = data.indexOf(item)
            if (pos != -1) {
                //click on edit button
                if(type == "personal") {
                    Navigation.findNavController(it).navigate(
                        R.id.action_nav_personalTimeSlotList_to_nav_timeSlotEdit,
                        bundleOf("timeslot" to item, "position" to position) //temp
                    )
                }
            }
        }, detailAction = {
            val destination = when(type) {
                "skill_specific" -> R.id.action_skillSpecificTimeSlotListFragment_to_nav_timeSlotDetails
                "personal" -> R.id.action_nav_personalTimeSlotList_to_nav_timeSlotDetails
                "interesting" -> R.id.action_nav_interestingTimeSlotList_to_nav_timeSlotDetails
                else -> {R.id.nav_timeSlotDetails}
            }

            selectTimeSlot(item)
            Navigation.findNavController(it).navigate(
                destination,
                bundleOf("point_of_origin" to type, "userId" to item.userId)
            )
        }, requestAction = {
            //downloadUser(item.userId)
            requestTimeSlot!!(item)
//            val b = bundleOf("profilePic" to item.)
//            b.putString("profileName", item.userName)
//            b.putString("profileId", item.userId)
            val destination = if(type == "skill_specific") R.id.action_nav_skillSpecificTimeSlotList_to_nav_chat else
                R.id.action_nav_interestingTimeSlotList_to_nav_chat
            Navigation.findNavController(it).navigate(destination)
        }, showRequestsAction = {
            showRequests!!(item)
            Navigation.findNavController(it).navigate(R.id.action_nav_personalTimeSlotList_to_nav_timeSlotChatsList)
        }, showAddReviewFrag = {
            Navigation.findNavController(it).navigate(R.id.action_nav_completedTimeSlotList_to_nav_addReview,
                bundleOf("reviewedUserId" to item.userId))
        })


        //Navigation.createNavigateOnClickListener(R.id.action_timeSlotListFragment_to_nav_timeSlotDetails, bundleOf("timeslot" to item)) )
    }

    //how many items?
    override fun getItemCount(): Int = displayData.size

    fun setFilter(keywords: String, parameter: String) {
        filterKeywords = keywords.lowercase().replace("\n", " ").trim()
        filterParameter = parameter

        val newData = if (filterKeywords != "") {
            when(filterParameter){
                "Title" -> data.filter { it.title.lowercase().replace("\n", " ").trim().contains(filterKeywords) }.toMutableList()
                "Location" -> data.filter { it.location.lowercase().replace("\n", " ").trim().contains(filterKeywords) }.toMutableList()
                "Date" -> data.filter { it.date.lowercase().replace("\n", " ").trim().contains(filterKeywords) }.toMutableList()
                "Duration" -> data.filter { it.duration.lowercase().replace("\n", " ").trim().contains(filterKeywords) }.toMutableList()
                else -> {
                    data.toMutableList()
                }
            }
        } else
            data.toMutableList()
        val diffs = DiffUtil.calculateDiff(MyDiffCallback(displayData, newData))
        displayData = newData
        diffs.dispatchUpdatesTo(this)
    }

    fun setOrder(parameter: String, orderingDirection: Boolean) {
        filterParameter = parameter
        val newData = displayData.toMutableList()
        if(orderingDirection)
            when(filterParameter){
                "Title" -> newData.sortByDescending { it.title.lowercase().replace("\n", " ").trim() }
                "Location" -> newData.sortByDescending { it.location.lowercase().replace("\n", " ").trim() }
                "Date" -> newData.sortByDescending { it.date.lowercase().replace("\n", " ").trim() }
                "Duration" -> newData.sortByDescending { it.duration.lowercase().replace("\n", " ").trim() }
                else -> {
                    data.toMutableList()
                }
            }
        else
            when(filterParameter){
                "Title" -> newData.sortBy { it.title.lowercase().replace("\n", " ").trim() }
                "Location" -> newData.sortBy { it.location.lowercase().replace("\n", " ").trim() }
                "Date" -> newData.sortBy { it.date.lowercase().replace("\n", " ").trim() }
                "Duration" -> newData.sortBy { it.duration.lowercase().replace("\n", " ").trim() }
                else -> {
                    data.toMutableList()
                }
            }
        val diffs = DiffUtil.calculateDiff(MyDiffCallback(displayData, newData))
        displayData = newData
        diffs.dispatchUpdatesTo(this)
    }
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