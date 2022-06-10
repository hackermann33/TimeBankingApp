package it.polito.timebankingapp.ui.chats.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.Chat
import it.polito.timebankingapp.model.Chat.Companion.STATUS_ACCEPTED
import it.polito.timebankingapp.model.Chat.Companion.STATUS_DISCARDED
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.user.CompactUser


class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var rv: RecyclerView
    private lateinit var currentChat: Chat
    private lateinit var adTmp: ChatViewAdapter
    private lateinit var textMessage: EditText
    private lateinit var layoutManager: LinearLayoutManager



    private lateinit var btnAcceptRequest:Button
    private lateinit var btnRequestService: Button
    private lateinit var btnDiscardRequest: Button

    private lateinit var rlSendMsgBar : RelativeLayout
    private lateinit var cvMessageChatStatus : CardView
    private lateinit var tvChatStatus :TextView




    private val chatVm: ChatViewModel by activityViewModels()
    //private val profileVM : ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        /*TODO(To remove glitch between transictions (require -> offerer) use a bundle to understand where do you come from)  */
        /*chatVm.chat.observe(viewLifecycleOwner) { cli ->
                currentChat = cli
                updateChatUi(view, cli)
        }
        */
        chatVm.isLoading.observe(viewLifecycleOwner){
            if(!it){
                currentChat = chatVm.chat.value!!
                updateChatUi(view, currentChat)
            }
        }

        setRecyclerViewAdapter(view)

        chatVm.chatMessages.observe(viewLifecycleOwner) {
            adTmp = ChatViewAdapter(
                it.toMutableList(),
                ::sendMessage,
                chatVm.chat.value?.status == STATUS_DISCARDED || chatVm.chat.value?.status == STATUS_ACCEPTED
            )

            rv.adapter = adTmp
            rv.scrollToPosition(adTmp.itemCount - 1)
        }

        val sendMsgBar : RelativeLayout = view.findViewById(R.id.layout_gchat_chatbox)
        val cardChatDiscarded : CardView = view.findViewById(R.id.card_chat_discarded)
        val tvChatDiscarded : TextView = view.findViewById(R.id.tv_chat_discarded)
        val btnAcceptRequest = view.findViewById<Button>(R.id.fragment_chat_btn_accept)
        val btnDiscardRequest = view.findViewById<TextView>(R.id.fragment_chat_btn_discard)
        val btnRequireService = view.findViewById<Button>(R.id.fragment_chat_btn_request_service)



        /*if(chatVm.chat.value?.status == STATUS_DISCARDED) {
            if(chatVm.chat.value?.offerer!!.id == Firebase.auth.uid) *//* Discarded and I'm Offerer *//*
                tvChatDiscarded.text = getString(R.string.you_assigned_service_to_another)
            if(chatVm.chat.value?.requester!!.id == Firebase.auth.uid) *//* Discarded and I'm Requester *//*
                tvChatDiscarded.text = getString(R.string.this_service_has_been_assigned_to_another_user)
            sendMsgBar.visibility = View.GONE
            cardChatDiscarded.visibility = View.VISIBLE
            btnRequireService.isEnabled = false
            btnAcceptRequest.isEnabled = false
            btnDiscardRequest.isEnabled = false
        }
        if(chatVm.chat.value?.status == STATUS_ACCEPTED){
            if(chatVm.chat.value?.offerer!!.id == Firebase.auth.uid) *//* Accepted and I'm Offerer *//*
                tvChatDiscarded.text = getString(R.string.assigned_to_him)
            if(chatVm.chat.value?.requester!!.id == Firebase.auth.uid) *//* Accepted and I'm Requester *//*
                tvChatDiscarded.text = getString(R.string.assigned_to_you)
            sendMsgBar.visibility = View.GONE
            cardChatDiscarded.visibility = View.VISIBLE
            btnRequireService.isEnabled = false
            btnAcceptRequest.isEnabled = false
            btnDiscardRequest.isEnabled = false
        }*/
        textMessage = view.findViewById(R.id.edit_gchat_message)


        val sendButton = view.findViewById<Button>(R.id.button_gchat_send)
        sendButton.setOnClickListener {
            if (textMessage.text.isNotEmpty()) {
                btnRequireService.isEnabled = false
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
    }

    override fun onDestroy() {
        //chatVm.clearChat()
        super.onDestroy()
    }

    private fun setRecyclerViewAdapter(v: View) {

        rv = v.findViewById(R.id.recycler_gchat)
        rv.layoutManager = LinearLayoutManager(context)

        adTmp = ChatViewAdapter(mutableListOf(), ::sendMessage, chatVm.chat.value?.status == STATUS_DISCARDED || chatVm.chat.value?.status == STATUS_ACCEPTED)
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
        btnAcceptRequest = v.findViewById<Button>(R.id.fragment_chat_btn_accept)
        btnRequestService = v.findViewById<Button>(R.id.fragment_chat_btn_request_service)
        btnDiscardRequest = v.findViewById<Button>(R.id.fragment_chat_btn_discard)


        rlSendMsgBar = v.findViewById(R.id.layout_gchat_chatbox)
        cvMessageChatStatus = v.findViewById(R.id.card_chat_discarded)
        tvChatStatus= v.findViewById(R.id.tv_chat_discarded)



        val pbProfilePic = v.findViewById<ProgressBar>(R.id.fragment_chat_pb_profile_pic)

        val otherUser: CompactUser = Helper.getOtherUser(cli)
        Helper.loadImageIntoView(civProfilePic, pbProfilePic, otherUser.profilePicUrl)


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


        /* TODO(When image is clicked, navigation is not to the correct profile !!!) */
        when (cli.getType()) {

            /* Chatting to the offerer */
            Chat.CHAT_TYPE_TO_OFFERER -> {
                btnDiscardRequest.visibility = View.GONE
                btnAcceptRequest.visibility = View.GONE
                btnRequestService.visibility = View.VISIBLE
                Log.d(TAG, "TYPE TO OFFERER")
                when (cli.status) {
                    Chat.STATUS_UNINTERESTED -> {
                        btnRequestService.isEnabled = true
                        Log.d(TAG, "STATUS UNIINTERESTED")
                    }
                    Chat.STATUS_INTERESTED -> {
                        Log.d(TAG, "STATUS INTERESTED")
                        btnRequestService.text = "Service requested"
                        Helper.setConfirmationOnButton(requireContext(), btnRequestService)
                    }
                    else -> {
                        Helper.setConfirmationOnButton(
                            requireContext(),
                            btnRequestService
                        )
                        /*Chat.STATUS_ACCEPTED -> {
                            Log.d(
                                TAG,
                                "STATUS ACCEPTED"
                            );Helper.setConfirmationOnButton(
                            requireContext(),
                            btnRequestService
                        ); chatToAccepted(v, cli)
                        }
                        Chat.STATUS_DISCARDED -> {
                            Log.d(
                                TAG,
                                "STATUS DISCARDED"
                            ); Helper.setConfirmationOnButton(
                            requireContext(),
                            btnRequestService
                        ); chatToDiscarded(v)
                        }*/

                    }
                }

            }
            Chat.CHAT_TYPE_TO_REQUESTER -> {
                btnDiscardRequest.visibility = View.VISIBLE
                btnAcceptRequest.visibility = View.VISIBLE
                btnRequestService.visibility = View.GONE
                rbReviewScore.rating = otherUser.asRequesterReview.score.toFloat()
                tvReviewsNumber.text = "${otherUser.asRequesterReview.number} reviews"
                Log.d(TAG, "TYPE TO REQUESTER")
                when (cli.status) {
                    Chat.STATUS_UNINTERESTED -> Log.d(TAG, "STATUS UNINTERESTED")
                    Chat.STATUS_INTERESTED -> Log.d(TAG, "STATUS INTERESTED")
                    Chat.STATUS_ACCEPTED -> {Log.d(TAG, "STATUS ACCEPTED");
                        chatToAccepted(v, cli)
                    }
                    Chat.STATUS_DISCARDED -> {
                        Log.d(TAG, "STATUS DISCARDED")
                        chatToDiscarded(v)
                    }
                }
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
        tvTimeSlotTitle.text = cli.timeSlot.title
        tvProfileName.text = otherUser.nick
/*
        tvProfileName.setOnClickListener {
            v.findNavController().navigate(
                R.id.action_nav_chat_to_nav_showProfile,
                bundleOf("point_of_origin" to "skill_specific")
            )
        }*/
    }

    private fun chatToDiscarded(v: View) {
        tvChatStatus.text = getString(R.string.this_service_has_been_assigned_to_another_user)
        rlSendMsgBar.visibility = View.GONE
        cvMessageChatStatus.visibility = View.VISIBLE
        btnRequestService.isEnabled = false
        btnAcceptRequest.isEnabled = false
        btnDiscardRequest.isEnabled = false
    }

    private fun chatToAccepted(v: View,  c: Chat) {
        val str = if(c.timeSlot.assignedTo.id == Firebase.auth.uid) getString(R.string.assigned_to_you) else getString(R.string.assigned_to_him)
        tvChatStatus.text = str
        rlSendMsgBar.visibility = View.GONE
        cvMessageChatStatus.visibility = View.VISIBLE


        btnRequestService.isEnabled = false
        btnAcceptRequest.isEnabled = false
        btnDiscardRequest.isEnabled = false

    }


    fun sendMessage(chatMessage: ChatMessage) {
        chatVm.sendMessageAndUpdate(currentChat, chatMessage ) /* This means that we're creating the request also*/
    }

    companion object {
        const val TAG = "ChatFragment"
    }

}


