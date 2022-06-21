package it.polito.timebankingapp.ui.chats.chat

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.model.Chat.Companion.STATUS_ACCEPTED
import it.polito.timebankingapp.model.Chat.Companion.STATUS_COMPLETED
import it.polito.timebankingapp.model.Chat.Companion.STATUS_DISCARDED
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.user.CompactUser
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import java.util.*


class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var rv: RecyclerView
    private lateinit var currentChat: Chat
    private lateinit var adTmp: ChatViewAdapter
    private lateinit var etMessageInput: EditText
    private lateinit var layoutManager: LinearLayoutManager


    private lateinit var btnAcceptRequest: Button
    private lateinit var btnRequestService: Button
    private lateinit var btnDiscardRequest: Button

    private lateinit var rlSendMsgBar: RelativeLayout
    private lateinit var cvMessageChatStatus: CardView
    private lateinit var tvChatStatusTitle: TextView
    private lateinit var tvChatStatusInfo: TextView

    private var passedFromInterested: Boolean = false


    private val chatVm: ChatViewModel by activityViewModels()
    private val profileVM: ProfileViewModel by activityViewModels()
    private val timeSlotVm: TimeSlotsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        passedFromInterested = false

        chatVm.chat.observe(viewLifecycleOwner) {
            if (!it.isEmpty()) { /* Update Ui if it's not empty*/
                currentChat = it
                Log.d(
                    TAG,
                    "UI rendering... reqId: ${it.requestId} status: ${it.status} title: ${it.timeSlot.title} tsStatus: ${it.timeSlot.status}"
                )
                updateChatUi(view, currentChat)
            }

        }

        setRecyclerViewAdapter(view)

        chatVm.chatMessages.observe(viewLifecycleOwner) {

            adTmp = ChatViewAdapter(
                it.toMutableList(),
                ::sendMessage,
                chatVm.chat.value?.status == STATUS_COMPLETED
            )

            if(currentChat.lastMessage.userId != Firebase.auth.uid){
                chatVm.resetUnreadMsgs()
            }

            rv.adapter = adTmp
            rv.scrollToPosition(adTmp.itemCount - 1)
        }

        rlSendMsgBar = view.findViewById(R.id.layout_gchat_chatbox)
        cvMessageChatStatus = view.findViewById(R.id.fragment_chat_cv_status)

        Log.d(TAG, "status visibile: ${cvMessageChatStatus.isVisible}")

        tvChatStatusTitle = view.findViewById(R.id.fragment_chat_tv_status_main)
        tvChatStatusInfo = view.findViewById(R.id.fragment_chat_tv_status_second)

        btnAcceptRequest = view.findViewById(R.id.fragment_chat_btn_accept)
        btnDiscardRequest = view.findViewById(R.id.fragment_chat_btn_discard)
        btnRequestService = view.findViewById(R.id.fragment_chat_btn_request_service)


        /* Navigation to showProfile*/
        val clOtherProfile =
            view.findViewById<ConstraintLayout>(R.id.fragment_chat_cl_other_profile)
        clOtherProfile.setOnClickListener {
            val userId = Helper.getOtherUser(currentChat).id
            profileVM.retrieveTimeSlotProfileData(userId)

            findNavController().navigate(
                R.id.action_nav_chat_to_nav_showProfile,
                bundleOf(
                    "point_of_origin" to "skill_specific",
                    "userId" to userId
                ), /* TODO (Edit this bundle in order to avoid casini ) */
            )
        }

        /* Navigation to detail */
        val tvTimeSlotTitle = view.findViewById<TextView>(R.id.fragment_chat_tv_time_slot_title)
        tvTimeSlotTitle.setOnClickListener {
            timeSlotVm.setSelectedTimeSlot(currentChat.timeSlot)
            Log.d(TAG, "$currentChat ${currentChat.offerer}")
            findNavController().navigate(
                R.id.action_nav_chat_to_nav_timeSlotDetails,
                bundleOf("isPersonal" to (currentChat.offerer.id == Firebase.auth.uid))
            )
        }

        val btnSendMessage = view.findViewById<ImageButton>(R.id.button_gchat_send)
        btnSendMessage.imageAlpha = 0x3f

        etMessageInput = view.findViewById(R.id.edit_gchat_message)

        etMessageInput.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                handled = true
            }
            handled
        })

        etMessageInput.doAfterTextChanged {
            Log.d(TAG, "...text changed: $it enable: ${!it.isNullOrEmpty()}")
            val enabled = !it.isNullOrEmpty()
            btnSendMessage.isEnabled = enabled
            btnSendMessage.imageAlpha = if (enabled) 0xFF else 0x3f
        }

        btnSendMessage.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        if (etMessageInput.text.toString().trim().isNotEmpty()) {
            btnRequestService.isEnabled = false
            sendMessage(
                ChatMessage(
                    Firebase.auth.currentUser!!.uid,
                    etMessageInput.text.toString().trim(),
                    Calendar.getInstance().time,
                )
            )
            etMessageInput.text.clear()
        }
    }


    private fun setRecyclerViewAdapter(v: View) {

        rv = v.findViewById(R.id.recycler_gchat)
        rv.layoutManager = LinearLayoutManager(context)

        adTmp = ChatViewAdapter(
            mutableListOf(),
            ::sendMessage,
            chatVm.chat.value?.status == STATUS_DISCARDED || chatVm.chat.value?.status == STATUS_ACCEPTED
        )
        rv.adapter = adTmp
        rv.scrollToPosition(adTmp.itemCount - 1)

        rv.addOnLayoutChangeListener(OnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom && rv.adapter?.itemCount!! > 0) {
                rv.postDelayed(Runnable {
                    rv.smoothScrollToPosition(
                        (rv.adapter?.itemCount ?: 1) - 1
                    )
                }, 100)
            }
        })


        layoutManager = rv.layoutManager as LinearLayoutManager
        layoutManager.stackFromEnd = true

    }


    private fun updateChatUi(v: View, cli: Chat) {
        val civProfilePic = v.findViewById<CircleImageView>(R.id.chat_profile_pic)
        val rbReviewScore = v.findViewById<RatingBar>(R.id.fragment_chat_rb_review_score)
        val tvReviewsNumber = v.findViewById<TextView>(R.id.fragment_chat_tv_reviews_count)
        val tvTimeSlotTitle = v.findViewById<TextView>(R.id.fragment_chat_tv_time_slot_title)
        val tvProfileName = v.findViewById<TextView>(R.id.chat_profile_name)


        val pbProfilePic = v.findViewById<ProgressBar>(R.id.fragment_chat_pb_profile_pic)

        val otherUser: CompactUser = Helper.getOtherUser(cli)
        Helper.loadImageIntoView(civProfilePic, pbProfilePic, otherUser.profilePicUrl)
        tvTimeSlotTitle.text = cli.timeSlot.title
        tvProfileName.text = otherUser.nick

        val sendButton = v.findViewById<ImageButton>(R.id.button_gchat_send)


        btnRequestService.setOnClickListener {
            //Helper.setConfirmationOnButton(requireContext(), btnRequestService) <= add if you can't do it without it
            /*btnRequireService.isEnabled = false *//*TODO(Reabilitate if error happens during requests) */
            chatVm.requestService(cli)
        }

        btnAcceptRequest.setOnClickListener {
            chatVm.acceptRequestAndUpdate(cli).addOnSuccessListener { res ->
                if (res) {
                    val msg = "TimeSlot correctly assigned"
                    val snackBar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
                    snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()
                }
                /* balance sufficiente*/

            }
                .addOnFailureListener {/* Some problem happened */
                }
            /*btnAcceptRequest.isEnabled = false
            btnDiscardRequest.alpha = 0.8F
            btnDiscardRequest.isEnabled = false*/
        }

        btnDiscardRequest.setOnClickListener {
            chatVm.discardRequest(cli.requestId)
            /*btnAcceptRequest.isEnabled = false
            btnDiscardRequest.alpha = 0.8F
            btnDiscardRequest.isEnabled = false*/
        }


        /* Chatting to the offerer */
        if (cli.getType() == Chat.CHAT_TYPE_TO_OFFERER) { // TODO(invert type and status if, in order to reduce duplicated code)
            btnDiscardRequest.visibility = View.GONE
            btnAcceptRequest.visibility = View.GONE
            btnRequestService.visibility = View.VISIBLE
            rbReviewScore.rating = otherUser.asOffererReview.score.toFloat()
            tvReviewsNumber.text = "${otherUser.asOffererReview.number} reviews (as offerer)"
            Log.d(TAG, "TYPE TO OFFERER")
            when (cli.status) {
                Chat.STATUS_UNINTERESTED -> {
                    btnRequestService.isEnabled = true

                    if (passedFromInterested) {
                        val msg =
                            "Offerer tried to accept your request but your balance was not enough." +
                                    "Your request has been deleted"
                        val snackBar = Snackbar.make(v, msg, Snackbar.LENGTH_LONG)
                        snackBar.setAction("DISMISS") { snackBar.dismiss() }.show()

                        cvMessageChatStatus.visibility = View.GONE
                        Helper.resetConfirmationOnButton(requireContext(), btnRequestService)
                        passedFromInterested = false
                    }
                    /*cvMessageChatStatus.visibility = View.GONE*/
                    Log.d(TAG, "STATUS UNIINTERESTED")
                }
                Chat.STATUS_INTERESTED -> {
                    if (!passedFromInterested) passedFromInterested = true

                    /*cvMessageChatStatus.visibility = View.GONE*/
                    Log.d(TAG, "STATUS INTERESTED")
                    btnRequestService.text = "Service requested"
                    cvMessageChatStatus.visibility = View.GONE
                    Helper.setConfirmationOnButton(requireContext(), btnRequestService)
                }
                Chat.STATUS_ACCEPTED -> {
                    Log.d(
                        TAG,
                        "STATUS ACCEPTED"
                    );Helper.setConfirmationOnButton(
                        requireContext(),
                        btnRequestService
                    ); chatToAccepted(v, cli.getType())
                }
                Chat.STATUS_DISCARDED -> {
                    Log.d(TAG, "STATUS DISCARDED");
                    Helper.setConfirmationOnButton(requireContext(), btnRequestService);
                    chatToDiscarded(v, cli.getType())
                }
                Chat.STATUS_COMPLETED -> {
                    Log.d(TAG, "STATUS COMPLETED");
                    sendButton.isEnabled = false
                    etMessageInput.inputType = InputType.TYPE_NULL

                    cvMessageChatStatus.visibility = View.VISIBLE
                    if (cli.timeSlot.assignedTo.id == Firebase.auth.uid!!) {
                        tvChatStatusTitle.text = "TimeSlot is completed by you"
                        tvChatStatusInfo.text =
                            "TimeSlot is completed. Put a review in Completed Time Slots section to the offerer " +
                                    "if you haven't already done."
                    } else {
                        tvChatStatusTitle.text = "TimeSlot completed by another user"
                        tvChatStatusInfo.text =
                            "TimeSlot is completed by another user. Chat has been disabled"
                    }

                    Helper.setConfirmationOnButton(requireContext(), btnRequestService);
                    sendButton.visibility = View.GONE
                    etMessageInput.hint = "Chat disabled"
                }

            }
        } else {/* Chatting to the requester */
            btnDiscardRequest.visibility = View.VISIBLE
            btnAcceptRequest.visibility = View.VISIBLE
            btnRequestService.visibility = View.GONE
            rbReviewScore.rating = otherUser.asRequesterReview.score.toFloat()
            tvReviewsNumber.text = "${otherUser.asRequesterReview.number} reviews (as requester)"
            Log.d(TAG, "TYPE TO REQUESTER")

            when (cli.status) {
                Chat.STATUS_UNINTERESTED -> {
                    btnRequestService.isEnabled = true

                    if (passedFromInterested) {
                        cvMessageChatStatus.visibility = View.GONE
                        setFragmentResult("chatFragment", bundleOf("SNACKBAR" to true))
                        findNavController().navigateUp()
                    }
                    /*cvMessageChatStatus.visibility = View.GONE*/
                    Log.d(TAG, "STATUS UNIINTERESTED")
                }
                Chat.STATUS_INTERESTED -> {
                    if (!passedFromInterested) passedFromInterested = true
                    btnAcceptRequest.isEnabled = true
                    btnDiscardRequest.isEnabled = true
                    cvMessageChatStatus.visibility = View.GONE
                    Log.d(TAG, "STATUS INTERESTED"); /*cvMessageChatStatus.visibility = View.GONE*/

                }
                Chat.STATUS_ACCEPTED -> {
                    Log.d(TAG, "STATUS ACCEPTED");
                    chatToAccepted(v, cli.getType())
                }
                Chat.STATUS_DISCARDED -> {
                    Log.d(TAG, "STATUS DISCARDED")
                    chatToDiscarded(v, cli.getType())
                }
                Chat.STATUS_COMPLETED -> {
                    Log.d(TAG, "STATUS COMPLETED");
                    sendButton.isEnabled = false
                    etMessageInput.inputType = InputType.TYPE_NULL //disabling textMessageBox

                    tvChatStatusTitle.text = "TimeSlot completed"
                    tvChatStatusInfo.text =
                        "TimeSlot is completed. Put a review  to the requester in Completed Time Slots section " +
                                "if you haven't already done."

                    etMessageInput.inputType = InputType.TYPE_NULL
                    cvMessageChatStatus.visibility = View.VISIBLE

                    sendButton.visibility = View.GONE
                    etMessageInput.hint = "Chat disabled"

                    btnAcceptRequest.isEnabled = false
                    btnAcceptRequest.alpha = 0.6F

                    btnDiscardRequest.isEnabled = false
                    btnDiscardRequest.alpha = 0.6F
                }

            }
        }

        /*sendButton.setOnClickListener {
            if (etMessageInput.text.isNotEmpty()) {
                btnRequestService.isEnabled = false
                sendMessage(
                    ChatMessage(
                        Firebase.auth.currentUser!!.uid,
                        etMessageInput.text.toString()*//*,
                        Calendar.getInstance(),*//*
                    )
                )
                etMessageInput.text.clear()
            }
        }*/

    }

    private fun chatToDiscarded(v: View, type: Int) {
        when (type) {
            Chat.CHAT_TYPE_TO_OFFERER -> {
                tvChatStatusTitle.text =
                    getString(R.string.service_not_available); tvChatStatusInfo.text =
                    getString(R.string.service_assigned_to_another)
            }
            Chat.CHAT_TYPE_TO_REQUESTER -> {
                tvChatStatusTitle.text = "Service discarded"; tvChatStatusInfo.text =
                    "You have discarded the request by this user!"
            }
        }

        //rlSendMsgBar.visibility = View.GONE //keep chat always available to simplify things..
        cvMessageChatStatus.visibility = View.VISIBLE
        btnRequestService.isEnabled = false

        btnAcceptRequest.isEnabled = false
        btnAcceptRequest.alpha = 0.6F

        btnDiscardRequest.isEnabled = false
        btnDiscardRequest.alpha = 0.6F

    }

    private fun chatToAccepted(v: View, type: Int) {
        when (type) {
            Chat.CHAT_TYPE_TO_OFFERER -> {
                tvChatStatusTitle.text = "Service assigned"; tvChatStatusInfo.text =
                    "Service has been assigned to you. Go to the calendar section to see your assigned services."
            }
            Chat.CHAT_TYPE_TO_REQUESTER -> {
                tvChatStatusTitle.text = "Service accepted"; tvChatStatusInfo.text =
                    "Service has been assigned to this user. Go to the calendar section to see services accepted by you."
            }
        }

        //rlSendMsgBar.visibility = View.GONE //keep chat always enabled to simplify things..
        cvMessageChatStatus.visibility = View.VISIBLE


        btnRequestService.isEnabled = false

        btnAcceptRequest.isEnabled = false
        btnAcceptRequest.alpha = 0.6F

        btnDiscardRequest.isEnabled = false
        btnDiscardRequest.alpha = 0.6F

    }


    fun sendMessage(chatMessage: ChatMessage) {
        chatVm.sendMessageAndUpdate(
            currentChat,
            chatMessage
        ) /* This means that we're creating the request also*/
    }

    override fun onDestroy() {
        Log.d(TAG, "View destroyed")
        chatVm.clearChat()
        super.onDestroy()
    }

    companion object {
        const val TAG = "ChatFragment"
    }

}


