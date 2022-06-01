package it.polito.timebankingapp.ui.timeslots.timeslot_details

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.ui.chats.chat.ChatFragment
import it.polito.timebankingapp.ui.chats.chat.ChatViewModel
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel


class TimeSlotDetailsFragment : Fragment(R.layout.fragment_time_slot_details) {

    private val globalModel : TimeSlotsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val chatVm: ChatViewModel by activityViewModels()

    private lateinit var timeSlot: TimeSlot
    private lateinit var type: String
    private lateinit var userId: String

    private lateinit var btnRequestService: Button

    private var status: Int = Chat.STATUS_UNINTERESTED


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("userId").toString()
        type = arguments?.getString("point_of_origin").toString() //skill_specific or personal


//        profileViewModel.retrieveTimeSlotProfileData(userId)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnRequestService = view.findViewById<Button>(R.id.button_request_service)

        val ts = globalModel.selectedTimeSlot.value


        /*if( status != Chat.STATUS_UNINTERESTED) {
            btnRequestService.text = "SERVICE REQUESTED"
            Helper.setConfirmationOnButton(requireContext(), btnRequestService)
        }*/



        val btnAskInfo = view.findViewById<Button>(R.id.openChatButton)
        if(ts!!.userId == Firebase.auth.uid){
            btnAskInfo.isEnabled = false
            btnAskInfo.visibility = View.INVISIBLE
            btnRequestService.isEnabled = false
            btnRequestService.visibility = View.GONE
        }
        /*chatVm.chat.observe(viewLifecycleOwner) {
            btnRequestService.isEnabled = it.status == Chat.STATUS_UNINTERESTED
        }*/


        globalModel.selectedTimeSlot.observe(viewLifecycleOwner) {
            timeSlot = ts ?: TimeSlot()
            /*if (ts != null) {
                if(ts.date.isNotEmpty()) ts.date.replace("_", "/")
            }*/
            showTimeSlot(view, it)

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
        }
    }


    private fun showTimeSlot(view: View, ts: TimeSlot?) {
        view.findViewById<TextView>(R.id.time_slot_title).text = (ts?.title)
        view.findViewById<TextView>(R.id.time_slot_date).text = ts?.date
        view.findViewById<TextView>(R.id.time_slot_time).text = ts?.time
        view.findViewById<TextView>(R.id.time_slot_duration).text = ts?.duration.toString()
        view.findViewById<TextView>(R.id.time_slot_location).text = ts?.location
        view.findViewById<TextView>(R.id.time_slot_description).text = ts?.description
        view.findViewById<TextView>(R.id.time_slot_restrictions).text = ts?.restrictions


        val civOffererPic = view.findViewById<CircleImageView>(R.id.offerer_pic)
        val pb = view.findViewById<ProgressBar>(R.id.progressBar3)
        val tvOffererName = view.findViewById<TextView>(R.id.offerer_name)


        Helper.loadImageIntoView(civOffererPic, pb, ts!!.offerer.profilePicUrl)
        tvOffererName.text = ts.offerer.nick
        if (type == "skill_specific") {

            //se esiste già una richiesta, disabilita btnRequestService
            if (type == "skill_specific" && ts.status == TimeSlot.TIME_SLOT_STATUS_AVAILABLE)
                btnRequestService.visibility = View.VISIBLE

            view.findViewById<ConstraintLayout>(R.id.layout_offerer)
                .also { it.visibility = View.VISIBLE }

            /* Retrieve status of the current chat*/
            chatVm.getChat(Helper.makeRequestId(ts!!.id, Firebase.auth.uid!!)).addOnSuccessListener {
                status = it.toObject<Chat>()?.status ?: Chat.STATUS_UNINTERESTED

                if(status != Chat.STATUS_UNINTERESTED) {
                    btnRequestService.text = "Service requested"
                    Helper.setConfirmationOnButton(requireContext(), btnRequestService)

                }/*btnRequestService.isEnabled = status == Chat.STATUS_UNINTERESTED    */
            }


            val btnAskInfo = view.findViewById<Button>(R.id.openChatButton)



            btnAskInfo.setOnClickListener {
                showTimeSlotRequest(timeSlot)
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
                val btn = it as Button
                btn.text = "service requested"
                Helper.setConfirmationOnButton(requireContext(), btn)

                chatVm.requestService(Chat(timeSlot = ts, requester = profileViewModel.user.value!!.toCompactUser(), offerer = ts.offerer))
            }

        }
        val chipGroup = view.findViewById<ChipGroup>(R.id.time_slot_skillsGroup)

        chipGroup.removeAllViews()
        val chip = layoutInflater.inflate(
            R.layout.chip_layout_show,
            chipGroup!!.parent.parent as ViewGroup,
            false
        ) as Chip
        chip.text = ts?.relatedSkill
        chipGroup.addView(chip)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if(type == "personal")
            inflater.inflate(R.menu.menu_editpencil, menu)
        else //skill_specific, completed, interesting
            inflater.inflate(R.menu.menu_showprofile, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                if(type == "skill_specific" || type == "interesting" || type == "completed") {
                    /*Toast.makeText(
                        context, "Show user profile",
                        Toast.LENGTH_SHORT
                    ).show()*/

                    profileViewModel.retrieveTimeSlotProfileData(userId)
                    findNavController().navigate(
                        R.id.action_nav_timeSlotDetails_to_nav_showProfile,
                        bundleOf("point_of_origin" to type, "userId" to userId)
                    )
                }
                else if (type == "personal") { //personal
                    /*Toast.makeText(
                        context, "Edit time slot",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    editTimeslot() //evoked when the pencil button is pressed
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Show chat from the current request of the current timeSlot */
    fun showTimeSlotRequest(timeSlot: TimeSlot) {
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