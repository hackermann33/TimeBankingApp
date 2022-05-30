package it.polito.timebankingapp.ui.reviews.addreview

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.review.Review
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.reviews.ReviewsViewModel


class AddReviewFragment : Fragment(R.layout.fragment_add_review) {

    private val pvm : ProfileViewModel by activityViewModels()
    private val rvm by activityViewModels<ReviewsViewModel>()

    private lateinit var v : View

    private lateinit var user: User
    private lateinit var newReview: Review
    private lateinit var reviewedTimeSlot: TimeSlot

    private lateinit var submitBtn: Button
    private lateinit var ratingBar: RatingBar
    private lateinit var reviewTextView: TextView
    private lateinit var warningLabel: TextView


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        reviewedTimeSlot = arguments?.getSerializable("timeslot") as TimeSlot
        //if (reviewedUserId == "null") reviewedUserId = "ry0npG5mapRq0ccreqTEQjvdqQa2"
        rvm.checkIfAlreadyReviewed(reviewedTimeSlot.userId)
        //rvm.retrieveRequesterInfo(reviewedTimeSlot)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view


        pvm.user.observe(viewLifecycleOwner) {
            user = it
            val tempMap = mutableMapOf<String, String>()
            tempMap["id"] = user.id
            tempMap["fullName"] = user.fullName
            tempMap["profilePicUrl"] = user.profilePicUrl
            newReview = Review(reviewer = tempMap, role= "requester")
        }

        submitBtn = v.findViewById(R.id.add_review_button)
        ratingBar = v.findViewById(R.id.add_review_rating_bar)
        reviewTextView = v.findViewById(R.id.add_review_text)
        warningLabel = v.findViewById(R.id.ratingWarningLabel)

        rvm.alreadyReviewed.observe(viewLifecycleOwner){
            if(!it) { //if a review already exists
                submitBtn.setOnClickListener {
                    val rating = ratingBar.rating
                    val text = reviewTextView.text.toString()

                    if (rating >= 1) {
                        newReview.reviewText = text
                        newReview.stars = rating.toInt()
                        newReview.timestamp = java.util.Date()
                        rvm.addReview(newReview, reviewedTimeSlot.userId)
                        findNavController().navigateUp()
                        Toast.makeText(activity, "Review successfully added!", Toast.LENGTH_SHORT)
                            .show();
                    } else {
                        warningLabel.visibility = View.VISIBLE //ERROR MESSAGE
                    }
                }
            } else{
                submitBtn.setOnClickListener {
                    warningLabel.visibility = View.VISIBLE //ERROR MESSAGE
                    warningLabel.text = "You've already reviewed this user!"
                    //Toast.makeText(activity,"You've already reviewed this user!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}