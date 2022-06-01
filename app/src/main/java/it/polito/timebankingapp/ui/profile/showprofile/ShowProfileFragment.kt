package it.polito.timebankingapp.ui.profile.showprofile

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import it.polito.timebankingapp.MainActivity
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.review.Review
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.reviews.reviewslist.ReviewsViewAdapter


class ShowProfileFragment : Fragment(R.layout.fragment_showprofile) {

    private lateinit var user: User
    private lateinit var timeSlotUser: User

    private lateinit var v : View
    private lateinit var type : String

/*
    private lateinit var rv : RecyclerView
    private lateinit var adTmp: ReviewsViewAdapter
*/

    private val vm : ProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("point_of_origin").toString() //skill_specific or personal
        if(type == "skill_specific" || type == "completed" || type == "interesting") {
            (activity as MainActivity?)?.setActionBarTitle("Offerer profile")
        }
        else
            setHasOptionsMenu(true)
    }

    /*TODO (Anche questo fragment potrebbe essere unificato) */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        //val progressBar = view.findViewById<ProgressBar>(R.id.profile_pic_progress_bar)
        var bundle = Bundle()

        if (type == "skill_specific" || type == "completed" || type == "interesting") {
            vm.timeslotUser.observe(viewLifecycleOwner) {
                timeSlotUser = it //oppure it
                bundle = bundleOf("profile" to timeSlotUser, "type" to "timeslot") //per le recensioni
                showProfile(view, timeSlotUser)
            }

            view.findViewById<TextView>(R.id.balanceLabel).visibility  = View.GONE
            view.findViewById<TextView>(R.id.balance).visibility  = View.GONE
            view.findViewById<View>(R.id.divider13).visibility  = View.GONE


        } else { //personal
            vm.user.observe(viewLifecycleOwner) {
                user = it //oppure it
                bundle = bundleOf("profile" to user, "type" to "personal") //per le recensioni
                showProfile(view, user)
            }

            setFragmentResultListener("editProfile") { _, bundle ->
                val result = bundle.getBoolean("editProfileConfirm")

                if (result) {
                    val snackBar =
                        Snackbar.make(view, "Profile successfully edited.", Snackbar.LENGTH_LONG)
                    snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
                }
            }
        }

    }

    private fun showProfile(view: View, usr: User) {
        val flProfilePic = view.findViewById<FrameLayout>(R.id.fragment_show_profile_fl_profile_pic)
        val sv = view.findViewById<ScrollView>(R.id.scrollView2)
        val pb = view.findViewById<ProgressBar>(R.id.profile_pic_progress_bar)
        val profilePicCircleView = view.findViewById<ImageView>(R.id.fragment_show_profile_iv_profile_pic)

        val tvName = view.findViewById<TextView>(R.id.fullName)
        val tvNick = view.findViewById<TextView>(R.id.nickname)
        val tvEmail = view.findViewById<TextView>(R.id.email)
        val tvLocation = view.findViewById<TextView>(R.id.location)
        val tvBalance = view.findViewById<TextView>(R.id.balance)
        val tvDescription = view.findViewById<TextView>(R.id.description)

        /* Review part */
        val btnShowReviews : Button = v.findViewById(R.id.show_all_reviews)
        val rvLastReviews: RecyclerView = view.findViewById(R.id.short_reviews_list)
        val rbAsOffererRating = v.findViewById<RatingBar>(R.id.profile_reviews_rating_score_offerer)
        val rbAsRequesterRating = v.findViewById<RatingBar>(R.id.profile_reviews_rating_score_requester)



        if(!usr.hasImage()){
            pb.visibility = View.GONE
        }

        Helper.loadImageIntoView(profilePicCircleView, pb, usr.profilePicUrl)

        tvName.text = usr.fullName

        tvNick.text = usr.nick

        tvEmail.text = usr.email//usr.email

        tvLocation.text = usr.location

        tvBalance.text = usr.balance.toString()

        tvDescription.text = usr.description

        val chipGroup = view.findViewById<ChipGroup>(R.id.skillsGroup)

        chipGroup.removeAllViews()
        usr.skills.forEach { skill ->
            val chip = layoutInflater.inflate(
                R.layout.chip_layout_show,
                chipGroup!!.parent.parent as ViewGroup,
                false
            ) as Chip
            chip.text = skill
            chipGroup.addView(chip)
        }

        /* Review part */
        val rvaReview = ReviewsViewAdapter(usr.reviews.toMutableList())
        rvLastReviews.layoutManager = LinearLayoutManager(context)
        rvLastReviews.adapter = rvaReview
        rbAsOffererRating.rating = usr.getReviewsScore(Review.AS_OFFERER_TYPE).toFloat()
        rbAsRequesterRating.rating = usr.getReviewsScore(Review.AS_REQUESTER_TYPE).toFloat()

    }


    private fun setProfilePicSize(sv: ScrollView, flProfilePic: FrameLayout): Pair<Int, Int>{
        var h = 0
        var w = 0

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {

            sv.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                     h = sv.height
                     w = sv.width
                    flProfilePic.post {
                        flProfilePic.layoutParams = LinearLayout.LayoutParams(w, h / 3)
                    }
                    sv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
        return Pair(h,w)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(type != "skill_specific" || type != "completed" || type != "interesting")
            inflater.inflate(R.menu.menu_editpencil, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                val progressBar = v.findViewById<ProgressBar>(R.id.profile_pic_progress_bar)

                if(progressBar.visibility == View.GONE) {
			        editProfile() //evoked when the pencil button is pressed
                    /*Toast.makeText(
                        context, "Edit profile",
                        Toast.LENGTH_SHORT
                    ).show()*/ 
                }
                else {
                    /*Toast.makeText(
                        context, "Wait until all has has been retrieved.",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    val snackBar = Snackbar.make(v, "Wait until all has has been retrieved.", Snackbar.LENGTH_SHORT)
                    snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        //launch edit profile fragment
        val b = bundleOf("profile" to user)
        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileActivity, b)
    }

}
