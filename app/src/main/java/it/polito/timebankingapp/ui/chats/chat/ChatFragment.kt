package it.polito.timebankingapp.ui.chats.chat

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.Request
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.chat.ChatsListItem
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import org.w3c.dom.Text
import java.util.*


class ChatFragment : Fragment(R.layout.fragment_chat) {

    private lateinit var rv: RecyclerView

    private lateinit var adTmp: ChatViewAdapter
    private lateinit var textMessage: EditText
    private lateinit var layoutManager: LinearLayoutManager

    private val chatVm: ChatViewModel by activityViewModels()
    //private val profileVM : ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        chatVm.chat.observe(viewLifecycleOwner) { cli ->
            updateChatUi(view, cli)
        }

        setRecyclerViewAdapter(view)

        chatVm.chatMessages.observe(viewLifecycleOwner) {
            adTmp = ChatViewAdapter(it.toMutableList(), ::sendMessage)
            rv.adapter = adTmp
            rv.scrollToPosition(adTmp.itemCount - 1)
        }

        textMessage = view.findViewById(R.id.edit_gchat_message)
        val sendButton = view.findViewById<Button>(R.id.button_gchat_send)

        sendButton.setOnClickListener {
            if (textMessage.text.isNotEmpty()) {
                sendMessage(
                    ChatMessage(
                        Firebase.auth.currentUser!!.uid,
                        textMessage.text.toString(),
                        Calendar.getInstance(),
                    )
                )
                textMessage.text.clear()
            }

        }
    }

    private fun setRecyclerViewAdapter(v: View) {

        rv = v.findViewById(R.id.recycler_gchat)
        rv.layoutManager = LinearLayoutManager(context)

        adTmp = ChatViewAdapter(mutableListOf(), ::sendMessage)
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


    private fun updateChatUi(v: View, cli: ChatsListItem) {
        val civProfilePic = v.findViewById<CircleImageView>(R.id.chat_profile_pic)
        val rbReviewScore = v.findViewById<RatingBar>(R.id.ratingBar)
        val tvReviewsNumber = v.findViewById<TextView>(R.id.fragment_login_tv_reviews_count)
        val tvProfileName = v.findViewById<TextView>(R.id.chat_profile_name)

        when (cli.status) {
            Request.STATUS_INTERESTED -> Log.d("chatFragment", "STATUS INTERESTED")
            Request.STATUS_ACCEPTED -> Log.d("chatFragment", "STATUS ACCEPTED")
        }


        Helper.loadImageIntoView(civProfilePic, cli.otherProfilePic)

        rbReviewScore.rating = cli.avgReviews
        tvReviewsNumber.text = cli.nReviews.toString()


        civProfilePic.setOnClickListener {
            v.findNavController().navigate(
                R.id.action_nav_chat_to_nav_showProfile,
                bundleOf("point_of_origin" to "skill_specific")
            )
        }

        tvProfileName.text = cli.otherUserName

        tvProfileName.setOnClickListener {
            v.findNavController().navigate(
                R.id.action_nav_chat_to_nav_showProfile,
                bundleOf("point_of_origin" to "skill_specific")
            )
        }
    }


    fun sendMessage(chatMessage: ChatMessage) {
        chatVm.sendMessage(chatMessage)
    }

}


