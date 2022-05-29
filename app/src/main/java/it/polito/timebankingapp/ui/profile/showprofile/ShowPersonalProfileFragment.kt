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
import it.polito.timebankingapp.ui.reviews.ReviewsViewModel
import it.polito.timebankingapp.ui.reviews.reviewslist.ReviewsViewAdapter


class ShowPersonalProfileFragment : Fragment(R.layout.fragment_showprofile) {

    private lateinit var user: User
    private lateinit var timeSlotUser: User
    private lateinit var reviews: List<Review>

    private lateinit var v : View
    private lateinit var type : String

    private lateinit var rv : RecyclerView
    private lateinit var adTmp: ReviewsViewAdapter

    private val vm : ProfileViewModel by activityViewModels()

    private val rvm by activityViewModels<ReviewsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("point_of_origin").toString() //skill_specific or personal
        if(type == "skill_specific") {
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

        rvm.reviews.observe(viewLifecycleOwner) {
            //print(it)
            reviews = it

            //definizione rw per le recensioni
            rv = view.findViewById(R.id.short_reviews_list)
            rv.layoutManager = LinearLayoutManager(context)
            adTmp = ReviewsViewAdapter(reviews.take(2).toMutableList())
            rv.adapter = adTmp

            val showReviewsBtn : Button = v.findViewById(R.id.show_all_reviews)

            showReviewsBtn.setOnClickListener {
                findNavController().navigate(R.id.action_nav_showProfile_to_nav_reviewsList, bundle)
            }

            val ratingBar = v.findViewById<RatingBar>(R.id.profile_reviews_rating_score)

            if(reviews.isNotEmpty()) {
                var den = 0
                var num = 0
                for (i in reviews.indices) {
                    den++
                    num += reviews[i].stars
                }
                ratingBar.rating = num.toFloat() / den
            }
        }

        if (type == "skill_specific") {
            vm.timeslotUser.observe(viewLifecycleOwner) {
                timeSlotUser = it //oppure it
                bundle = bundleOf("profile" to timeSlotUser, "type" to "timeslot") //per le recensioni
                rvm.retrieveAllReviews(/*it.id*/ " ry0npG5mapRq0ccreqTEQjvdqQa2")
                showProfile(view, timeSlotUser)
            }

            view.findViewById<TextView>(R.id.balanceLabel).visibility  = View.GONE
            view.findViewById<TextView>(R.id.balance).visibility  = View.GONE
            view.findViewById<View>(R.id.divider13).visibility  = View.GONE


        } else { //personal
            vm.user.observe(viewLifecycleOwner) {
                user = it //oppure it
                bundle = bundleOf("profile" to user, "type" to "personal") //per le recensioni
                rvm.retrieveAllReviews(/*it.id*/ " ry0npG5mapRq0ccreqTEQjvdqQa2")
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


        if(!usr.hasImage()){
            pb.visibility = View.GONE
        }
        val dim :Pair<Int,Int> = setProfilePicSize(sv, flProfilePic)
        Helper.loadImageIntoView(profilePicCircleView, pb, usr.profilePicUrl)

        val nameView = view.findViewById<TextView>(R.id.fullName)
        nameView.text = usr.fullName

        val nickView = view.findViewById<TextView>(R.id.nickname)
        nickView.text = usr.nick

        val emailView = view.findViewById<TextView>(R.id.email)
        emailView.text = usr.email//usr.email

        val locationView = view.findViewById<TextView>(R.id.location)
        locationView.text = usr.location

        val balanceView = view.findViewById<TextView>(R.id.balance)
        balanceView.text = usr.balance.toString()

        val descriptionView = view.findViewById<TextView>(R.id.description)
        descriptionView.text = usr.description

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
        if(type != "skill_specific")
            inflater.inflate(R.menu.menu_editpencil, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                val progressBar = v.findViewById<ProgressBar>(R.id.profile_pic_progress_bar)
                editProfile() //evoked when the pencil button is pressed
                if(progressBar.visibility == View.GONE) {
                    Toast.makeText(
                        context, "Edit profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    Toast.makeText(
                        context, "Wait until all has has been retrieved.",
                        Toast.LENGTH_SHORT
                    ).show()
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
