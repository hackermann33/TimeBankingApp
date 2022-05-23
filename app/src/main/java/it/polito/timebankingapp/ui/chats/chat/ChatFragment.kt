package it.polito.timebankingapp.ui.chats.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.timebankingapp.MainActivity
import it.polito.timebankingapp.R
import it.polito.timebankingapp.model.chat.ChatMessage
import it.polito.timebankingapp.ui.chats.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*


class ChatFragment : Fragment(R.layout.fragment_chat_list) {

    private lateinit var rv : RecyclerView

    private lateinit var adTmp: ChatViewAdapter
    private lateinit var textMessage: EditText
    private lateinit var layoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        rv = view.findViewById(R.id.recycler_gchat)
        rv.layoutManager = LinearLayoutManager(context)

        val tempList = mutableListOf<ChatMessage>()
        chatVm.chatMessages.observe(viewLifecycleOwner) {
            adTmp = ChatViewAdapter(it.toMutableList(), ::sendMessage)
            rv.adapter = adTmp
        }

        //questa lista deve essere ipoteticamente ritornata dal corrispettivo ChatsListItem
        val tempList = mutableListOf<ChatMessage>()

        //val res =  BitmapFactory.decodeResource(requireContext().resources,R.drawable.default_avatar)
        tempList.add(ChatMessage("1","user1","Ciao","17/05/2022-11:05"))
        tempList.add(ChatMessage("2","user2","Ciao, questa è una prova per vedere quanto può essere lungo un messaggio","17/05/2022-11:07"))
        tempList.add(ChatMessage("3","user1","Arrivederci","17/05/2022-11:09"))

        (activity as MainActivity?)?.supportActionBar?.title = "Nome Cognome"

        adTmp = ChatViewAdapter(tempList)
        rv.adapter = adTmp

        layoutManager = rv.layoutManager as LinearLayoutManager
        textMessage = view.findViewById(R.id.edit_gchat_message)
        val sendButton = view.findViewById<Button>(R.id.button_gchat_send)

        sendButton.setOnClickListener {
            if(textMessage.text.isNotEmpty()) {
                adTmp.addMessage(
                    ChatMessage(
                        Firebase.auth.currentUser!!.uid,
                        textMessage.text.toString(),
                        Calendar.getInstance()
                    )
                )
                textMessage.text.clear()
            }
        }

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
}


