package it.polito.timebankingapp.ui.timeslots.timeslot_details

import android.os.Bundle
import android.view.*
import android.widget.Button
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
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.Helper.Companion.toUser
import it.polito.timebankingapp.model.timeslot.TimeSlot
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString("userId").toString()
        type = arguments?.getString("point_of_origin").toString() //skill_specific or personal
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val ts = globalModel.selectedTimeSlot.value
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

        if(type == "skill_specific"){
            view.findViewById<ConstraintLayout>(R.id.layout_offerer).also { it.visibility = View.VISIBLE }
            val btnRequestService = view.findViewById<Button>(R.id.button_request_service).also { it.visibility = View.VISIBLE }
            val btnOpenChat = view.findViewById<Button>(R.id.openChatButton)
            btnOpenChat.setOnClickListener{
                showTimeSlotRequest(timeSlot)
                findNavController().navigate(R.id.nav_chat)
            }


            /* Rememeber to update number of chats for that timeSlot*/
            btnRequestService.setOnClickListener{
                if (ts != null) {
                    profileViewModel.getUserFromId(ts.userId).addOnSuccessListener {
                        val chatId = globalModel.requestTimeSlot(ts, profileViewModel.user.value!!,  it.toUser()!!)
                            .addOnSuccessListener {
                            Snackbar.make(view, "Request correctly sent!", Snackbar.LENGTH_SHORT).show()

                        }.addOnFailureListener{
                            Snackbar.make(view, "Oops, something gone wrong!", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
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
        if(type == "skill_specific")
            inflater.inflate(R.menu.menu_showprofile, menu)
        else //personal
            inflater.inflate(R.menu.menu_editpencil, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option1 -> {
                if(type == "skill_specific" || type == "interesting") {
                    Toast.makeText(
                        context, "Show user profile",
                        Toast.LENGTH_SHORT
                    ).show()
                    profileViewModel.clearTimeSlotUserImage()
                    profileViewModel.retrieveTimeSlotProfileData(userId)
                    findNavController().navigate(
                        R.id.action_nav_timeSlotDetails_to_nav_showProfile,
                        bundleOf("point_of_origin" to type, "userId" to userId)
                    )
                }
                else if (type == "personal") { //personal
                    Toast.makeText(
                        context, "Edit time slot",
                        Toast.LENGTH_SHORT
                    ).show()
                    editTimeslot() //evoked when the pencil button is pressed
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Show chat from the current user to the current timeSlot */
    fun showTimeSlotRequest(timeSlot: TimeSlot) {
        val chatId = Helper.makeRequestId(timeSlot.id, Firebase.auth.uid!!)
        chatVm.selectToOffererChatFromTimeSlot(timeSlot)
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