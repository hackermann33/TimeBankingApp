package it.polito.timebankingapp.ui.timeslots.timeslot_details

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.chats.chat.ChatViewModel
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel


class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    private val globalModel : TimeSlotsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val chatVm: ChatViewModel by activityViewModels()


    //private lateinit var type: String
    private var isPersonal: Boolean = false
    private lateinit var userId: String

    private lateinit var timeSlot: TimeSlot


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("userId").toString()

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        globalModel.selectedTimeSlot.observe(viewLifecycleOwner) {
            timeSlot = it

            isPersonal = timeSlot.userId == Firebase.auth.uid

            /* If it's not personal, retrieve request infos too */
            if(!isPersonal)
                chatVm.getChat(Helper.makeRequestId(timeSlot.id, Firebase.auth.uid!!)).addOnSuccessListener { req ->
                    showTimeSlot(view, timeSlot, req.toObject<Chat>()?.status ?: Chat().status)
                }
            else
                showTimeSlot(view, timeSlot, null)

        }

        //showTimeSlot(view, arguments?.getSerializable("timeslot") as TimeSlot?) //temp
        /* da decommentare quando si userà decentemente la viewmodel
        model.selected.observe(viewLifecycleOwner, Observer<TimeSlot> { ts ->
            // Update the UI
            showTimeSlot(view, ts)
        })
        */

        setFragmentResultListener("timeSlot") { _, bundle ->
            val result = bundle.getInt("timeSlotConfirm")

            if(result == 2) {
                val snackBar =
                    Snackbar.make(view, "Time slot successfully edited.", Snackbar.LENGTH_LONG)
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            }
            else if(result == 3) {
                val snackBar =
                    Snackbar.make(view, "Something went wrong. Time slot was not edited!", Snackbar.LENGTH_LONG)
                snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
            }
        }
    }


    private fun showTimeSlot(view: View, ts: TimeSlot, requestStatus: Int?) {
        view.findViewById<TextView>(R.id.time_slot_title).text = (ts.title)
        view.findViewById<TextView>(R.id.time_slot_date).text = ts.date
        view.findViewById<TextView>(R.id.time_slot_time).text = ts.time
        view.findViewById<TextView>(R.id.time_slot_duration).text = ts.duration
        view.findViewById<TextView>(R.id.time_slot_location).text = ts.location
        view.findViewById<TextView>(R.id.time_slot_description).text = ts.description
        view.findViewById<TextView>(R.id.time_slot_restrictions).text = ts.restrictions
        val chipSkill = view.findViewById<Chip>(R.id.fragment_time_slot_details_ch_skill)
        chipSkill.layout
        chipSkill.text = ts.relatedSkill


        val clOffererProfile = view.findViewById<ConstraintLayout>(R.id.layout_offerer)
        val civOffererPic = view.findViewById<CircleImageView>(R.id.offerer_pic)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar3)
        val tvOffererName = view.findViewById<TextView>(R.id.offerer_name)
        val rbOffererReviews = view.findViewById<RatingBar>(R.id.fragment_time_slot_details_rb_offerer_review)
        val tvReviewsNumber = view.findViewById<TextView>(R.id.fragment_time_slot_details_tv_reviews_number)

        val btnAskInfo = view.findViewById<Button>(R.id.openChatButton)
        val btnRequestService = view.findViewById<Button>(R.id.button_request_service)


        Helper.loadImageIntoView(civOffererPic, pb, ts.offerer.profilePicUrl)
        tvOffererName.text = ts.offerer.nick
        rbOffererReviews.rating = ts.offerer.asOffererReview.score.toFloat()
        tvReviewsNumber.text = "${ts.offerer.asOffererReview.number} reviews (as offerer)"


        clOffererProfile.setOnClickListener {
            profileViewModel.retrieveTimeSlotProfileData(ts.userId)
            findNavController().navigate(
                R.id.action_nav_timeSlotDetails_to_nav_showProfile,
                bundleOf("point_of_origin" to "skill_specific", "userId" to userId), /* TODO (Edit this bundle in order to avoid casini ) */
            )
        }


        if(isPersonal) {
            clOffererProfile.visibility = View.GONE
            btnAskInfo.visibility = View.GONE
            btnRequestService.visibility = View.GONE
        }
        else{
            //se esiste già una richiesta, disabilita btnRequestService
            /* Retrieve status of the current chat/request */
            btnAskInfo.visibility = View.VISIBLE
            btnRequestService.visibility = View.VISIBLE

            if(requestStatus != Chat.STATUS_UNINTERESTED) {
                Helper.setConfirmationOnButton(requireContext(), btnRequestService)
                clOffererProfile.visibility = View.VISIBLE
                btnAskInfo.text = "Open chat"
                when(requestStatus){
                    Chat.STATUS_INTERESTED -> btnRequestService.text = "Service requested"
                    Chat.STATUS_ACCEPTED -> btnRequestService.text = "Service accepted"
                    Chat.STATUS_COMPLETED -> btnRequestService.text = "Service completed"
                }
            }else { //STATUS_UNINTERESTED
                clOffererProfile.visibility = View.VISIBLE
            }


            btnAskInfo.setOnClickListener {
                showTimeSlotRequest(ts)
                findNavController().navigate(R.id.action_nav_timeSlotDetails_to_nav_chat)
            }



            /* Rememeber to update number of chats for that timeSlot*/
            btnRequestService.setOnClickListener {
                /*val timeSlotToRequest = ts
                val chatId = Helper.makeRequestId(ts.id, Firebase.auth.uid!!)

                globalModel.makeTimeSlotRequest(ts, profileViewModel.user.value!!)
                    .addOnSuccessListener {
                        *//* here I need to update chat *//*
                        chatVm.updateChatInfo(
                            ts,
                            ts.offerer,
                            true
                        )

                        Snackbar.make(view, "Request correctly sent!", Snackbar.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_nav_timeSlotDetails_to_nav_chat)
                    }.addOnFailureListener {
                        Snackbar.make(view, "Oops, something gone wrong!", Snackbar.LENGTH_SHORT)
                            .show()
                    }*/
                /*val btn = it as Button
                btn.text = "Service requested"
                Helper.setConfirmationOnButton(requireContext(), btn)

                */
                chatVm.requestService(Chat(timeSlot = ts, requester = profileViewModel.user.value!!.toCompactUser(), offerer = ts.offerer))
                findNavController().navigate(R.id.action_nav_timeSlotDetails_to_nav_chat)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(isPersonal)
            inflater.inflate(R.menu.menu_editpencil, menu)
        else //skill_specific, completed, interesting
            inflater.inflate(R.menu.menu_showprofile, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                if(isPersonal) {//personal
                    /*Toast.makeText(
                        context, "Edit time slot",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    editTimeslot() //evoked when the pencil button is pressed
                }
                else{
                    /*Toast.makeText(
                        context, "Show user profile",
                        Toast.LENGTH_SHORT
                    ).show()*/

                    profileViewModel.retrieveTimeSlotProfileData(userId)
                    findNavController().navigate(
                        R.id.action_nav_timeSlotDetails_to_nav_showProfile,
                        bundleOf("point_of_origin" to "skill_specific", "userId" to userId) /* TODO (Edit this bundle in order to avoid casini ) */
                    )

                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Show chat from the current request of the current timeSlot */
    fun showTimeSlotRequest(timeSlot: TimeSlot) {
        Log.d("showTimeSlotRequest", "ts: $timeSlot")
        val chatId = Helper.makeRequestId(timeSlot.id, Firebase.auth.uid!!)
            val offerer = timeSlot.offerer

            chatVm.selectChatFromTimeSlot(timeSlot, profileViewModel.user.value!!.toCompactUser() )
    }

    private fun editTimeslot() {
        //launch edit timeslot fragment
        /*
        var temp: TimeSlot = TimeSlot("title_temp", "wow", "12/01/2022", "13:43","32", "Palermo")
        var bundle: Bundle = bundleOf("timeslot" to temp)
        */
        /*val ts = TimeSlot().also {
            it.title = "TitleTrial"; it.description = "Descr trial"; it.date = "2022/12/18"; it.time = "14:15"; it.duration = "56"; it.location = "Turin"
        }*/
        val b = bundleOf("timeslot" to timeSlot)
        findNavController().navigate(R.id.action_nav_timeSlotDetails_to_timeSlotEditFragment, b)
    }
}