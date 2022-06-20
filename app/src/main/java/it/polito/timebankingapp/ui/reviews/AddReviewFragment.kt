package it.polito.timebankingapp.ui.reviews

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
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
    private lateinit var review: Review
    private lateinit var reviewedUserNick: TextView
    private lateinit var reviewedUserProfilePic: CircleImageView
    private lateinit var progressBar: ProgressBar


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        v = view

        reviewedUserNick = v.findViewById(R.id.add_review_user_fullname)
        reviewedUserProfilePic = v.findViewById(R.id.add_review_profile_pic)
        progressBar = v.findViewById(R.id.progressBar3)

        rvm.review.observe(viewLifecycleOwner) {
            review = it
            reviewedUserNick.text = review.userToReview.nick
            Helper.loadImageIntoView(reviewedUserProfilePic, progressBar , review.userToReview.profilePicUrl)
            updateUi(view)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_publish_review, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var msg: String = "Before publishing, set at least the rating score."
        return when (item.itemId) {
            R.id.option1 -> {
                if(!review.published) {
                    val rating = ratingBar.rating
                    val text = reviewTextView.text.toString()

                    if (rating >= 1) {
                        review.reviewText = text
                        review.stars = rating.toInt()
                        review.timestamp = java.util.Date()
                        rvm.addReview(review)
                        findNavController().navigateUp()

                        msg = "Review succesfully added"
                    } else {
                        warningLabel.visibility = View.VISIBLE //ERROR MESSAGE
                    }
                }
                else{
                    msg = "You have already reviewed this completed timeSlot!"
                }

                val snackBar = Snackbar.make(
                    v,
                    msg,
                    Snackbar.LENGTH_LONG
                )
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
                true
            }
            else -> {

                super.onOptionsItemSelected(item)}
        }
    }


    fun updateUi(v: View) {
        submitBtn = v.findViewById(R.id.add_review_button)
        ratingBar = v.findViewById(R.id.add_review_rating_bar)
        reviewTextView = v.findViewById(R.id.add_review_text)
        warningLabel = v.findViewById(R.id.ratingWarningLabel)

        if (review.published) {
            Log.d("rev", "$review")
            ratingBar.rating = review.stars.toFloat()
            ratingBar.setIsIndicator(true)
            reviewTextView.text = review.reviewText
            reviewTextView.isEnabled = false

            /*warningLabel.visibility = View.VISIBLE //ERROR MESSAGE
            warningLabel.text = "You've already reviewed this user!"*/
        }
        else {
            ratingBar.rating = 0F
            ratingBar.setIsIndicator(false)
            reviewTextView.text = ""
            reviewTextView.isEnabled = true

        }
    }
}


