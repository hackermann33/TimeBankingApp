package it.polito.timebankingapp.ui.reviews.reviewslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.review.Review


class ReviewsViewAdapter(
    var data: MutableList<Review>
) : RecyclerView.Adapter<ReviewsViewAdapter.ItemViewHolder>() {

    private var displayData = data.toMutableList()

    class ItemViewHolder(private val mainView: View ) : RecyclerView.ViewHolder(mainView) {
        private val fullName: TextView = mainView.findViewById(R.id.review_list_item_full_name)
        private val ratingBar: RatingBar = mainView.findViewById(R.id.reviews_item_rating_bar)
        private val timestamp: TextView = mainView.findViewById(R.id.reviews_item_rating_timestamp)
        private val reviewText: TextView = mainView.findViewById(R.id.reviews_item_review_text)
        private var profilePic: CircleImageView = mainView.findViewById(R.id.review_list_item_profile_pic)

        fun bind(rw: Review, editAction: (v: View) -> Unit, detailAction: (v: View) -> Unit, requestAction: (v: View) -> Unit, showRequestsAction: (v: View) -> Unit) {
           /* fullName.text = ts.title
            ratingBar.numStars = rw.numStars
            timestamp.text = rw.timestamp
            reviewText.text = rw.reviewText
            profilePic = qualcosa con glide?
            //this.mainView.setOnClickListener(detailAction)
            */
        }

    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val destination =  R.layout.reviews_list_item

        val vg = LayoutInflater
            .from(parent.context)
            .inflate(destination, parent, false) //attachToRoot: take all you measures
        //but do not attach it immediately to the ViewHolder tree of components (could be a ghost item)

        return ItemViewHolder(vg)
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
 /*
        val item = displayData[position]

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
        }, requestAction = {

            requestTimeSlot!!(item)
            Navigation.findNavController(it).navigate(R.id.action_nav_timeSlotList_to_nav_chat)
        }, showRequestsAction = {
            showRequests!!(item)
            Navigation.findNavController(it).navigate(R.id.action_nav_timeSlotList_to_nav_chatsList)
        })

*/
    }

    //how many items?
    override fun getItemCount(): Int = displayData.size
}