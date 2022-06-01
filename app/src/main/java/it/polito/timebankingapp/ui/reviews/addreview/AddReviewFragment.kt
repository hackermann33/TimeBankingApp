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

    private val pvm: ProfileViewModel by activityViewModels()
    private val rvm by activityViewModels<ReviewsViewModel>()

    private lateinit var v: View

    //private lateinit var user: User
    private lateinit var reviewedTimeSlot: TimeSlot
    private lateinit var reviewedUserId: String
    private lateinit var submitBtn: Button
    private lateinit var ratingBar: RatingBar
    private lateinit var reviewTextView: TextView
    private lateinit var warningLabel: TextView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        rvm.review.observe(viewLifecycleOwner) {
            updateUi(view, it)
        }
    }

    fun updateUi(v: View, r: Review) {
        submitBtn = v.findViewById(R.id.add_review_button)
        ratingBar = v.findViewById(R.id.add_review_rating_bar)
        reviewTextView = v.findViewById(R.id.add_review_text)
        warningLabel = v.findViewById(R.id.ratingWarningLabel)

        if (!r.published) {
            submitBtn.setOnClickListener {
                val rating = ratingBar.rating
                val text = reviewTextView.text.toString()

                if (rating >= 1) {
                    r.reviewText = text
                    r.stars = rating.toInt()
                    r.timestamp = java.util.Date()
                    rvm.addReview(r)
                    findNavController().navigateUp()
                    Toast.makeText(activity, "Review successfully added!", Toast.LENGTH_SHORT)
                        .show();
                } else {
                    warningLabel.visibility = View.VISIBLE //ERROR MESSAGE
                }
            }
        } else {
            submitBtn.setOnClickListener {
                warningLabel.visibility = View.VISIBLE //ERROR MESSAGE
                warningLabel.text = "You've already reviewed this user!"
                //Toast.makeText(activity,"You've already reviewed this user!", Toast.LENGTH_LONG).show();
            }
        }

    }
}


