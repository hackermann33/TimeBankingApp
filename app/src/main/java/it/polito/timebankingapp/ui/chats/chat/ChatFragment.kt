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
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.util.*


class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var rv: RecyclerView
    private lateinit var currentChat: Chat
    private lateinit var adTmp: ChatViewAdapter
    private lateinit var textMessage: EditText
    private lateinit var layoutManager: LinearLayoutManager


    private lateinit var btnAcceptRequest: Button
    private lateinit var btnRequestService: Button
    private lateinit var btnDiscardRequest: Button

    private lateinit var rlSendMsgBar: RelativeLayout
    private lateinit var cvMessageChatStatus: CardView
    private lateinit var tvChatStatusTitle: TextView
    private lateinit var tvChatStatusInfo: TextView


    private val chatVm: ChatViewModel by activityViewModels()
    //private val profileVM : ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        /*TODO(To remove glitch between transictions (require -> offerer) use a bundle to understand where do you come from)  */
        /*chatVm.chat.observe(viewLifecycleOwner) { cli ->
                currentChat = cli
                updateChatUi(view, cli)
        }
        */
        chatVm.chat.observe(viewLifecycleOwner) {
            //if (!it) {
                if(!it.isEmpty()) {
                    Log.d(TAG, "UI rendering... $it ${Calendar.getInstance().timeInMillis}")
                    currentChat = it// chatVm.chat.value!!
                    updateChatUi(view, currentChat)
                }
                //}
        }

        setRecyclerViewAdapter(view)

        chatVm.chatMessages.observe(viewLifecycleOwner) {
            adTmp = ChatViewAdapter(
                it.toMutableList(),
                ::sendMessage,
                chatVm.chat.value?.status == STATUS_COMPLETED
            )

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

        val sendButton = view.findViewById<ImageButton>(R.id.button_gchat_send)
        sendButton.imageAlpha = 0x3f



        textMessage = view.findViewById(R.id.edit_gchat_message)

        textMessage.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                handled = true
            }
            handled
        })


        textMessage.doAfterTextChanged {
            Log.d(TAG, "...text changed: $it enable: ${!it.isNullOrEmpty()}")
            val enabled = !it.isNullOrEmpty()
            sendButton.isEnabled = enabled
            sendButton.imageAlpha = if(enabled) 0xFF else 0x3f
        }

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage(){
        if (textMessage.text.isNotEmpty()) {
            btnRequestService.isEnabled = false
            sendMessage(
                ChatMessage(
                    Firebase.auth.currentUser!!.uid,
                    textMessage.text.toString()/*,
                        Calendar.getInstance(),*/
                )
            )
            textMessage.text.clear()
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
        val tvTimeSlotTitle = v.findViewById<TextView>(R.id.fragment_chat_tv_offer_title)
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
            chatVm.acceptRequest(cli)
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
                  /*  cvMessageChatStatus.visibility = View.GONE*/

                    Log.d(TAG, "STATUS UNIINTERESTED")
                }
                Chat.STATUS_INTERESTED -> {
                    /*cvMessageChatStatus.visibility = View.GONE*/
                    Log.d(TAG, "STATUS INTERESTED")
                    btnRequestService.text = "Service requested"
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
                    textMessage.inputType = InputType.TYPE_NULL
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
                Chat.STATUS_UNINTERESTED -> { Log.d(TAG, "STATUS UNINTERESTED"); /*cvMessageChatStatus.visibility = View.GONE*/ }
                Chat.STATUS_INTERESTED -> {Log.d(TAG, "STATUS INTERESTED") ; /*cvMessageChatStatus.visibility = View.GONE*/}
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
                    textMessage.inputType = InputType.TYPE_NULL //disabling textMessageBox
                }

            }
        }

        sendButton.setOnClickListener {
            if (textMessage.text.isNotEmpty()) {
                btnRequestService.isEnabled = false
                sendMessage(
                    ChatMessage(
                        Firebase.auth.currentUser!!.uid,
                        textMessage.text.toString()/*,
                        Calendar.getInstance(),*/
                    )
                )
                textMessage.text.clear()
            }
        }


/*
        civProfilePic.setOnClickListener {
            v.findNavController().navigate(
                R.id.action_nav_chat_to_nav_showProfile,
                bundleOf("point_of_origin" to "skill_specific")
            )
        }
*/

/*
        tvProfileName.setOnClickListener {
            v.findNavController().navigate(
                R.id.action_nav_chat_to_nav_showProfile,
                bundleOf("point_of_origin" to "skill_specific")
            )
        }*/
    }

    private fun chatToDiscarded(v: View, type: Int) {
        when(type){
            Chat.CHAT_TYPE_TO_OFFERER -> { tvChatStatusTitle.text = getString(R.string.service_not_available); tvChatStatusInfo.text = getString(R.string.service_assigned_to_another) }
            Chat.CHAT_TYPE_TO_REQUESTER -> {tvChatStatusTitle.text = "Service discarded"; tvChatStatusInfo.text = "You have discarded the request by this user!"}
        }

        //rlSendMsgBar.visibility = View.GONE //keep chat always available to simplify things..
        cvMessageChatStatus.visibility = View.VISIBLE
        btnRequestService.isEnabled = false
        btnAcceptRequest.isEnabled = false
        btnDiscardRequest.isEnabled = false
    }

    private fun chatToAccepted(v: View, type: Int) {
        when(type){
            Chat.CHAT_TYPE_TO_OFFERER -> { tvChatStatusTitle.text = "Service assigned"; tvChatStatusInfo.text = "Service has been assigned to you. Go to the calendar section to see your assigned services." }
            Chat.CHAT_TYPE_TO_REQUESTER -> {tvChatStatusTitle.text = "Service accepted"; tvChatStatusInfo.text = "Service has been assigned to this user. Go to the calendar section to see services accepted by you."}
        }

        //rlSendMsgBar.visibility = View.GONE //keep chat always enabled to simplify things..
        cvMessageChatStatus.visibility = View.VISIBLE


        btnRequestService.isEnabled = false
        btnAcceptRequest.isEnabled = false
        btnDiscardRequest.isEnabled = false

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


