package it.polito.timebankingapp.ui.reviews.reviewslist

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.review.Review
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.profile.ProfileViewModel

class ReviewsListFragment : Fragment(R.layout.fragment_reviews_list) {
    private lateinit var rv: RecyclerView
    private lateinit var adTmp: ReviewsViewAdapter
    private lateinit var v: View

    private val pvm by activityViewModels<ProfileViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view
        rv = view.findViewById(R.id.reviews_list_rv)
        rv.layoutManager = LinearLayoutManager(context)

        val type = arguments?.getString("profile")
        val usr = arguments?.getSerializable("profile") as User? ?: User()

        val ivProfilePic: ImageView = v.findViewById(R.id.fragment_review_list_iv_profile_pic)
        val pbProfilePic: ProgressBar =
            v.findViewById<ProgressBar>(R.id.fragment_review_list_pb_profile_pic)
        val userName: TextView = v.findViewById(R.id.review_list_user_full_name)


        userName.text = usr.fullName
        pvm.user.observe(viewLifecycleOwner) {
            if (it != null)
                Helper.loadImageIntoView(ivProfilePic, pbProfilePic, it.profilePicUrl)

            val review = Review()
            val tempReviewsList = mutableListOf<Review>()

            tempReviewsList.add(review)
            tempReviewsList.add(review)
            tempReviewsList.add(review)
            tempReviewsList.add(review)
            tempReviewsList.add(review)
            tempReviewsList.add(review)
            tempReviewsList.add(review)
            tempReviewsList.add(review)
            tempReviewsList.add(review)


            adTmp = ReviewsViewAdapter(tempReviewsList)
            rv.adapter = adTmp
        }
    }
}