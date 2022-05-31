package it.polito.timebankingapp.ui.reviews.reviewslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.review.Review


class ReviewsViewAdapter(
    var data: MutableList<Review>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_REVIEWS = 1
    private val VIEW_TYPE_EMPTY_MESSAGE = 2

    private var displayData = data.toMutableList()
    private var isEmpty: Boolean = displayData.isEmpty()
    init {
        if(isEmpty)
            displayData.add(Review()) //singolo elemento == messaggio di lista vuota
    }

    class ItemViewHolder(private val mainView: View ) : RecyclerView.ViewHolder(mainView) {
        private val fullName: TextView = mainView.findViewById(R.id.review_list_item_full_name)
        private val ratingBar: RatingBar = mainView.findViewById(R.id.reviews_item_rating_bar)
        private val timestamp: TextView = mainView.findViewById(R.id.reviews_item_rating_timestamp)
        private val reviewText: TextView = mainView.findViewById(R.id.reviews_item_review_text)
        private var civImagePic: CircleImageView = mainView.findViewById(R.id.review_list_item_profile_pic)
        var pbOtherProfilePic: ProgressBar =itemView.findViewById(R.id.progressBar)

        fun bind(rw: Review, detailAction: (v: View) -> Unit) {
            fullName.text = "Anonymous reviewer"//rw.reviewer.getValue("fullName")
            ratingBar.rating = rw.stars.toFloat()
            timestamp.text = rw.timestamp.toString()
            reviewText.text = rw.reviewText
            //profilePic = qualcosa con glide?
            //Helper.loadImageIntoView(civImagePic, pbOtherProfilePic , rw.reviewer["profilePicUrl"]!!)

            //this.mainView.setOnClickListener(detailAction)
        }
    }

    class EmptyItemViewHolder(private val mainView: View ) : RecyclerView.ViewHolder(mainView) {
        fun bind() {
             //empty
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!isEmpty) {
            VIEW_TYPE_REVIEWS
        } else {
            VIEW_TYPE_EMPTY_MESSAGE
        }
    }

    //inflate the item_layout-based structure inside each ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val vg: View
        return if (viewType == VIEW_TYPE_REVIEWS) {
            vg = LayoutInflater
                    .from(parent.context)
                .inflate(R.layout.reviews_list_item, parent, false)
            ItemViewHolder(vg)
        } else { // (viewType == VIEW_TYPE_EMPTY_MESSAGE) {
            vg = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.reviews_list_item_empty, parent, false)
            EmptyItemViewHolder(vg)
        }
    }

    //populate data for each inflated ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = displayData[position]
        when (holder.itemViewType) {
            VIEW_TYPE_REVIEWS -> (holder as ItemViewHolder).bind(item,detailAction = {} )
            VIEW_TYPE_EMPTY_MESSAGE -> (holder as EmptyItemViewHolder).bind()
        }
    }

    //how many items?
    override fun getItemCount(): Int = displayData.size
}