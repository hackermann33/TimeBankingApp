package it.polito.timebankingapp.ui.profile.showprofile

import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.MainActivity
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.profile.ProfileViewModel


class ShowPersonalProfileFragment : Fragment(R.layout.fragment_showprofile) {

    private lateinit var usr: User
    private lateinit var loggedUser: FirebaseUser
    private lateinit var v : View
    private lateinit var type : String

    val vm : ProfileViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = arguments?.getString("point_of_origin").toString() //skill_specific or personal
        if(type == "skill_specific") {
            var userId = arguments?.getString("userId").toString()
            vm.retrieveTimeSlotProfileData(userId)
            (activity as MainActivity?)?.setActionBarTitle("Offerer profile")
        }
        else
            setHasOptionsMenu(true)
    }

    override fun onDetach() {
        vm.clearTimeSlotUserImage()
        super.onDetach()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        v = view

        val navController = findNavController()
        val profilePicCircleView = view.findViewById<CircleImageView>(R.id.profile_pic)
        val progressBar = view.findViewById<ProgressBar>(R.id.profile_pic_progress_bar)


        if (type == "skill_specific") {
            if (vm.timeslotUserImage.value == null)
                progressBar.visibility = View.GONE

            vm.timeslotUser.observe(viewLifecycleOwner) {
                usr = it //oppure it
                showProfile(view)
            }

            vm.timeslotUserImage.observe(viewLifecycleOwner) {
                if (it != null)
                    profilePicCircleView.setImageBitmap(it)
                else
                    profilePicCircleView.setImageBitmap(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.default_avatar
                        )
                    )
                progressBar.visibility = View.GONE
            }

        } else { //personal
            if (vm.userImage.value == null)
                progressBar.visibility = View.GONE

            vm.fireBaseUser.observe(viewLifecycleOwner) {
                if (it != null) {
                    loggedUser = it
                    usr = vm.user.value!!
                    showProfile(view)
                } else
                    navController.navigate(R.id.action_nav_showProfile_to_nav_login)
            }

            vm.userImage.observe(viewLifecycleOwner) {
                if (it != null) {
                    profilePicCircleView.setImageBitmap(it)
                    progressBar.visibility = View.GONE
                } else {
                    profilePicCircleView.setImageResource(R.drawable.default_avatar)
                }
            }

            //loggedUser = usrVm.userProfile.value!!
            //showProfile(view)

            /*sharedPref = requireActivity().getPreferences(android.content.Context.MODE_PRIVATE)

        val profile = sharedPref.getString("profile", "")
        usr = if (sharedPref.contains("profile")) GsonBuilder().create()
            .fromJson(profile, User::class.java)
        else User()

         */

            //usr = savedInstanceState?.getSerializable("user") as User


            /*setFragmentResultListener("profile") { requestKey, bundle ->
            usr = bundle.getSerializable("user") as User
            showProfile(view)
            val jsonString = GsonBuilder().create().toJson(usr)
            with(sharedPref.edit()) {
                putString("profile", jsonString)
                apply()
            }
        }

        showProfile(view) */

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

    private fun showWelcomeMessage() {
        TODO("Not yet implemented")
    }

    private fun showProfile(view: View) {
        val profilePic = view.findViewById<CircleImageView>(R.id.profile_pic)
        val frameLayout = view.findViewById<FrameLayout>(R.id.frame_layout_pic)
        val sv = view.findViewById<ScrollView>(R.id.scrollView2)
        val progressBar = view.findViewById<ProgressBar>(R.id.profile_pic_progress_bar)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            sv.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val h = sv.height
                    val w = sv.width
                    /*profilePic.post {
                        profilePic.layoutParams = LinearLayout.LayoutParams(w, h / 3)
                    }
                    sv.viewTreeObserver.removeOnGlobalLayoutListener(this)*/
                    frameLayout.post { frameLayout.layoutParams = LinearLayout.LayoutParams(w, h / 3) }
                    sv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
        /*try {
            if(usr.tempImagePath == "") {
                val cw = ContextWrapper(requireContext())
                vm.retrieveAndSetProfilePic(usr, profilePic, progressBar,cw)
            }else {
                progressBar.visibility = View.GONE
                val f = File(usr.tempImagePath) //loggedUser.photoUrl (già salvata in locale)
                val bitmap = BitmapFactory.decodeStream(FileInputStream(f))
                profilePic.setImageBitmap(bitmap)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }*/

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
                /*if(progressBar.visibility == View.GONE) {
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
                }*/
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        //launch edit profile fragment
        val b = bundleOf("profile" to usr)
        findNavController().navigate(R.id.action_showProfileFragment_to_editProfileActivity, b)
    }

}
