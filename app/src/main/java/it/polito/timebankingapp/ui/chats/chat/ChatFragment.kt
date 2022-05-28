package it.polito.timebankingapp.ui.chats.chat

import android.os.Bundle
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.Helper
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.model.timeslot.TimeSlot
import it.polito.timebankingapp.model.user.User
import it.polito.timebankingapp.ui.chats.ChatViewModel
import it.polito.timebankingapp.ui.profile.ProfileViewModel
import it.polito.timebankingapp.ui.timeslots.TimeSlotsViewModel
import java.util.*


class ChatFragment : Fragment(R.layout.fragment_chat_list) {

    private lateinit var rv : RecyclerView

    private lateinit var adTmp: ChatViewAdapter
    private lateinit var textMessage: EditText
    private lateinit var layoutManager: LinearLayoutManager

    private val chatVm : ChatViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv = view.findViewById(R.id.recycler_gchat)
        rv.layoutManager = LinearLayoutManager(context)

        adTmp = ChatViewAdapter(mutableListOf(), ::sendMessage)
        rv.adapter = adTmp
        rv.scrollToPosition(adTmp.itemCount-1)

        val tempList = mutableListOf<ChatMessage>()
        chatVm.chatMessages.observe(viewLifecycleOwner) {
            adTmp = ChatViewAdapter(it.toMutableList(), ::sendMessage)
            rv.adapter = adTmp
            rv.scrollToPosition(adTmp.itemCount-1)
        }


            rv.addOnLayoutChangeListener(OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                if (bottom < oldBottom && rv.adapter?.itemCount!! > 0 )  {
                    rv.postDelayed(Runnable {
                        rv.smoothScrollToPosition(
                            (rv.adapter?.itemCount ?: 1) - 1
                        )
                    }, 100)
                }
            })


        //questa lista deve essere ipoteticamente ritornata dal corrispettivo ChatsListItem
//        val tempList = mutableListOf<ChatMessage>()
//
//        //val res =  BitmapFactory.decodeResource(requireContext().resources,R.drawable.default_avatar)
//        tempList.add(ChatMessage("1","user1","Ciao","17/05/2022-11:05"))
//        tempList.add(ChatMessage("2","user2","Ciao, questa è una prova per vedere quanto può essere lungo un messaggio","17/05/2022-11:07"))
//        tempList.add(ChatMessage("3","user1","Arrivederci","17/05/2022-11:09"))
//
//        (activity as MainActivity?)?.supportActionBar?.title = "Nome Cognome"
//
//        adTmp = ChatViewAdapter(tempList)
//        rv.adapter = adTmp

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        ratingBar.rating = 4.5F

        val profilePic = view.findViewById<CircleImageView>(R.id.chat_profile_pic)
//        loadImageIntoView(profilePic, arguments?.getString("profilePic"))



        chatVm.otherProfilePic.observe(viewLifecycleOwner){ picUrl ->
            Helper.loadImageIntoView(profilePic, picUrl)
        }
        profilePic.setOnClickListener{
            findNavController().navigate(R.id.action_nav_chat_to_nav_showProfile, bundleOf("point_of_origin" to "skill_specific"))
        }
        val profileName = view.findViewById<TextView>(R.id.chat_profile_name)
        profileName.setOnClickListener{
//            profileVM.retrieveTimeSlotProfileData(arguments?.getString("profileId") ?: "")
            findNavController().navigate(R.id.action_nav_chat_to_nav_showProfile, bundleOf("point_of_origin" to "skill_specific"))
        }
        chatVm.otherUserName.observe(viewLifecycleOwner){
            profileName.text = it
        }



        layoutManager = rv.layoutManager as LinearLayoutManager
        layoutManager.stackFromEnd = true
        textMessage = view.findViewById(R.id.edit_gchat_message)
        val sendButton = view.findViewById<Button>(R.id.button_gchat_send)

        sendButton.setOnClickListener {
            if(textMessage.text.isNotEmpty()) {
//                adTmp.addMessage(
//                    ChatMessage(
//                        Firebase.auth.currentUser!!.uid,
//                        textMessage.text.toString(),
//                        Calendar.getInstance()
//                    )
//                )
                sendMessage(
                    ChatMessage(
                    Firebase.auth.currentUser!!.uid,
                    textMessage.text.toString(),
                    Calendar.getInstance(),
                ))

                textMessage.text.clear()

            }
        }

    }


    fun DocumentSnapshot.toUser(): User? {

        return try {
            val pic = get("pic") as String
            val fullName = get("fullName") as String
            val nick = get("nick") as String
            val email = get("email") as String
            val location = get("location") as String
            val desc = get("description") as String
            val balance = get("balance") as Long
            val skills = get("skills") as MutableList<String>

            User(id, pic, fullName, nick, email, location, desc, balance.toInt(), skills)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDetach() {
        chatVm.clearChats()
        super.onDetach()

    }
        /*
        textMessage.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                addMessage()
                handled = true
            }
            handled
        }*/

//    @SuppressLint("SimpleDateFormat")
//    private fun addMessage(){
//        val dateCalendar = GregorianCalendar().time
//        val dateFormatter = SimpleDateFormat("dd/MM/yyyy-HH:mm")
//        val timestamp = dateFormatter.format(dateCalendar)
//
//        if (textMessage.length() > 0) {
//            //temporary method
//            adTmp.addMessage(ChatMessage( "user2", textMessage.text.toString(), Calendar.getInstance()))
//            textMessage.text.clear();
//            layoutManager!!.scrollToPosition(adTmp.itemCount - 1);
//        }
//    }

    fun sendMessage(chatMessage: ChatMessage) {
        chatVm.sendMessage(chatMessage)
    }



    /*private fun loadImageIntoView(view: CircleImageView, url: String?) {
        val storageReference = FirebaseStorage.getInstance().reference
        val picRef = storageReference.child(url ?: "")
        picRef.downloadUrl
            .addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                Glide.with(view.context)
                    .load(downloadUrl)
                    .into(view)
            }
            .addOnFailureListener { e ->
                Log.w(
                    "loadImage",
                    "Getting download url was not successful.",
                    e
                )
            }
    }*/
}


